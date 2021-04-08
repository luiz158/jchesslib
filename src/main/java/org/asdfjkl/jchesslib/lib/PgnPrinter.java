/* JerryFX - A Chess Graphical User Interface
 * Copyright (C) 2020 Dominik Klein
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.asdfjkl.jchesslib.lib;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class PgnPrinter {

    StringBuilder pgn;
    StringBuilder currentLine;
    int variationDepth;
    boolean forceMoveNumber;

    public PgnPrinter() {
        this.pgn = new StringBuilder();
        this.currentLine = new StringBuilder();
        this.variationDepth = 0;
        this.forceMoveNumber = true;
    }

    private void reset() {
        this.pgn = new StringBuilder();
        this.currentLine = new StringBuilder();
        this.variationDepth = 0;
        this.forceMoveNumber = true;
    }

    private void flushCurrentLine() {
        if(this.currentLine.length() != 0) {
            this.pgn.append(this.currentLine.toString().trim());
            this.pgn.append("\n");
            this.currentLine.setLength(0);
        }
    }

    private void writeToken(String token) {
        if(80 - this.currentLine.length() < token.length()) {
            this.flushCurrentLine();
        }
        this.currentLine.append(token);
    }

    private void writeLine(String line) {
        this.flushCurrentLine();
        this.pgn.append(line.trim()).append("\n");
    }

    private void printHeaders(Game g) {
        String tag = "[Event \"" + g.getStringHeader("event") + "\"]";
        pgn.append(tag).append("\n");
        tag = "[Site \"" + g.getStringHeader("site") + "\"]";
        pgn.append(tag).append("\n");
        String dateString = g.getStringHeader("date");
        switch(dateString.length()) {
            case 4:
                dateString += ".??.??";
                break;
            case 7:
                dateString += ".??";
        }
        if(dateString.length() > 10) {
            dateString = dateString.substring(0, 10);
        }
        tag = "[Date \"" + dateString + "\"]";
        pgn.append(tag).append("\n");
        tag = "[Round \"" + g.getNumberHeader("round") + "\"]";
        pgn.append(tag).append("\n");
        tag = "[White \"" + g.getStringHeader("white") + "\"]";
        pgn.append(tag).append("\n");
        tag = "[Black \"" + g.getStringHeader("black") + "\"]";
        pgn.append(tag).append("\n");
        tag = "[Result \"" + g.getStringHeader("result") + "\"]";
        pgn.append(tag).append("\n");
        ArrayList<String> allStringTags = g.getStringTags();
        for(String tag_i : allStringTags) {
            if(!tag_i.equals("event") && !tag_i.equals("site") && !tag_i.equals("date")
                    && !tag_i.equals("white") && !tag_i.equals("black") && !tag_i.equals("result" ))
            {
                String value_i = g.getStringHeader(tag_i);
                String tag_i_cap = tag_i.substring(0, 1).toUpperCase() + tag_i.substring(1);;
                if(tag_i.equals("eco")) {
                    tag_i_cap = "ECO";
                }
                String tag_val = "[" + tag_i_cap + " \"" + value_i + "\"]";
                pgn.append(tag_val).append("\n");
            }
        }
        ArrayList<String> allNumberTags = g.getNumberTags();
        for(String tag_i : allNumberTags) {
            if(!tag_i.equals("round" ))
            {
                int value_i = g.getNumberHeader(tag_i);
                String tag_i_cap = tag_i.substring(0, 1).toUpperCase() + tag_i.substring(1);;
                String tag_val = "[" + tag_i_cap + " \"" + value_i + "\"]";
                pgn.append(tag_val).append("\n");
            }
        }
        // add fen string tag if root is not initial position
        Board rootBoard = g.getRootNode().getBoard();
        if(!rootBoard.isInitialPosition()) {
            String tag_fen = "[FEN \"" + rootBoard.fen() + "\"]";
            pgn.append(tag_fen).append("\n");
        }
    }

    private void printMove(GameNode node) {
        Board b = node.getParent().getBoard();
        if(b.turn == CONSTANTS.WHITE) {
            String tkn = Integer.toString(b.fullmoveNumber);
            tkn += ". ";
            this.writeToken(tkn);
        }
        else if(this.forceMoveNumber) {
            String tkn = Integer.toString(b.fullmoveNumber);
            tkn += "... ";
            this.writeToken(tkn);
        }
        this.writeToken(node.getSan() + " ");
        this.forceMoveNumber = false;
    }

    private void printAnnotation(String annotation, boolean turn) {

        int token = -1;
        switch(annotation) {
            case "??":
                token = CONSTANTS.NAG_BLUNDER;
                break;
            case "?":
                token = CONSTANTS.NAG_MISTAKE;
                break;
            case "?!":
                token = CONSTANTS.NAG_DUBIOUS_MOVE;
                break;
            case "!?":
                token = CONSTANTS.NAG_SPECULATIVE_MOVE;
                break;
            case "!":
                token = CONSTANTS.NAG_GOOD_MOVE;
                break;
            case "!!":
                token = CONSTANTS.NAG_BRILLIANT_MOVE;
                break;
            case "□":
                token = CONSTANTS.NAG_FORCED_MOVE;
                break;
            case "Δ":
                token = CONSTANTS.NAG_WITH_THE_IDEA_OF;
                break;
            case "N":
                token = CONSTANTS.NAG_NOVELTY;
                break;
            case "⩲":
                token = CONSTANTS.NAG_WHITE_SLIGHT_ADVANTAGE;
                break;
            case "⩱":
                token = CONSTANTS.NAG_BLACK_SLIGHT_ADVANTAGE;
                break;
            case "±":
                token = CONSTANTS.NAG_WHITE_MODERATE_ADVANTAGE;
                break;
            case "∓":
                token = CONSTANTS.NAG_BLACK_MODERATE_ADVANTAGE;
                break;
            case "+−":
                token = CONSTANTS.NAG_WHITE_DECISIVE_ADVANTAGE;
                break;
            case "−+":
                token = CONSTANTS.NAG_BLACK_DECISIVE_ADVANTAGE;
                break;
            case "∞":
                token = CONSTANTS.NAG_UNCLEAR_POSITION;
                break;
            case "=∞":
                if(turn == CONSTANTS.WHITE) {
                    token = CONSTANTS.NAG_WHITE_HAS_COMPENSATION;
                } else {
                    token = CONSTANTS.NAG_BLACK_HAS_COMPENSATION;
                }
                break;
            case "↑":
                if(turn == CONSTANTS.WHITE) {
                    token = CONSTANTS.NAG_WHITE_HAS_INITIATIVE;
                } else {
                    token = CONSTANTS.NAG_BLACK_HAS_INITIATIVE;
                }
                break;
            case "→":
                if(turn == CONSTANTS.WHITE) {
                    token = CONSTANTS.NAG_WHITE_HAS_ATTACK;
                } else {
                    token = CONSTANTS.NAG_BLACK_HAS_ATTACK;
                }
                break;
            case "⇄":
                if(turn == CONSTANTS.WHITE) {
                    token = CONSTANTS.NAG_WHITE_MODERATE_COUNTERPLAY;
                } else {
                    token = CONSTANTS.NAG_BLACK_MODERATE_COUNTERPLAY;
                }
                break;
            case "⊕":
                if(turn == CONSTANTS.WHITE) {
                    token = CONSTANTS.NAG_WHITE_TIME_TROUBLE;
                } else {
                    token = CONSTANTS.NAG_BLACK_TIME_TROUBLE;
                }
                break;
            case "⊙":
                if(turn == CONSTANTS.WHITE) {
                    token = CONSTANTS.NAG_WHITE_ZUGZWANG;
                } else {
                    token = CONSTANTS.NAG_BLACK_ZUGZWANG;
                }
                break;
        }

        if(token >= 0) {
            String tkn = "$" + token + " ";
            this.writeToken(tkn);
        }
    }

    private void printResult(int result) {
        String res = "";
        if(result == CONSTANTS.RES_WHITE_WINS) {
            res += "1-0";
        } else if(result == CONSTANTS.RES_BLACK_WINS) {
            res += "0-1";
        } else if(result == CONSTANTS.RES_DRAW) {
            res += "1/2-1/2";
        } else {
            res += "*";
        }
        this.writeToken(res + " ");
    }

    private void beginVariation() {
        this.variationDepth++;
        String tkn = "( ";
        this.writeToken(tkn);
        this.forceMoveNumber = true;
    }

    private void endVariation() {
        this.variationDepth--;
        String tkn = ") ";
        this.writeToken(tkn);
        this.forceMoveNumber = true;
    }

    private void printComment(String comment) {
        String write = "{ " + comment.replace("}","").trim() + " } ";
        this.writeToken(write);
        //this->forceMoveNumber = false;
    }

    private void printGameContent(GameNode g) {

        Board b = g.getBoard();

        // first write mainline move, if there are variations
        int cntVar = g.getVariations().size();
        if(cntVar > 0) {
            GameNode mainVariation = g.getVariation(0);
            this.printMove(mainVariation);
            // write nags
            for(String moveAnnotation : mainVariation.getMoveAnnotations()) {
                this.printAnnotation(moveAnnotation, b.turn);
            }
            for(String positionAnnotation : mainVariation.getPositionAnnotations()) {
                this.printAnnotation(positionAnnotation, b.turn);
            }
            // write comments
            if(!mainVariation.getComment().isEmpty()) {
                this.printComment(mainVariation.getComment());
            }
        }

        // now handle all variations (sidelines)
        for(int i=1;i<cntVar;i++) {
            // first create variation start marker, and print the move
            GameNode var_i = g.getVariation(i);
            this.beginVariation();
            this.printMove(var_i);
            // next print nags
            for(String moveAnnotation : var_i.getMoveAnnotations()) {
                this.printAnnotation(moveAnnotation, b.turn);
            }
            for(String positionAnnotation : var_i.getPositionAnnotations()) {
                this.printAnnotation(positionAnnotation, b.turn);
            }
            // finally print comments
            if(!var_i.getComment().isEmpty()) {
                this.printComment(var_i.getComment());
            }

            // recursive call for all children
            this.printGameContent(var_i);

            // print variation end
            this.endVariation();
        }

        // finally do the mainline
        if(cntVar > 0) {
            GameNode mainVariation = g.getVariation(0);
            this.printGameContent(mainVariation);
        }
    }

    public String printGame(Game g) {

        this.reset();

        this.printHeaders(g);

        this.writeLine("");
        GameNode root = g.getRootNode();

        // special case if the root node has
        // a comment before the actual game starts
        if(!root.getComment().isEmpty()) {
            this.printComment(root.getComment());
        }

        this.printGameContent(root);
        this.printResult(g.getResult());
        this.pgn.append(this.currentLine.toString());

        return this.pgn.toString();
    }

    public void writeGame(Game g, String filename) {

        BufferedWriter out = null;
        String pgn = this.printGame(g);
        try {
            out = Files.newBufferedWriter(Path.of(filename));
            out.write(pgn);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
             if(out != null) {
                 try {
                     out.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
        }
    }

}

