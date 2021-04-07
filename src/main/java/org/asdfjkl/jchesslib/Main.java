package org.asdfjkl.jchesslib;

import org.apache.commons.cli.*;
import org.asdfjkl.jchesslib.lib.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        Options options = new Options();

        Option input = new Option("i", "input", true, "input PGN file");
        input.setRequired(true);
        options.addOption(input);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            String inputFilePath = cmd.getOptionValue("input");

            PgnReader pgnReader = new PgnReader();
            PgnPrinter pgnPrinter = new PgnPrinter();
            OptimizedRandomAccessFile raf = new OptimizedRandomAccessFile(inputFilePath, "r");
            Game g = pgnReader.readGame(raf);
            String pgn = pgnPrinter.printGame(g);
                System.out.println(pgn);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("jchesslib", options);
            System.exit(1);
        } catch (IOException e) {
        e.printStackTrace();
        }
    }

    public static void printProgress(int current, int finish, String message) {

        System.out.print("\r" + message + ": "+ current + "/"+finish);

    }


}
