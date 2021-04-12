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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializable;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import jdk.jshell.Diag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class GameNode {

    static int id;
    private final int nodeId;
    private Board board = null;
    private Move move = null; // move leading to this node
    private GameNode parent = null;

    private String sanCache;
    private final ArrayList<GameNode> variations;

    private String comment;
    private int clock = -1;
    private ArrayList<ColoredField> coloredFields;
    private ArrayList<Arrow> arrows;
    private ArrayList<Attachment> attachments;
    private ArrayList<Diagram> diagrams;
    private ArrayList<Evaluation> evaluations;
    private ArrayList<String> moveAnnotations;
    private ArrayList<String> positionAnnotations;
    private int egt = -1;
    private int emt = -1;

    private String circlesColor = "";
    private String arrowsColor = "";


    protected static int initId() {
        return id++;
    }

    public GameNode() {
        this.nodeId = initId();
        this.variations = new ArrayList<GameNode>();
        //this.board = new Board(true);
        //this.board.resetToStartingPosition();
        this.comment = "";
        this.sanCache = "";
    }

    public void addOrRemoveArrow(Arrow arrow) {
        if(arrows == null) {
            arrows = new ArrayList<Arrow>();
            arrows.add(arrow);
        } else {
            if(!arrows.contains(arrow)) {
                arrows.add(arrow);
            } else {
                int idx = arrows.indexOf(arrow);
                arrows.remove(idx);
            }
        }
    }

    public ArrayList<Arrow> getArrows() {
        if(arrows == null) {
            arrows = new ArrayList<Arrow>();
        }
        return arrows;
    }

    public void addOrRemoveColoredField(ColoredField coloredField) {
        if(coloredFields == null) {
            coloredFields = new ArrayList<ColoredField>();
            coloredFields.add(coloredField);
        } else {
            if(!coloredFields.contains(coloredField)) {
                coloredFields.add(coloredField);
            } else {
                int idx = coloredFields.indexOf(coloredField);
                coloredFields.remove(idx);
            }
        }
    }

    public ArrayList<ColoredField> getColoredFields() {
        if(coloredFields == null) {
            coloredFields = new ArrayList<ColoredField>();
        }
        return coloredFields;
    }

    public void addAttachment(Attachment a) {

        if(attachments == null) {
            attachments = new ArrayList<>();
        }
        if(!attachments.contains(a)) {
            attachments.add(a);
        }
    }

    public ArrayList<Attachment> getAttachments() {
        if(attachments == null) {
            attachments = new ArrayList<>();
        }
        return attachments;
    }

    public void addDiagram(Diagram d) {
        if(diagrams == null) {
            diagrams = new ArrayList<>();
        }
        if(!diagrams.contains(d)) {
            diagrams.add(d);
        }
    }

    public ArrayList<Diagram> getDiagrams() {
        if(diagrams == null) {
            diagrams = new ArrayList<>();
        }
        return diagrams;
    }

    public void addEvaluation(Evaluation e) {
        if (evaluations == null) {
            evaluations = new ArrayList<>();
        }
        if(!evaluations.contains(e)) {
            evaluations.add(e);
        }
    }

    public ArrayList<Evaluation> getEvaluations() {
        if(evaluations == null) {
            evaluations = new ArrayList<>();
        }
        return evaluations;
    }

    public void addMoveAnnotation(String a) {
        if(moveAnnotations == null) {
            moveAnnotations = new ArrayList<>();
        }
        if(!moveAnnotations.contains(a)) {
            moveAnnotations.add(a);
        }
    }

    public ArrayList<String> getMoveAnnotations() {
        if(moveAnnotations == null) {
            moveAnnotations = new ArrayList<>();
        }
        return moveAnnotations;
    }

    public void addPositionAnnotation(String a) {
        if(positionAnnotations == null) {
            positionAnnotations = new ArrayList<>();
        }
        if(!positionAnnotations.contains(a)) {
            positionAnnotations.add(a);
        }
    }

    public ArrayList<String> getPositionAnnotations() {
        if(positionAnnotations == null) {
            positionAnnotations = new ArrayList<>();
        }
        return positionAnnotations;
    }

    public void setClock(int clock) { this.clock = clock; }

    public int getClock() { return clock; }

    public void setEgt(int egt) { this.egt = egt; }

    public int getEgt() { return egt; }

    public void setEmt(int emt) { this.emt = emt; }

    public int getEmt() { return emt; }

    public int getId() {
        return this.nodeId;
    }

    public Board getBoard() {
        return this.board;
    }

    public void setBoard(Board b) {
        this.board = b;
    }

    public String getSan(Move m) {
        return this.board.san(m);
    }

    public String getSan() {
        if(this.sanCache.isEmpty() && this.parent != null) {
            this.sanCache = this.parent.getSan(this.move);
        }
        return this.sanCache;
    }

    public GameNode getParent() {
        return this.parent;
    }

    public Move getMove() {
        return this.move;
    }

    public void setMove(Move m) {
        this.move = m;
    }

    public void setParent(GameNode node) {
        this.parent = node;
    }

    public void setComment(String s) {
        this.comment = s;
    }

    public String getComment() {
        return this.comment;
    }

    public GameNode getVariation(int i) {
        if(this.variations.size() > i) {
            return this.variations.get(i);
        } else {
            throw new IllegalArgumentException("there are only "+this.variations.size() + " variations, but index "+i + "requested");
        }
    }

    public void deleteVariation(int i) {
        if(this.variations.size() > i) {
            this.variations.remove(i);
        } else {
            throw new IllegalArgumentException("there are only "+this.variations.size() + " variations, " +
                    "but index "+i + "requested for deletion");
        }
    }

    public ArrayList<GameNode> getVariations() {
        return this.variations;
    }

    public void addVariation(GameNode node) {
        this.variations.add(node);
    }

    public boolean hasVariations() {
        return this.variations.size() > 1;
    }

    public boolean hasChild() { return  this.variations.size() > 0; }

    public boolean isLeaf() {
        return this.variations.size() == 0;
    }

    public int getDepth() {
        if(this.parent == null) {
            return 0;
        } else {
            return this.parent.getDepth() + 1;
        }
    }

    /*
    @Override
    public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if(move != null) {
            gen.writeString(move.getUci());
        }
        if(!comment.isEmpty()) {
            gen.writeStartObject();
            gen.writeStringField("comment", comment);
            gen.writeEndObject();
        }
        if(clock >= 0) {
            gen.writeStartObject();
            gen.writeNumberField("clock", clock);
            gen.writeEndObject();
        }
        if(coloredFields != null && coloredFields.size() > 0) {
            for (ColoredField c : coloredFields) {
                gen.writeStartObject();
                gen.writeFieldName("circle");
                gen.writeStartObject();
                String coord = c.toString();
                gen.writeStringField("field", coord);
                gen.writeStringField("color", c.color);
                gen.writeEndObject();
                gen.writeEndObject();
            }
        }
        if(arrows != null && arrows.size() > 0) {
            for (Arrow a : arrows) {
                gen.writeStartObject();
                gen.writeFieldName("arrow");
                gen.writeStartObject();
                String coord = a.toString();
                gen.writeStringField("from", coord.substring(0,2));
                gen.writeStringField("to", coord.substring(2,4));
                gen.writeStringField("color", a.color);
                gen.writeEndObject();
                gen.writeEndObject();
            }
        }
        if(attachments != null) {
            for (Attachment a : attachments) {
                gen.writeStartObject();
                gen.writeStringField("url", a.url);
                gen.writeStringField("mediaType", a.mediaType);
                gen.writeEndObject();
            }
        }
        if(diagrams != null) {
            for (Diagram d : diagrams) {
                gen.writeStartObject();
                gen.writeFieldName("diagram");
                gen.writeStartObject();
                if(!d.label.isEmpty()) {
                    gen.writeStringField("label", d.label);
                }
                if(d.coloredFields.size() > 0) {
                    for (ColoredField c : coloredFields) {
                        gen.writeStartObject();
                        gen.writeFieldName("circle");
                        gen.writeStartObject();
                        String coord = c.toString();
                        gen.writeStringField("field", coord);
                        gen.writeStringField("color", c.color);
                        gen.writeEndObject();
                        gen.writeEndObject();
                    }
                }
                if(d.arrows.size() > 0) {
                    for (Arrow a : arrows) {
                        gen.writeStartObject();
                        gen.writeFieldName("arrow");
                        gen.writeStartObject();
                        String coord = a.toString();
                        gen.writeStringField("from", coord.substring(0,2));
                        gen.writeStringField("to", coord.substring(2,4));
                        gen.writeStringField("color", a.color);
                        gen.writeEndObject();
                        gen.writeEndObject();
                    }
                }
                gen.writeEndObject();
                gen.writeEndObject();
            }
        }

        if(evaluations != null && evaluations.size() > 0) {
            for(Evaluation e : evaluations) {
                gen.writeStartObject();
                if(e.cp != null) {
                    gen.writeNumberField("cp", e.cp);
                }
                if(e.mate != null) {
                    gen.writeNumberField("mate", e.mate);
                }
                if(e.depth != null) {
                    gen.writeNumberField("depth", e.depth);
                }
                if(!e.engine.isEmpty()) {
                    gen.writeStringField("engine", e.engine);
                }
                gen.writeEndObject();
            }
        }

        if(move != null && moveAnnotations != null && moveAnnotations.size() > 0) {
            gen.writeStartObject();
            gen.writeFieldName("moveAnnotations");
            gen.writeStartArray();
            for(String ma : moveAnnotations) {
                gen.writeString(ma);
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }

        if(positionAnnotations != null && positionAnnotations.size() > 0) {
            gen.writeStartObject();
            gen.writeFieldName("positionAnnotations");
            gen.writeStartArray();
            for(String pa : positionAnnotations) {
                gen.writeString(pa);
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }

        if(egt > 0) {
            gen.writeStartObject();
            gen.writeNumberField("egt", egt);
            gen.writeEndObject();
        }

        if(emt > 0) {
            gen.writeStartObject();
            gen.writeNumberField("emt", emt);
            gen.writeEndObject();
        }

        if(variations.size() > 0) {
            gen.writeObject(variations.get(0));
        }

        if(variations.size() > 1) {
            gen.writeStartObject();
            gen.writeFieldName("moves");
            gen.writeStartArray();
            for(int i=0;i<variations.size();i++) {
                gen.writeObject(variations.get(i));
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }

    }

    @Override
    public void serializeWithType(JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }
*/

}
