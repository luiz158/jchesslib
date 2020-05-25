package com.dkl;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

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
