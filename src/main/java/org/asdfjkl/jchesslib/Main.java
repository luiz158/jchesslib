package org.asdfjkl.jchesslib;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.cli.*;
import org.asdfjkl.jchesslib.lib.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        Options options = new Options();

        Option inputFile = new Option("if", "input-file", true, "input file");
        inputFile.setRequired(true);
        options.addOption(inputFile);

        Option inputType = new Option("it", "input-type", true, "input type: (either \"jgn\" or \"pgn\"");
        inputType.setRequired(true);
        options.addOption(inputType);

        Option outputFile = new Option("of", "output-file", true, "output file");
        outputFile.setRequired(true);
        options.addOption(outputFile);

        Option outputType = new Option("ot", "output-type", true, "output type: (either \"jgn\" or \"pgn\"");
        outputType.setRequired(true);
        options.addOption(outputType);

        Option prettyPrint = new Option("d", "debug", false, "enable pretty-printing for debugging (no valid jgn files)");
        prettyPrint.setRequired(false);
        options.addOption(prettyPrint);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        long startTime = System.currentTimeMillis();

        try {
            cmd = parser.parse(options, args);

            String inputFilePath = cmd.getOptionValue("input-file");
            String inputTypeString = cmd.getOptionValue("input-type");
            String outputFileString = cmd.getOptionValue("output-file");
            String outputTypeString = cmd.getOptionValue("output-type");

            if(!(inputTypeString.equals("jgn") || inputTypeString.equals("pgn"))) {
                throw new ParseException("input-type must be either \"jgn\" or \"pgn\"");
            }

            if(!(outputTypeString.equals("jgn") || outputTypeString.equals("pgn"))) {
                throw new ParseException("output-type must be either \"jgn\" or \"pgn\"");
            }

            // case 1: input-pgn, output-pgn
            if(inputTypeString.equals("pgn") && (outputTypeString.equals("pgn"))) {

                PgnReader reader = new PgnReader();
                if (reader.isIsoLatin1(inputFilePath)) {
                    reader.setEncodingIsoLatin1();
                }

                PgnPrinter printer = new PgnPrinter();

                ArrayList<Long> offsets = reader.scanPgn(inputFilePath);
                OptimizedRandomAccessFile raf = null;
                BufferedWriter out = null;
                try {
                    raf = new OptimizedRandomAccessFile(inputFilePath, "r");
                    out = Files.newBufferedWriter(Path.of(outputFileString), StandardCharsets.UTF_8);
                    for (int i = 0; i < offsets.size(); i++) {
                        long offset_i = offsets.get(i);
                        raf.seek(offset_i);
                        Game g = reader.readGame(raf);
                        String pgn = printer.printGame(g);
                        out.write(pgn);
                        out.write("\n\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // case 2: input-pgn, output-jgn
            if(inputTypeString.equals("pgn") && (outputTypeString.equals("jgn"))) {

                // make sure, object mapper does not close the outputstream automatically
                JsonFactory jsonFactory = new JsonFactory();
                jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

                ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
                if(cmd.hasOption("debug")) {
                    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                }
                SimpleModule module =
                        new SimpleModule("JgnPrinter", new Version(2, 1, 3, null, null, null));
                module.addSerializer(Game.class, new JgnPrinter());
                objectMapper.registerModule(module);

                PgnReader reader = new PgnReader();
                if (reader.isIsoLatin1(inputFilePath)) {
                    reader.setEncodingIsoLatin1();
                }

                ArrayList<Long> offsets = reader.scanPgn(inputFilePath);
                OptimizedRandomAccessFile raf = null;
                BufferedWriter out = null;
                try {
                    raf = new OptimizedRandomAccessFile(inputFilePath, "r");
                    out = Files.newBufferedWriter(Path.of(outputFileString), StandardCharsets.UTF_8);
                    for (int i = 0; i < offsets.size(); i++) {
                        long offset_i = offsets.get(i);
                        raf.seek(offset_i);
                        Game g = reader.readGame(raf);
                        //String jgn = objectMapper.writeValueAsString(g);
                        //byte[] jgnBytes = objectMapper.writeValueAsBytes(g);
                        //out.write(new String(jgnBytes));
                        objectMapper.writeValue(out, g);
                        //out.write
                        out.write("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (raf != null) {
                        try {
                            raf.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // case 3: input-jgn and output-pgn
            if(inputTypeString.equals("jgn") && (outputTypeString.equals("pgn"))) {

                PgnPrinter printer = new PgnPrinter();

                ObjectMapper mapper = new ObjectMapper();
                JgnReader deserializer = new JgnReader();

                BufferedWriter out = null;
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(inputFilePath, StandardCharsets.UTF_8));
                    out = Files.newBufferedWriter(Path.of(outputFileString), StandardCharsets.UTF_8);
                    String line;
                    while ((line = br.readLine()) != null) {

                        JsonNode jgnTree = mapper.readTree(line);
                        Game g = deserializer.read(jgnTree);
                        String pgn = printer.printGame(g);
                        out.write(pgn);
                        out.write("\n\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(out != null) {
                        try {
                            out.close();
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(br != null) {
                        try {
                            br.close();
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // case 4: input-jgn and output-jgn
            if(inputTypeString.equals("jgn") && (outputTypeString.equals("jgn"))) {

                ObjectMapper inputMapper = new ObjectMapper();
                JgnReader deserializer = new JgnReader();

                // make sure, object mapper does not close the outputstream automatically
                JsonFactory jsonFactory = new JsonFactory();
                jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);

                ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
                if(cmd.hasOption("debug")) {
                    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                }
                SimpleModule module =
                        new SimpleModule("JgnPrinter", new Version(2, 1, 3, null, null, null));
                module.addSerializer(Game.class, new JgnPrinter());
                objectMapper.registerModule(module);

                BufferedWriter out = null;
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(inputFilePath, StandardCharsets.UTF_8));
                    out = Files.newBufferedWriter(Path.of(outputFileString), StandardCharsets.UTF_8);
                    String line;
                    while ((line = br.readLine()) != null) {

                        JsonNode jgnTree = inputMapper.readTree(line);
                        Game g = deserializer.read(jgnTree);
                        //String jgn = objectMapper.writeValueAsString(g);
                        objectMapper.writeValue(out, g);
                        //out.write(jgn);
                        out.write("\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(out != null) {
                        try {
                            out.close();
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(br != null) {
                        try {
                            br.close();
                        } catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            long stopTime = System.currentTimeMillis();
            long timeElapsed = stopTime - startTime;
            System.out.println("elapsed time for processing: " + (timeElapsed / 1000) + " secs");


                /*
                String inputFilePath = cmd.getOptionValue("input-pgn");

                PgnReader pgnReader = new PgnReader();
                PgnPrinter pgnPrinter = new PgnPrinter();
                OptimizedRandomAccessFile raf = new OptimizedRandomAccessFile(inputFilePath, "r");
                Game g = pgnReader.readGame(raf);
                String pgn = pgnPrinter.printGame(g);
                System.out.println(pgn);

                System.out.println("-----------------------------");

                ObjectMapper objectMapper = new ObjectMapper();
                //objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                SimpleModule module =
                        new SimpleModule("JgnPrinter", new Version(2, 1, 3, null, null, null));
                module.addSerializer(Game.class, new JgnPrinter());
                objectMapper.registerModule(module);

                String json = objectMapper.writeValueAsString(g);
                System.out.println(json);
            }

            if(cmd.hasOption("input-jgn")) {

                ObjectMapper mapper = new ObjectMapper();

                String inputFilePath = cmd.getOptionValue("input-jgn");
                JsonNode jgnTree = mapper.readTree(new File(inputFilePath));

                JgnReader deserializer = new JgnReader();
                Game g = deserializer.read(jgnTree);

                PgnPrinter pgnPrinter = new PgnPrinter();
                System.out.println(pgnPrinter.printGame(g));

            }
*/


        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("jchesslib", options);
            System.exit(1);
        }
    }

    public static void printProgress(int current, int finish, String message) {

        System.out.print("\r" + message + ": "+ current + "/"+finish);

    }


}
