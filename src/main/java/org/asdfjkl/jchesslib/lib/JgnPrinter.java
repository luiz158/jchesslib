package org.asdfjkl.jchesslib.lib;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;

public class JgnPrinter extends JsonSerializer<Game> {

    public void serializeGameNode(GameNode gameNode, JsonGenerator gen,
                                  SerializerProvider serializer) throws IOException {

        if(gameNode.getMove() != null) {
            gen.writeString(gameNode.getMove().getUci());
        }
        if(!gameNode.getComment().isEmpty()) {
            gen.writeStartObject();
            gen.writeStringField("comment", gameNode.getComment());
            gen.writeEndObject();
        }
        if(gameNode.getClock() >= 0) {
            gen.writeStartObject();
            gen.writeNumberField("clock", gameNode.getClock());
            gen.writeEndObject();
        }
        if(gameNode.getColoredFields() != null && gameNode.getColoredFields().size() > 0) {
            for (ColoredField c : gameNode.getColoredFields()) {
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
        if(gameNode.getArrows() != null && gameNode.getArrows().size() > 0) {
            for (Arrow a : gameNode.getArrows()) {
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
        if(gameNode.getAttachments() != null) {
            for (Attachment a : gameNode.getAttachments()) {
                gen.writeStartObject();
                gen.writeStringField("url", a.url);
                gen.writeStringField("mediaType", a.mediaType);
                gen.writeEndObject();
            }
        }
        if(gameNode.getDiagrams() != null) {
            for (Diagram d : gameNode.getDiagrams()) {
                gen.writeStartObject();
                gen.writeFieldName("diagram");
                gen.writeStartObject();
                if(!d.label.isEmpty()) {
                    gen.writeStringField("label", d.label);
                }
                if(d.coloredFields.size() > 0) {
                    for (ColoredField c : d.coloredFields) {
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
                    for (Arrow a : d.arrows) {
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

        if(gameNode.getEvaluations() != null && gameNode.getEvaluations().size() > 0) {
            for(Evaluation e : gameNode.getEvaluations()) {
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

        if(gameNode.getMove() != null && gameNode.getMoveAnnotations() != null
                && gameNode.getMoveAnnotations().size() > 0) {
            gen.writeStartObject();
            gen.writeFieldName("moveAnnotations");
            gen.writeStartArray();
            for(String ma : gameNode.getMoveAnnotations()) {
                gen.writeString(ma);
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }

        if(gameNode.getPositionAnnotations() != null && gameNode.getPositionAnnotations().size() > 0) {
            gen.writeStartObject();
            gen.writeFieldName("positionAnnotations");
            gen.writeStartArray();
            for(String pa : gameNode.getPositionAnnotations()) {
                gen.writeString(pa);
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }

        if(gameNode.getEgt() > 0) {
            gen.writeStartObject();
            gen.writeNumberField("egt", gameNode.getEgt());
            gen.writeEndObject();
        }

        if(gameNode.getEmt() > 0) {
            gen.writeStartObject();
            gen.writeNumberField("emt", gameNode.getEmt());
            gen.writeEndObject();
        }

        if(gameNode.getVariations().size() > 0) {
            serializeGameNode(gameNode.getVariations().get(0), gen, serializer);
        }

        if(gameNode.getVariations().size() > 1) {
            gen.writeStartObject();
            gen.writeFieldName("moves");
            gen.writeStartArray();
            for(int i=1;i<gameNode.getVariations().size();i++) {
                serializeGameNode(gameNode.getVariations().get(i), gen, serializer);
            }
            gen.writeEndArray();
            gen.writeEndObject();
        }
    }


    @Override
    public void serialize(Game game, JsonGenerator gen, SerializerProvider serializerProvider)
            throws IOException {

        gen.writeStartObject();
        gen.writeStringField("type", "Game");
        gen.writeFieldName("info");
        gen.writeStartObject();
        for(String key : game.getInfoStrings().keySet()) {
            gen.writeStringField(key, game.getInfoStrings().get(key));
        }
        for(String key : game.getInfoNumbers().keySet()) {
            gen.writeNumberField(key, game.getInfoNumbers().get(key));
        }
        gen.writeEndObject();
        gen.writeFieldName("moves");
        gen.writeStartArray();
        serializeGameNode(game.getRootNode(), gen, serializerProvider);
        gen.writeEndArray();
        gen.writeEndObject();
    }

    @Override
    public Class<Game> handledType() {
        throw new UnsupportedOperationException("Not supported.");
    }
}
