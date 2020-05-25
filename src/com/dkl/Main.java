package com.dkl;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.*;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;


public class Main {

    public static void main(String[] args) {

        /*
        String fnUtf8 = "C:/Users/user/MyFiles/workspace/fileUtf8.txt";
        String fnIsoLatin1 = "C:/Users/user/MyFiles/workspace/fileIsoLatin1.txt";
        try {
            OptimizedRandomAccessFile raf = new OptimizedRandomAccessFile(fnUtf8, "r");
            //for(int i =0;i<50;i++) {
            //    System.out.println(raf.read());
            //}
            String line = raf.readLine();
            try
            {
                String utf8 = new String(line.getBytes("ISO-8859-1"), "UTF-8");
                byte[] bytes = line.getBytes("ISO-8859-1");
                for(int i=0;i<bytes.length;i++) {
                    System.out.println(bytes[i]);
                }
                System.out.println(utf8);
            }
            catch (java.io.UnsupportedEncodingException e)
            {
                e.printStackTrace();
                System.exit(-1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            OptimizedRandomAccessFile raf = new OptimizedRandomAccessFile(fnIsoLatin1, "r");
            String line = raf.readLine();
            try
            {
                byte[] myBytes = line.getBytes("ISO-8859-1");
                System.out.println(line);
            }
            catch (java.io.UnsupportedEncodingException e)
            {
                e.printStackTrace();
                System.exit(-1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        */


/*
        if(args.length > 0) {
            String filename = args[0];
            PgnReader reader = new PgnReader();
            PgnPrinter printer = new PgnPrinter();
            ArrayList<Long> offsets = reader.scanPgn(filename, true);

            for(int i=0;i<offsets.size();i++) {
                OptimizedRandomAccessFile raf = null;
                try {
                    raf = new OptimizedRandomAccessFile(filename, "r");
                    raf.seek(offsets.get(i));
                    Game g = reader.readGame(raf);
                    System.out.println(printer.printGame(g));
                    System.out.println("\n");
                    raf.close();
                } catch (IOException e) {
                    System.out.println("error reading: " + filename);
                    System.out.println("game nr......: " + i);
                    e.printStackTrace();
                } finally {
                     if(raf != null) {
                         try {
                             raf.close();
                         } catch(IOException e) {
                             e.printStackTrace();
                         }
                     }
                }
            }
        } else {
            System.out.println("java -jar program.jar pgnfile");
        }
*/



        TestCases cases = new TestCases();
        //cases.runPerfT();
        //cases.pgnReadGameTest();
        //cases.pgnReadMiddleGTest();
        cases.pgnReadAllMillBaseTest();

        /*
        PgnReader reader = new PgnReader();
        PgnPrinter printer = new PgnPrinter();

        if(args.length > 0 ) {
            String filename = args[0];
            System.out.println(filename);

            try {
            OptimizedRandomAccessFile raf = new OptimizedRandomAccessFile(filename, "r");
            Game g = reader.readGame(raf);
            System.out.println(printer.printGame(g);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } */












        // write your code here
        //int x = Integer.parseUnsignedInt("2147483646");
        //System.out.println("Hello World!");
        //System.out.println(x);

        /*
        long foo = Long.parseUnsignedLong("9C15F73E62A76AE2", 16);
        System.out.println(foo);

        long bar = 0x9C15F73E62A76AE2L;
        System.out.println(bar);


        TestCases cases = new TestCases();
        cases.pgnReadAllMillBaseTest();

        */




/*
        System.out.println("Read Test 1 (Hashmap, Optimized Random Access File, String as UTF-8)");
        cases.pgnReadSingleEntryTest1();
        System.out.println("Read Test 2 (Class of Strings, Optimized Random Access File)");
        cases.pgnReadSingleEntryTest2();
        System.out.println("Read Test 3 (Class of Strings, BufferedStream)");
        cases.pgnReadSingleEntryTest3();
        System.out.println("Read Test 4 (Hashmap, BufferedStream)");
        cases.pgnReadSingleEntryTest4();
 */
        //cases.pgnScanTest();
        //cases.runPgnPrintTest();
        //cases.runZobristTest();
        //cases.runSanTest();
        //cases.fenTest();
        //cases.runPerfT();
    }
}
