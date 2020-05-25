package com.dkl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class PgnReader {

    GameNode currentNode = null;
    String currentLine;  // current line
    int currentIdx = 0; // current index
    Stack<GameNode> gameStack;

    public PgnReader() {
        gameStack = new Stack<>();
    }




    /*
    bool PgnReader::isUtf8(const QString &filename) {
        // very simple way to detecting majority of encodings:
        // first try ISO 8859-1
        // open the file and read a max of 100 first bytes
        // if conversion to unicode works, try some more bytes (at most 40 * 100)
        // if conversion errors occur, we simply assume UTF-8
        //const char* iso = "ISO 8859-1";
        //const char* utf8 = "UTF-8";
        QFile file(filename);
        if(!file.open(QFile::ReadOnly)) {
            return true;
        }
        QDataStream in(&file);
        // init some char array to read bytes
        char first100arr[100];
        for(int i=0;i<100;i++) {
            first100arr[i] = 0x00;
        }
        char *first100 = first100arr;
        // prep conversion tools
        QTextCodec::ConverterState state;
        QTextCodec *codec = QTextCodec::codecForName("UTF-8");

        int iterations = 40;
        int i=0;
        int l = 100;
        bool isUtf8 = true;
        while(i<iterations && l>=100) {
            l = in.readRawData(first100, 100);
        const QString text = codec->toUnicode(first100, 100, &state);
            if (state.invalidChars > 0) {
                isUtf8 = false;
                break;
            }
            i++;
        }
        return isUtf8;
    }
    */


    /*
    int PgnReader::readGameFromString(QString &pgn_string, chess::Game *g) {
        QTextStream in(&pgn_string);
        return this->readGame(in, g);
    }

    int PgnReader::readGameFromString(QString &pgn_string, quint64 offset, chess::Game *g) {
        QString substring = QString(pgn_string.mid(offset, pgn_string.size()));
        QTextStream in(&substring);
        return this->readGame(in, g);
    }
     */


    ArrayList<Long> scanPgn(String filename, boolean isUtf8) {

        ArrayList<Long> offsets = new ArrayList<>();

        boolean inComment = false;
        long game_pos = -1;
        long last_pos = 0;

        String currentLine = "";
        OptimizedRandomAccessFile raf = null;
        try {
            raf = new OptimizedRandomAccessFile(filename, "r");
            while ((currentLine = raf.readLine()) != null) {
                // skip comments
                if (currentLine.startsWith("%")) {
                    continue;
                }

                if (!inComment && currentLine.startsWith("[")) {
                    if (game_pos == -1) {
                        game_pos = last_pos;
                    }
                    last_pos = raf.getFilePointer();
                    continue;
                }
                if ((!inComment && currentLine.contains("{"))
                        || (inComment && currentLine.contains("}"))) {
                    inComment = currentLine.lastIndexOf("{") > currentLine.lastIndexOf("}");
                }

                if (game_pos != -1) {
                    offsets.add(game_pos);
                    game_pos = -1;
                }

                last_pos = raf.getFilePointer();
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
        }
        return offsets;
    }

    public HashMap<String, String> readSingleHeader(String filename, long offset) {

        HashMap<String, String> header = new HashMap<>();

        String currentLine = "";
        OptimizedRandomAccessFile raf = null;

        boolean continueSearch = true;
        boolean foundHeader = false;

        try {
            raf = new OptimizedRandomAccessFile(filename, "r");
            raf.seek(offset);
            while ((currentLine = raf.readLine()) != null && continueSearch) {
                // skip comments
                if (currentLine.startsWith("%")) {
                    continue;
                }

                if (currentLine.startsWith("[")) {

                    foundHeader = true;
                    //
                    if (currentLine.length() > 4) {
                        int spaceOffset = currentLine.indexOf(' ');
                        int firstQuote = currentLine.indexOf('"');
                        int secondQuote = currentLine.indexOf('"', firstQuote + 1);
                        String tag = currentLine.substring(1, spaceOffset);
                        String value = currentLine.substring(firstQuote + 1, secondQuote);
                        header.put(tag, value);
                    }
                } else {
                    if (foundHeader) {
                        continueSearch = false;
                        break;
                    }
                }
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
        }
        return header;
    }

    public HashMap<String, String> readSingleHeader(OptimizedRandomAccessFile raf, long offset) {

        HashMap<String, String> header = new HashMap<>();

        String currentLine = "";

        boolean continueSearch = true;
        boolean foundHeader = false;

        try {
            raf.seek(offset);
            while ((currentLine = raf.readLine()) != null && continueSearch) {
                // skip comments
                if (currentLine.startsWith("%")) {
                    continue;
                }

                if (currentLine.startsWith("[")) {

                    foundHeader = true;
                    //
                    if (currentLine.length() > 4) {
                        int spaceOffset = currentLine.indexOf(' ');
                        int firstQuote = currentLine.indexOf('"');
                        int secondQuote = currentLine.indexOf('"', firstQuote + 1);
                        String tag = currentLine.substring(1, spaceOffset);
                        String value = currentLine.substring(firstQuote + 1, secondQuote);
                        String valueUtf8 = new String(value.getBytes("ISO-8859-1"), "UTF-8");
                        header.put(tag, valueUtf8);
                    }
                } else {
                    if (foundHeader) {
                        continueSearch = false;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return header;
    }

    /*
    public PgnEntry2 readSingleHeader2(OptimizedRandomAccessFile raf, long offset) {

        PgnEntry2 entry = new PgnEntry2();

        String currentLine = "";

        boolean continueSearch = true;
        boolean foundHeader = false;

        try {
            raf.seek(offset);
            while ((currentLine = raf.readLine()) != null && continueSearch) {
                // skip comments
                if (currentLine.startsWith("%")) {
                    continue;
                }

                if (currentLine.startsWith("[")) {

                    foundHeader = true;
                    //
                    if (currentLine.length() > 4) {
                        int spaceOffset = currentLine.indexOf(' ');
                        int firstQuote = currentLine.indexOf('"');
                        int secondQuote = currentLine.indexOf('"', firstQuote + 1);
                        String tag = currentLine.substring(1, spaceOffset);
                        String value = currentLine.substring(firstQuote + 1, secondQuote);
                        //header.put(tag, value);
                        if (tag.equals("Event")) {
                            entry.event = value;
                        }
                        if (tag.equals("Site")) {
                            entry.site = value;
                        }
                        if (tag.equals("Date")) {
                            entry.date = value;
                        }
                        if (tag.equals("Round")) {
                            entry.round = value;
                        }
                        if (tag.equals("White")) {
                            entry.white = value;
                        }
                        if (tag.equals("Black")) {
                            entry.black = value;
                        }
                        if (tag.equals("Result")) {
                            entry.result = value;
                        }
                        if (tag.equals("ECO")) {
                            entry.eco = value;
                        }

                    }
                } else {
                    if (foundHeader) {
                        continueSearch = false;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entry;
    }

    public PgnEntry2 readSingleHeader3(FileInputStream fis, long offset) {

        PgnEntry2 entry = new PgnEntry2();

        String currentLine = "";

        boolean continueSearch = true;
        boolean foundHeader = false;

        //InputStreamReader isr = null;
        //BufferedReader br = null;

        try {
            fis.getChannel().position(0);
            fis.skip(offset);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            while ((currentLine = br.readLine()) != null && continueSearch) {
                // skip comments
                if (currentLine.startsWith("%")) {
                    continue;
                }

                if (currentLine.startsWith("[")) {

                    foundHeader = true;
                    //
                    if (currentLine.length() > 4) {
                        int spaceOffset = currentLine.indexOf(' ');
                        int firstQuote = currentLine.indexOf('"');
                        int secondQuote = currentLine.indexOf('"', firstQuote + 1);
                        String tag = currentLine.substring(1, spaceOffset);
                        String value = currentLine.substring(firstQuote + 1, secondQuote);
                        //System.out.println(tag+ " "+value);
                        //header.put(tag, value);
                        if (tag.equals("Event")) {
                            entry.event = value;
                        }
                        if (tag.equals("Site")) {
                            entry.site = value;
                        }
                        if (tag.equals("Date")) {
                            entry.date = value;
                        }
                        if (tag.equals("Round")) {
                            entry.round = value;
                        }
                        if (tag.equals("White")) {
                            entry.white = value;
                        }
                        if (tag.equals("Black")) {
                            entry.black = value;
                        }
                        if (tag.equals("Result")) {
                            entry.result = value;
                        }
                        if (tag.equals("ECO")) {
                            entry.eco = value;
                        }

                    }
                } else {
                    if (foundHeader) {
                        continueSearch = false;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entry;
    }

    public HashMap<String, String> readSingleHeader4(FileInputStream fis, long offset) {

        HashMap<String, String> entry = new HashMap<>();

        String currentLine = "";

        boolean continueSearch = true;
        boolean foundHeader = false;

        //InputStreamReader isr = null;
        //BufferedReader br = null;

        try {
            fis.getChannel().position(0);
            fis.skip(offset);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            while ((currentLine = br.readLine()) != null && continueSearch) {
                // skip comments
                if (currentLine.startsWith("%")) {
                    continue;
                }

                if (currentLine.startsWith("[")) {

                    foundHeader = true;
                    //
                    if (currentLine.length() > 4) {
                        int spaceOffset = currentLine.indexOf(' ');
                        int firstQuote = currentLine.indexOf('"');
                        int secondQuote = currentLine.indexOf('"', firstQuote + 1);
                        String tag = currentLine.substring(1, spaceOffset);
                        //System.out.println(tag);
                        String value = currentLine.substring(firstQuote + 1, secondQuote);
                        entry.put(tag, value);
                    }
                } else {
                    if (foundHeader) {
                        continueSearch = false;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entry;
    }

     */

    private boolean isCol(char c) {
        if(c >= 'a' && c <= 'h') {
            return true;
        } else {
            return false;
        }
    }

    private boolean isRow(char c) {
        int row = Character.getNumericValue(c) - 1;
        if(row >= 0 && row <= 7) {
            return true;
        } else {
            return false;
        }
    }

    private void addMove(Move m) {

        GameNode next = new GameNode();

        Board currentBoard = this.currentNode.getBoard();
        //System.out.println(currentBoard.toString());
        //System.out.println("adding: " + m.getUci());

        Board childBoard = currentBoard.makeCopy();
        childBoard.apply(m);
        next.setMove(m);
        next.setBoard(childBoard);
        next.setParent(this.currentNode);
        this.currentNode.addVariation(next);

        this.currentNode = next;
    }

    private boolean parsePawnMove() {

        //System.out.println("PAWN MOVE "+currentLine.substring(currentIdx, currentIdx+3));
        int col = Board.alphaToPos(Character.toUpperCase(currentLine.charAt(currentIdx)));
        //System.out.println(Character.toUpperCase(currentLine.charAt(currentIdx)));
        //System.out.println("columN: "+col);
        Board board = currentNode.getBoard();
        if(currentIdx +1 < currentLine.length()) {
            if(currentLine.charAt(currentIdx+1) == 'x') {
                //System.out.println("11111");
                // after x, next one must be letter denoting column
                // and then digit representing row, like exd4 (white)
                // then parse d, 4, and check wether there is a pawn
                // on e(4-1) = e3
                if(currentIdx+3 < currentLine.length()) {
                    //System.out.println("22222222");
                    if(this.isCol(currentLine.charAt(currentIdx+2))
                            && this.isRow(currentLine.charAt(currentIdx+3)))
                    {
                        int col_to = Board.alphaToPos(Character.toUpperCase(currentLine.charAt(currentIdx+2)));
                        int row_to = Character.getNumericValue(currentLine.charAt(currentIdx+3)) - 1;
                        int row_from = -1;
                        //System.out.println("6666666666");
                        //System.out.println("colto: "+col_to);
                        //System.out.println("rowto: "+row_to);
                        if(board.turn == CONSTANTS.WHITE && row_to - 1 >= 0
                                && board.isPieceAt(col, row_to - 1)
                            && board.getPieceAt(col, row_to - 1) == CONSTANTS.WHITE_PAWN) {
                            row_from = row_to - 1;
                        } else if(board.turn == CONSTANTS.BLACK && row_to + 1 <= 7
                                && board.isPieceAt(col, row_to + 1)
                                && board.getPieceAt(col, row_to + 1) == CONSTANTS.BLACK_PAWN) {
                            row_from = row_to + 1;
                        }
                        if(row_from >= 0 && row_from <= 7) {
                            //System.out.println("555555");
                            // check wether this is a promotion, i.e. exd8=Q
                            if(currentIdx+5 < currentLine.length() && currentLine.charAt(currentIdx+4) == '=' &&
                                    (currentLine.charAt(currentIdx+5) == 'R' ||
                                    currentLine.charAt(currentIdx+5) == 'B' ||
                                    currentLine.charAt(currentIdx+5) == 'N' ||
                                    currentLine.charAt(currentIdx+5) == 'Q')) {
                                Move m = new Move(col, row_from, col_to, row_to, currentLine.charAt(currentIdx+5));
                                this.addMove(m);
                                currentIdx += 6;
                                return true;
                            } else { // just a normal move, like exd4
                                //System.out.println("333333");
                                Move m = new Move(col, row_from, col_to, row_to);
                                //System.out.println("calculated pawn: "+m.getUci());
                                this.addMove(m);
                                currentIdx += 4;
                                return true;
                            }
                        } else {
                            currentIdx += 4;
                            return false;
                        }
                    } else {
                        currentIdx += 4;
                        return false;
                    }
                } else {
                    currentIdx += 2;
                    return false;
                }
            } else { // only other case: must be a number
                if(this.isRow(currentLine.charAt(currentIdx+1))) {
                    int row = Character.getNumericValue(currentLine.charAt(currentIdx+1)) - 1;
                    //System.out.println("ROW   "+row);
                    int from_row = -1;
                    if(board.turn == CONSTANTS.WHITE) {
                        for(int row_i = row - 1;row_i>= 1;row_i--) {
                            if(board.isPieceAt(col, row_i) && board.getPieceAt(col,row_i) == CONSTANTS.WHITE_PAWN) {
                                from_row = row_i;
                                break;
                            }
                        }
                    } else {
                        for(int row_i = row + 1;row_i<= 7;row_i++) {
                            if(board.isPieceAt(col, row_i) && board.getPieceAt(col,row_i) == CONSTANTS.BLACK_PAWN) {
                                from_row = row_i;
                                break;
                            }
                        }
                    }
                    //System.out.println("FROM ROW "+from_row);
                    if(from_row >= 0) { // means we found a from square
                        // check wether this is a promotion
                        if(currentIdx+3 < currentLine.length() && currentLine.charAt(currentIdx+2) == '=' &&
                                (currentLine.charAt(currentIdx+3) == 'R' ||
                                currentLine.charAt(currentIdx+3) == 'B' ||
                                currentLine.charAt(currentIdx+3) == 'N' ||
                                currentLine.charAt(currentIdx+3) == 'Q')) {
                            Move m = new Move(col, from_row, col, row, currentLine.charAt(currentIdx+3));
                            //System.out.println("MOVE UCI "+m.getUci());
                            this.addMove(m);
                            currentIdx += 4;
                            return true;
                        } else { // not a promotion, just a standard pawn move
                            Move m = new Move(col, from_row, col, row);
                            this.addMove(m);
                            currentIdx += 2;
                            return true;
                        }
                    } else {
                        currentIdx+=2;
                        return false;
                    }
                } else {
                    currentIdx+=2;
                    return false;
                }
            }
        }
        currentIdx += 2;
        return true;
    }

    private boolean createPieceMove(int pieceType, int to_col, int to_row) {

        Board board = currentNode.getBoard();
        int to_internal = board.xyToInternal(to_col, to_row);
        ArrayList<Move> pseudos = board.pseudoLegalMoves(CONSTANTS.ANY_SQUARE, to_internal, pieceType, false, board.turn);
        if (pseudos.size() == 1) {
            Move m = pseudos.get(0);
            this.addMove(m);
            return true;
        } else {
            ArrayList<Move> legals = board.legalsFromPseudos(pseudos);
            if (legals.size() == 1) {
                Move m = legals.get(0);
                this.addMove(m);
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean createPieceMove(int pieceType, int to_col, int to_row, char qc_from_col) {

        Board board = currentNode.getBoard();
        int from_col = Board.alphaToPos(Character.toUpperCase(qc_from_col));
        int to_internal = board.xyToInternal(to_col, to_row);
        ArrayList<Move> pseudos = board.pseudoLegalMoves(CONSTANTS.ANY_SQUARE, to_internal, pieceType, false, board.turn);
        ArrayList<Move> filter = new ArrayList<>();
        for(int i=0;i<pseudos.size();i++) {
            Move m = pseudos.get(i);
            if((m.from % 10) - 1 == from_col) {
                filter.add(m);
            }
        }
        if(filter.size() == 1) {
            Move m = filter.get(0);
            this.addMove(m);
            return true;
        } else {
            ArrayList<Move> legals = board.legalsFromPseudos(pseudos);
            if(legals.size() == 1) {
                Move m = legals.get(0);
                this.addMove(m);
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean createPieceMove(int pieceType, int to_col, int to_row, int from_row) {

        Board board = currentNode.getBoard();
        int to_internal = board.xyToInternal(to_col, to_row);
        ArrayList<Move> pseudos = board.pseudoLegalMoves(CONSTANTS.ANY_SQUARE, to_internal, pieceType, false, board.turn);
        ArrayList<Move> filter = new ArrayList<>();
        for(int i=0;i<pseudos.size();i++) {
            Move m = pseudos.get(i);
            if((m.from / 10) - 2 == from_row) {
                filter.add(m);
            }
        }
        if(filter.size() == 1) {
            Move m = filter.get(0);
            this.addMove(m);
            return true;
        } else {
            ArrayList<Move> legals = board.legalsFromPseudos(pseudos);
            if(legals.size() == 1) {
                Move m = legals.get(0);
                this.addMove(m);
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean parsePieceMove(int pieceType) {
        //System.out.println("parse piece move");
        //if(currentIdx + 4 < currentLine.length()) {
        //    System.out.println(currentLine.substring(currentIdx, currentIdx+4));
        //}

        // we have a piece move like "Qxe4" where index points to Q
        // First move idx after piece symbol, i.e. to ">x<e4"
        currentIdx+=1;
        if(currentIdx < currentLine.length() && currentLine.charAt(currentIdx) == 'x') {
            currentIdx+=1;
        }
        if(currentIdx < currentLine.length()) {
            if(this.isCol(currentLine.charAt(currentIdx))) {
                //System.out.println("is col true");
                //Qe? or Qxe?, now either digit must follow (Qe4 / Qxe4)
                //or we have a disambiguition (Qee5, Qexe5)
                //System.out.println("111");
                if(currentIdx+1 < currentLine.length()) {
                    //System.out.println("2222222222");
                    //System.out.println(currentLine.charAt(currentIdx+1));
                    if(this.isRow(currentLine.charAt(currentIdx+1))) {
                        //System.out.println("222");
                        //System.out.println(currentLine.substring(currentIdx));
                        int to_col = Board.alphaToPos(Character.toUpperCase(currentLine.charAt(currentIdx)));
                        int to_row = Character.getNumericValue(currentLine.charAt(currentIdx+1)) - 1;
                        currentIdx+=2;
                        //System.out.println("33333333333");
                        // standard move, i.e. Qe4
                        return createPieceMove(pieceType, to_col, to_row);
                    } else {
                        // fix: skip x if we have Qexe5
                        int skipForTake = 0;
                        if(currentLine.charAt(currentIdx+1) == 'x' && currentIdx + 2 < currentLine.length()) {
                            //System.out.println("fix");
                            skipForTake = 1;
                            currentIdx+=1;
                        }
                        // fix end
                        if(this.isCol(currentLine.charAt(currentIdx+1))) {
                            //System.out.println("44444444444"+currentLine.charAt(currentIdx+1));
                            // we have a disambiguition, that should resolved by
                            // the column denoted in the san, here in @line[idx]
                            int to_col = Board.alphaToPos(Character.toUpperCase(currentLine.charAt(currentIdx+1)));
                            if(currentIdx+2 < currentLine.length() && this.isRow(currentLine.charAt(currentIdx+2))) {
                                //System.out.println(currentLine.charAt(currentIdx+2));
                                int to_row = Character.getNumericValue(currentLine.charAt(currentIdx+2)) - 1;
                                //System.out.println(to_col);
                                //System.out.println(to_row);
                                // move w/ disambig on col, i.e. Qee4
                                // provide line[idx] to cratePieceMove to resolve disamb.
                                currentIdx+=3;
                                return createPieceMove(pieceType, to_col, to_row, currentLine.charAt(currentIdx-(3+skipForTake)));
                            } else {
                                currentIdx+=4;
                                return false;
                            }
                        } else {
                            currentIdx+=3;
                            return false;
                        }
                    }
                } else {
                    currentIdx+=2;
                    return false;
                }
            } else {
                //System.out.println("checking else");
                //if(currentIdx+1 < currentLine.length()) {
                //    System.out.println(currentLine.charAt(currentIdx+1));
                //    System.out.println(this.isRow(currentLine.charAt(currentIdx)));
                //}
                if(currentIdx+1 < currentLine.length() && this.isRow(currentLine.charAt(currentIdx))) {
                    //System.out.println("is col!");
                    // we have a move with disamb, e.g. Q4xe5 or Q4e5
                    int from_row = Character.getNumericValue(currentLine.charAt(currentIdx))- 1;
                    if(currentLine.charAt(currentIdx+1) == 'x') {
                        currentIdx+=1;
                    }
                    if(currentIdx+2 < currentLine.length() && this.isCol(currentLine.charAt(currentIdx+1))
                            && this.isRow(currentLine.charAt(currentIdx+2)))
                    {
                        int to_col = Board.alphaToPos(Character.toUpperCase(currentLine.charAt(currentIdx+1)));
                        int to_row = Character.getNumericValue(currentLine.charAt(currentIdx+2)) - 1;
                        // parse the ambig move
                        currentIdx+=3;
                        return createPieceMove(pieceType, to_col, to_row, from_row);
                    } else {
                        currentIdx+=3;
                        return false;
                    }
                } else {
                    currentIdx+=2;
                    return false;
                }
            }
        } else {
            currentIdx+=2;
            return false;
        }
    }


    private boolean parseCastleMove() {

        int lineSize = currentLine.length();
        if(currentIdx+4 < lineSize && ( currentLine.substring(currentIdx,currentIdx+5).equals("O-O-O")
                || currentLine.substring(currentIdx,currentIdx+5).equals("0-0-0")) )
        {
            if(currentNode.getBoard().turn == CONSTANTS.WHITE) {
                Move m = new Move(CONSTANTS.E1,CONSTANTS.C1);
                this.addMove(m);
                currentIdx += 5;
                return true;
            } else {
                Move m = new Move(CONSTANTS.E8,CONSTANTS.C8);
                this.addMove(m);
                currentIdx += 5;
                return true;
            }
        }
        if(currentIdx+2 < lineSize && ( currentLine.substring(currentIdx,currentIdx+3).equals("O-O"))) {  // || line.mid(idx,3) == QString::fromLatin1("0-0"))) {
            if(currentNode.getBoard().turn == CONSTANTS.WHITE) {
                Move m = new Move(CONSTANTS.E1,CONSTANTS.G1);
                this.addMove(m);
                currentIdx += 3;
                return true;
            } else {
                Move m = new Move(CONSTANTS.E8,CONSTANTS.G8);
                this.addMove(m);
                currentIdx += 3;
                return true;
            }
        }
        currentIdx+=1;
        return false;
    }


    private void parseNAG() {

        int lineSize = currentLine.length();

        if(currentLine.charAt(currentIdx) == '$') {
            int idx_end = currentIdx;
            while(idx_end < lineSize && currentLine.charAt(idx_end) != ' ') {
                idx_end ++;
            }
            if(idx_end+1 > currentIdx) {
                boolean ok;
                try {
                    int nr = Integer.parseInt(currentLine.substring(currentIdx + 1, idx_end));
                    currentNode.addNag(nr);
                    currentIdx = idx_end;
                } catch(NumberFormatException e) {
                    currentIdx += 1;
                }
            } else {
                currentIdx += 1;
            }
            return;
        }
        if(currentIdx+1 < lineSize && currentLine.substring(currentIdx,2).equals("??")) {
            currentNode.addNag(CONSTANTS.NAG_BLUNDER);
            currentIdx += 3;
            return;
        }
        if(currentIdx+1 < lineSize && currentLine.substring(currentIdx,2).equals("!!")) {
            currentNode.addNag(CONSTANTS.NAG_BRILLIANT_MOVE);
            currentIdx += 3;
            return;
        }
        if(currentIdx+1 < lineSize && currentLine.substring(currentIdx,2).equals("!?")) {
            currentNode.addNag(CONSTANTS.NAG_SPECULATIVE_MOVE);
            currentIdx += 3;
            return;
        }
        if(currentIdx+1 < lineSize && currentLine.substring(currentIdx,2).equals("?!")) {
            currentNode.addNag(CONSTANTS.NAG_DUBIOUS_MOVE);
            currentIdx += 3;
            return;
        }
        if(currentLine.charAt(currentIdx) == '?') {
            currentNode.addNag(CONSTANTS.NAG_MISTAKE);
            currentIdx += 2;
            return;
        }
        if(currentLine.charAt(currentIdx) == '!') {
            currentNode.addNag(CONSTANTS.NAG_GOOD_MOVE);
            currentIdx += 2;
            return;
        }
    }

    private int getNetxtToken() {

        int lineSize = currentLine.length();
        while(currentIdx < lineSize) {
            char ci = currentLine.charAt(currentIdx);
            if(ci == ' ' || ci == '.') {
                currentIdx += 1;
                continue;
            }
            if(ci >= '0' && ci <= '9') {
                if(ci == '1') {
                    if(currentIdx+1 < lineSize) {
                        if(currentLine.charAt(currentIdx+1) == '-') {
                            if(currentIdx+2 < lineSize && currentLine.charAt(currentIdx+2) == '0') {
                                return CONSTANTS.TKN_RES_WHITE_WIN;
                            }
                        }
                        if(currentIdx+2 < lineSize && currentLine.charAt(currentIdx+2) == '/') {
                            if(currentIdx+6 < lineSize && currentLine.substring(currentIdx,currentIdx+6).equals("1/2-1/2")) {
                                return CONSTANTS.TKN_RES_DRAW;
                            }
                        }
                    }
                }
                // irregular castling like 0-0 or 0-0-0
                if(ci == '0') {
                    if(currentIdx+1 < lineSize && currentLine.charAt(currentIdx+1) == '-') {
                        if(currentIdx+2 < lineSize && currentLine.charAt(currentIdx+2) == '1') {
                            return CONSTANTS.TKN_RES_BLACK_WIN;
                        } else {
                            if(currentIdx+2 < lineSize && currentLine.charAt(currentIdx+2) == '0') {
                                return CONSTANTS.TKN_CASTLE;
                            }
                        }
                    }
                }
                // none of the above -> move number, just continue
                currentIdx += 1;
                continue;
            }
            if(ci >= 'a' && ci <= 'h') {
                return CONSTANTS.TKN_PAWN_MOVE;
            }
            if(ci == 'O') {
                return CONSTANTS.TKN_CASTLE;
            }
            if(ci == 'R') {
                return CONSTANTS.TKN_ROOK_MOVE;
            }
            if(ci == 'N') {
                return CONSTANTS.TKN_KNIGHT_MOVE;
            }
            if(ci == 'B') {
                return CONSTANTS.TKN_BISHOP_MOVE;
            }
            if(ci == 'Q') {
                return CONSTANTS.TKN_QUEEN_MOVE;
            }
            if(ci == 'K') {
                return CONSTANTS.TKN_KING_MOVE;
            }
            if(ci == '+') {
                return CONSTANTS.TKN_CHECK;
            }
            if(ci == '(') {
                return CONSTANTS.TKN_OPEN_VARIATION;
            }
            if(ci == ')') {
                return CONSTANTS.TKN_CLOSE_VARIATION;
            }
            if(ci == '$' || ci == '!' || ci == '?') {
                return CONSTANTS.TKN_NAG;
            }
            if(ci == '{') {
                return CONSTANTS.TKN_OPEN_COMMENT;
            }
            if(ci == '*') {
                return CONSTANTS.TKN_RES_UNDEFINED;
            }
            if(ci == '-') {
                if(currentIdx + 1 < lineSize && currentLine.charAt(currentIdx+1) == '-') {
                    return CONSTANTS.TKN_NULL_MOVE;
                }
            }
            // if none of the above match, try to continue until we
            // find another usable token
            currentIdx += 1;
        }
        return CONSTANTS.TKN_EOL;
    }

    public Game readGame(OptimizedRandomAccessFile raf) {

        currentLine = "";
        currentIdx = 0;

        String startingFen = "";

        Game g = new Game();

        gameStack.clear();
        gameStack.push(g.getRootNode());
        currentNode = g.getRootNode();
        Board rootBoard = new Board(true);
        currentNode.setBoard(rootBoard);

        currentLine = null;

        try {
            while ((currentLine = raf.readLine()) != null) {
                if (currentLine.startsWith("%") || currentLine.isEmpty()) {
                    continue;
                }

                if (currentLine.startsWith("[")) {
                    if (currentLine.length() > 4) {
                        int spaceOffset = currentLine.indexOf(' ');
                        int firstQuote = currentLine.indexOf('"');
                        int secondQuote = currentLine.indexOf('"', firstQuote + 1);
                        String tag = currentLine.substring(1, spaceOffset);
                        String value = currentLine.substring(firstQuote + 1, secondQuote);
                        //System.out.println(tag);
                        if (tag.equals("FEN")) {
                            startingFen = value;
                        } else {
                            g.setHeader(tag, value);
                        }
                    }
                    continue;
                } else {
                    //System.out.println("break: "+currentLine);
                    break; // finished reading header
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return g;
        }
        // now the actual game should start.
        // try to set the starting fen, if it exists
        //System.out.println("starting fen: "+startingFen);
        if (!startingFen.isEmpty()) {
            Board boardFen = new Board(startingFen);
            if (!boardFen.isConsistent()) {
                return g;
            } else {
                currentNode.setBoard(boardFen);
            }
        }

        // we should now have a header, seek first non-empty line
        // if we already reached it, just skip this part
        if(currentLine.trim().isEmpty()) {
            try {
                while (true) {
                    currentLine = raf.readLine();
                    //System.out.println(currentLine);
                    if (currentLine == null) { //reached eof
                        return g;
                    }
                    if (currentLine.trim().isEmpty()) {
                        continue;
                    } else {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                return g;
            }
        }

        boolean firstLine = true;

        //System.out.println("Starting with: "+currentLine);

        try {
            while (true) {
                // if we are at the first line after skipping
                // all the empty ones, don't read another line
                // otherwise, call readLine
                if (!firstLine) {
                    currentLine = raf.readLine();
                } else {
                    firstLine = false;
                }
                if (currentLine == null || currentLine.isEmpty()) {
                    return g;
                }
                if (currentLine.startsWith("%")) {
                    continue;
                }

                currentIdx = 0;
                while (currentIdx < currentLine.length()) {
                    //if(currentIdx + 3 < currentLine.length()) {
                    //    System.out.println("TOKEN: " + currentLine.substring(currentIdx, currentIdx+4));
                        //System.out.println("current node is null: " + (currentNode == null));
                    //    System.out.println(currentNode.getBoard().toString());
                    //}
                    int tkn = getNetxtToken();
                    if (tkn == CONSTANTS.TKN_EOL) {
                        break;
                    }
                    if (tkn == CONSTANTS.TKN_RES_WHITE_WIN) {
                        // 1-0
                        g.setResult(CONSTANTS.RES_WHITE_WINS);
                        currentIdx += 4;
                    }
                    if (tkn == CONSTANTS.TKN_RES_BLACK_WIN) {
                        // 0-1
                        g.setResult(CONSTANTS.RES_BLACK_WINS);
                        currentIdx += 4;
                    }
                    if (tkn == CONSTANTS.TKN_RES_UNDEFINED) {
                        // *
                        g.setResult(CONSTANTS.RES_UNDEF);
                        currentIdx += 2;
                    }
                    if (tkn == CONSTANTS.TKN_RES_DRAW) {
                        // 1/2-1/2
                        g.setResult(CONSTANTS.RES_DRAW);
                        currentIdx += 8;
                    }
                    if (tkn == CONSTANTS.TKN_PAWN_MOVE) {
                        //System.out.println("pawn move");
                        parsePawnMove();
                    }
                    if (tkn == CONSTANTS.TKN_CASTLE) {
                        //System.out.println("token castle");
                        //System.out.println("check: "+currentLine.substring(currentIdx, currentIdx+4));
                        parseCastleMove();
                    }
                    if (tkn == CONSTANTS.TKN_ROOK_MOVE) {
                        parsePieceMove(CONSTANTS.ROOK);
                    }
                    if (tkn == CONSTANTS.TKN_KNIGHT_MOVE) {
                        parsePieceMove(CONSTANTS.KNIGHT);
                    }
                    if (tkn == CONSTANTS.TKN_BISHOP_MOVE) {
                        parsePieceMove(CONSTANTS.BISHOP);
                    }
                    if (tkn == CONSTANTS.TKN_QUEEN_MOVE) {
                        //System.out.println("Queen move");
                        parsePieceMove(CONSTANTS.QUEEN);
                    }
                    if (tkn == CONSTANTS.TKN_KING_MOVE) {
                        parsePieceMove(CONSTANTS.KING);
                    }
                    if (tkn == CONSTANTS.TKN_CHECK) {
                        currentIdx += 1;
                    }
                    if (tkn == CONSTANTS.TKN_NULL_MOVE) {
                        Move m = new Move();
                        m.isNullMove = true;
                        addMove(m);
                        currentIdx += 2;
                    }
                    if (tkn == CONSTANTS.TKN_OPEN_VARIATION) {
                        // put current node on stack, so that we don't forget it.
                        // however if we are at the root node, something
                        // is wrong in the PGN. Silently ignore "(" then
                        if(currentNode != g.getRootNode()) {
                            gameStack.push(currentNode);
                            currentNode = currentNode.getParent();
                        }
                        currentIdx += 1;
                    }
                    if (tkn == CONSTANTS.TKN_CLOSE_VARIATION) {
                        // pop from stack. but always leave root
                        if (gameStack.size() > 1) {
                            currentNode = gameStack.pop();
                        }
                        currentIdx += 1;
                    }
                    if (tkn == CONSTANTS.TKN_NAG) {
                        parseNAG();
                    }
                    if (tkn == CONSTANTS.TKN_OPEN_COMMENT) {
                        //String rest_of_line = currentLine.substring(currentIdx + 1, currentLine.length() - (currentIdx + 1));
                        String rest_of_line = currentLine.substring(currentIdx + 1, currentLine.length());
                        //System.out.println(rest_of_line);
                        int end = rest_of_line.indexOf("}");
                        //System.out.println(end);
                        if (end >= 0) {
                            String comment_line = rest_of_line.substring(0, end+1);
                            currentNode.setComment(comment_line);
                            currentIdx = currentIdx + end + 1;
                        } else {
                            // get comment over multiple lines
                            StringBuilder comment_lines = new StringBuilder();
                            //String comment_line = currentLine.substring(currentIdx + 1, currentLine.length() - (currentIdx + 1));
                            String comment_line = currentLine.substring(currentIdx + 1);
                            comment_lines.append(comment_line+"\n");
                            // we already have the comment part of the current line,
                            // so read-in the next line, and then loop until we find
                            // the end marker "}"
                            //currentLine = raf.readLine();
                            int linesRead = 0;
                            int end_index = -1;
                            while (linesRead < 500) { // what if we never find } ??? -> stop after 500 lines
                                currentLine = raf.readLine();
                                linesRead += 1;
                                if (currentLine.contains("}")) {
                                    end_index = currentLine.indexOf("}");
                                    break;
                                } else {
                                    comment_lines.append(currentLine+"\n");
                                }
                            }
                            if (end_index >= 0) {
                                comment_lines.append(currentLine, 0, end_index);
                                comment_lines.append("\n");
                                currentIdx = end_index + 1;
                            }
                            currentNode.setComment(comment_lines.toString());
                        }
                    }
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return g;
    }

}

    /*


    int PgnReader::readGame(QTextStream &in, qint64 offset, chess::Game *g) {


        if(offset != 0 && offset > 0) {
            in.seek(offset);
        }
        return this->readGame(in, g);
    }
    */

