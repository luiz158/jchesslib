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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Game extends JsonSerializable.Base {

    private GameNode root = null;
    private GameNode current = null;
    private int result;
    private boolean treeWasChanged;
    private HashMap<String, String> infoStrings;
    private HashMap<String, Integer> infoNumbers;

    public Game() {
        this.root = new GameNode();
        this.result = CONSTANTS.RES_UNDEF;
        this.current = this.root;
        this.infoStrings = new HashMap<String,String>();
        this.infoNumbers = new HashMap<String, Integer>();
    }

    private boolean containsPositionRec(long positionHash, GameNode node, int maxHalfmove) {
        if(maxHalfmove <= node.getBoard().halfmoveClock) {
            return false;
        }
        if(node.getBoard().getPositionHash() == positionHash) {
            return true;
        } else {
            for(GameNode var_i : node.getVariations()) {
                boolean hasPosition = containsPositionRec(positionHash, var_i, maxHalfmove);
                if(hasPosition) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsPosition(long positionHash, int minHalfmove, int maxHalfmove) {
        GameNode current = this.getRootNode();
        for(int i=0;i<minHalfmove-1;i++) {
            if(current.hasChild()) {
                current = current.getVariation(0);
            } else {
                return false;
            }
        }
        return containsPositionRec(positionHash, current, maxHalfmove);
    }

    private GameNode findNodeByIdRec(int id, GameNode node) {
        if(node.getId() == id) {
            return node;
        } else {
            for(GameNode var_i : node.getVariations()) {
                GameNode result = this.findNodeByIdRec(id, var_i);
                if(result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public GameNode findNodeById(int id) {
        GameNode current = this.getRootNode();
        GameNode result = this.findNodeByIdRec(id, current);
        if(result == null) {
            throw new IllegalArgumentException("node with id "+id+" doesn't exist!");
        } else {
            return result;
        }
    }

    private ArrayList<Integer> getAllIds(GameNode node) {

        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(node.getId());
        for(GameNode nodeI : node.getVariations()) {
            ids.addAll(getAllIds(nodeI));
        }
        return ids;
    }

    public ArrayList<Integer> getAllIds() {
        return getAllIds(getRootNode());
    }

    public void setStringHeader(String tag, String value) {
        this.infoStrings.put(tag, value);
    }

    public String getStringHeader(String tag) {
        String value = this.infoStrings.get(tag);
        if(value == null) {
            return "";
        } else {
            return value;
        }
    }

    public void setNumberHeader(String tag, int value) {
        this.infoNumbers.put(tag, value);
    }

    public int getNumberHeader(String tag) {
        Integer value = this.infoNumbers.get(tag);
        if(value == null) {
            return -1;
        } else {
            return value;
        }
    }

    public void resetHeaders() {

        this.infoStrings = new HashMap<>();
        this.infoNumbers = new HashMap<>();
    }

    public ArrayList<String> getStringTags() {
        ArrayList<String> tags = new ArrayList<String>();
        tags.addAll(this.infoStrings.keySet());
        return tags;
    }

    public ArrayList<String> getNumberTags() {
        ArrayList<String> tags = new ArrayList<String>();
        tags.addAll(this.infoNumbers.keySet());
        return tags;
    }

    public HashMap<String,String> getInfoStrings() {
        return infoStrings;
    }

    public void setInfoStrings(HashMap<String,String> infoStrings) {
        this.infoStrings = infoStrings;
    }

    public GameNode getRootNode() {
        return this.root;
    }

    public GameNode getEndNode() {
        GameNode temp = this.getRootNode();
        while(temp.getVariations().size() > 0) {
            temp = temp.getVariation(0);
        }
        return temp;
    }

    public GameNode getCurrentNode() {
        return this.current;
    }

    public int getResult() {
        return this.result;
    }

    public void setResult(int r) {
        this.result = r;
    }

    public void applyMove(Move m) {
        boolean existsChild = false;
        for(GameNode var_i : this.current.getVariations()) {
            Move mi = var_i.getMove();
            if(m.from == mi.from && m.to == mi.to && m.promotionPiece == mi.promotionPiece) {
                existsChild = true;
                this.current = var_i;
                break;
            }
        }
        if(!existsChild) {
            GameNode current = this.getCurrentNode();
            Board bCurrent = current.getBoard();
            Board bChild = bCurrent.makeCopy();
            bChild.apply(m);
            GameNode newCurrent = new GameNode();
            newCurrent.setBoard(bChild);
            newCurrent.setMove(m);
            newCurrent.setParent(current);
            current.getVariations().add(newCurrent);
            this.current = newCurrent;
            this.treeWasChanged = true;
        }
    }

    public void setCurrent(GameNode newCurrent) {
        this.current = newCurrent;
    }

    public void setRoot(GameNode newRoot) {
        this.root = newRoot;
    }

    public void goToMainLineChild() {
        if(this.current.getVariations().size() > 0) {
            this.current = this.current.getVariation(0);
        }
    }

    public void goToChild(int idxChild) {
        if(this.current.getVariations().size() > idxChild) {
            this.current = this.current.getVariation(idxChild);
        }
    }

    public void goToParent() {
        if(this.current.getParent() != null) {
            this.current = this.current.getParent();
        }
    }

    public void goToRoot() {
        this.current = this.root;
    }

    public void goToEnd() {
        GameNode temp = this.root;
        while(temp.getVariations().size() > 0) {
            temp = temp.getVariation(0);
        }
        this.current = temp;
    }

    public void moveUp(GameNode node) {
        if(node.getParent() != null) {
            GameNode parent = node.getParent();
            int i = parent.getVariations().indexOf(node);
            if (i > 0) {
                parent.getVariations().remove(i);
                parent.getVariations().add(i - 1, node);
            }
            this.treeWasChanged = true;
        }
    }

    public void moveDown(GameNode node) {
        if(node.getParent() != null) {
            GameNode parent = node.getParent();
            int i = parent.getVariations().indexOf(node);
            if(i < parent.getVariations().size() -1) {
                parent.getVariations().remove(i);
                parent.getVariations().add(i+1,node);
            }
            this.treeWasChanged = true;
        }
    }

    public void delVariant(GameNode node) {
        // go up the variation until we
        // find the root of the variation
        GameNode child = node;
        GameNode variationRoot = node;
        while (variationRoot.getParent() != null && variationRoot.getParent().getVariations().size() == 1) {
            child = variationRoot;
            variationRoot = variationRoot.getParent();
        }
        int idx = -1;
        // one more to get the actual root
        if (variationRoot.getParent() != null) {
            child = variationRoot;
            variationRoot = variationRoot.getParent();
            idx = variationRoot.getVariations().indexOf(child);
        }
        if (idx != -1) {
            variationRoot.getVariations().remove(idx);
            this.current = variationRoot;
        }
    }

    public void delBelow(GameNode node) {
        node.getVariations().clear();
        this.current = node;
    }


    public void removeCommentRec(GameNode node) {
        node.setComment("");
        for(GameNode var_i : node.getVariations()) {
            this.removeCommentRec(var_i);
        }
    }

    public void removeAllComments() {
        this.removeCommentRec(this.getRootNode());
    }

    public void goToLeaf() {
        while(!current.isLeaf()) {
            this.goToChild(0);
        }
    }

    public void removeAllAnnotationsRec(GameNode node) {
        node.getMoveAnnotations().clear();
        node.getPositionAnnotations().clear();
        for(GameNode var_i : node.getVariations()) {
            this.removeAllAnnotationsRec(var_i);
        }
    }

    public void removeAllAnnotations() { this.removeAllAnnotationsRec(this.getRootNode()); }

    public void resetWithNewRootBoard(Board newRootBoard) {
        GameNode oldRoot = this.getRootNode();
        this.delBelow(oldRoot);
        GameNode newRoot = new GameNode();
        newRoot.setBoard(newRootBoard);
        this.setRoot(newRoot);
        this.setCurrent(newRoot);
        this.result = CONSTANTS.RES_UNDEF;
        this.clearHeaders();
        this.treeWasChanged = true;
    }

    public void removeAllVariants() {
        GameNode temp = this.getRootNode();
        int size = temp.getVariations().size();
        while(size > 0) {
            GameNode main = temp.getVariations().get(0);
            temp.getVariations().clear();
            temp.getVariations().add(main);
            temp = main;
            size = temp.getVariations().size();
        }
        this.current = this.getRootNode();
    }

    public void clearHeaders() {
        this.infoStrings.clear();
        this.infoStrings.put("Event", "");
        this.infoStrings.put("Date", "");
        this.infoStrings.put("Round", "");
        this.infoStrings.put("White", "");
        this.infoStrings.put("Black", "");
        this.infoStrings.put("Result", "*");
    }

    public int countHalfmoves() {
        int halfmoves = 0;
        GameNode temp = this.root;
        while(temp.getVariations().size() > 0) {
            temp = temp.getVariation(0);
            halfmoves += 1;
        }
        return halfmoves;
    }

    public boolean isThreefoldRepetition() {

        int counter = 1;
        long zobrist = current.getBoard().getZobrist();
        GameNode temp = this.current;
        while(temp.getParent() != null) {
            temp = temp.getParent();
            long tempZobrist = temp.getBoard().getZobrist();
            if(tempZobrist == zobrist) {
                counter++;
            }
            if(counter >= 3) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("type", "Game");
        gen.writeFieldName("info");
        gen.writeStartObject();
        for(String key : infoStrings.keySet()) {
            gen.writeStringField(key, infoStrings.get(key));
        }
        for(String key : infoNumbers.keySet()) {
            gen.writeNumberField(key, infoNumbers.get(key));
        }
        gen.writeEndObject();
        gen.writeFieldName("moves");
        gen.writeStartArray();
        gen.writeObject(root);
        gen.writeEndArray();
        gen.writeEndObject();
    }

    @Override
    public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

}
