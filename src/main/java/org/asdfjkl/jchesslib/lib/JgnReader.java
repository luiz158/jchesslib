package org.asdfjkl.jchesslib.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.Map;

public class JgnReader {

    private void parseInfo(Game g, JsonNode jsonNode) {

        Iterator<Map.Entry<String, JsonNode>> iter = jsonNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> entry = iter.next();

            JsonNode value = entry.getValue();
            if (value.isTextual()) {
                g.setStringHeader(entry.getKey(), entry.getValue().asText());
            }
            if (value.isNumber()) {
                g.setNumberHeader(entry.getKey(), entry.getValue().asInt());
            }
        }
    }

    private void parseCircle(GameNode g, JsonNode jsonNode) {

        if(jsonNode.has("square") && jsonNode.get("square").isTextual()
        && jsonNode.has("color") && jsonNode.get("color").isTextual()) {
            try {
                ColoredField c = new ColoredField();
                c.fromPGNString("B"+jsonNode.get("square").asText());
                c.color = jsonNode.get("color").asText();
                g.addOrRemoveColoredField(c);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    private void parseCircle(Diagram d, JsonNode jsonNode) {

        if(jsonNode.has("square") && jsonNode.get("square").isTextual()
                && jsonNode.has("color") && jsonNode.get("color").isTextual()) {
            try {
                ColoredField c = new ColoredField();
                c.fromPGNString("B"+jsonNode.get("square").asText());
                c.color = jsonNode.get("color").asText();
                d.coloredFields.add(c);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    private void parseArrow(GameNode g, JsonNode jsonNode) {

        if(jsonNode.has("from") && jsonNode.get("from").isTextual()
                && jsonNode.has("to") && jsonNode.get("to").isTextual()
                && jsonNode.has("color") && jsonNode.get("color").isTextual()) {
            try {
                Arrow a = new Arrow();
                a.fromPGNString("B"+jsonNode.get("from").asText() + jsonNode.get("to").asText());
                a.color = jsonNode.get("color").asText();
                g.addOrRemoveArrow(a);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    private void parseArrow(Diagram d, JsonNode jsonNode) {

        if(jsonNode.has("from") && jsonNode.get("from").isTextual()
                && jsonNode.has("to") && jsonNode.get("to").isTextual()
                && jsonNode.has("color") && jsonNode.get("color").isTextual()) {
            try {
                Arrow a = new Arrow();
                a.fromPGNString("B"+jsonNode.get("from").asText() + jsonNode.get("to").asText());
                a.color = jsonNode.get("color").asText();
                d.arrows.add(a);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    private void parseAttachment(GameNode g, JsonNode jsonNode) {
        if(jsonNode.has("url") && jsonNode.get("url").isTextual()
                && jsonNode.has("mediaType") && jsonNode.get("mediaType").isTextual()) {
            Attachment a = new Attachment();
            a.url = jsonNode.get("url").asText();
            a.mediaType = jsonNode.get("mediaType").asText();
            g.addAttachment(a);
        }

    }

    private void parseDiagram(GameNode g, JsonNode jsonNode) {
        Diagram d = new Diagram();

        Iterator<Map.Entry<String, JsonNode>> iter = jsonNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> entry = iter.next();
            String fieldName = entry.getKey();
            JsonNode value = entry.getValue();

            if (fieldName.equals("label") && value.isTextual()) {
                d.label = value.asText();
            }
            if (fieldName.equals("arrow") && value.isObject()) {
                parseArrow(d, value);
            }
            if (fieldName.equals("circle") && value.isObject()) {
                parseCircle(d, value);
            }
        }
        if(d!=null) {
            g.addDiagram(d);
        }
    }

    private void parseEvaluation(GameNode g, JsonNode jsonNode) {
        Evaluation e = null;
        if(jsonNode.has("cp") && jsonNode.get("cp").isNumber()) {
            e = new Evaluation();
            e.cp = jsonNode.get("cp").asInt();
        }
        if(jsonNode.has("mate") && jsonNode.get("mate").isNumber()) {
            if(e == null) {
                e = new Evaluation();
            }
            e.mate = jsonNode.get("mate").asInt();
        }
        if(e != null) {
            if(jsonNode.has("engine") && jsonNode.get("engine").isTextual()) {
                e.engine = jsonNode.get("engine").asText();
            }
            if(jsonNode.has("depth") && jsonNode.get("depth").isNumber()) {
                e.engine = jsonNode.get("depth").asText();
            }
            g.addEvaluation(e);
        }
    }

    private void parseMoves(GameNode gameNode, JsonNode jsonNode) {

        GameNode currentNode = gameNode;
        if(jsonNode.isArray()) {

            Iterator<JsonNode> iter = jsonNode.elements();
            while (iter.hasNext()) {

                JsonNode arrayElmement = iter.next();

                if(arrayElmement.isTextual()) {

                    String uciMove = arrayElmement.asText();
                    Move m = null;
                    Board b = null;
                    try {
                        m = new Move(uciMove);
                        b = currentNode.getBoard().makeCopy();
                        b.apply(m);
                    } catch(IllegalArgumentException e) {
                    }
                    if(m != null && b != null) {
                        GameNode mainLineChild = new GameNode();
                        mainLineChild.setBoard(b);
                        mainLineChild.setMove(m);
                        currentNode.addVariation(mainLineChild);
                        mainLineChild.setParent(currentNode);
                        currentNode = mainLineChild;
                    }
                }

                if(arrayElmement.isObject()) {
                    if(arrayElmement.has("comment") && arrayElmement.get("comment").isTextual()) {
                        currentNode.setComment(arrayElmement.get("comment").asText());
                    }
                    if(arrayElmement.has("clock") && arrayElmement.get("clock").isNumber()) {
                        currentNode.setClock(arrayElmement.get("clock").asInt());
                    }
                    if(arrayElmement.has("emt") && arrayElmement.get("emt").isNumber()) {
                        currentNode.setEmt(arrayElmement.get("emt").asInt());
                    }
                    if(arrayElmement.has("egt") && arrayElmement.get("egt").isNumber()) {
                        currentNode.setEgt(arrayElmement.get("egt").asInt());
                    }
                    if(arrayElmement.has("positionAnnotation") && arrayElmement.get("positionAnnotation").isArray()) {
                        Iterator<JsonNode> posAnnIter = arrayElmement.get("positionAnnotation").elements();
                        while (iter.hasNext()) {
                            JsonNode nodePosAn = iter.next();
                            if(nodePosAn.isTextual()) {
                                currentNode.addPositionAnnotation(nodePosAn.asText());
                            }
                        }
                    }
                    if(arrayElmement.has("moveAnnotation") && arrayElmement.get("moveAnnotation").isArray()) {
                        Iterator<JsonNode> posAnnIter = arrayElmement.get("moveAnnotation").elements();
                        while (iter.hasNext()) {
                            JsonNode nodeMoveAn = iter.next();
                            if(nodeMoveAn.isTextual()) {
                                currentNode.addMoveAnnotation(nodeMoveAn.asText());
                            }
                        }
                    }
                    if(arrayElmement.has("circle") && arrayElmement.get("circle").isObject()) {
                        parseCircle(currentNode, arrayElmement.get("circle"));
                    }
                    if(arrayElmement.has("arrow") && arrayElmement.get("arrow").isObject()) {
                        parseArrow(currentNode, arrayElmement.get("arrow"));
                    }
                    if(arrayElmement.has("attachment") && arrayElmement.get("attachment").isObject()) {
                        parseAttachment(currentNode, arrayElmement.get("attachment"));
                    }
                    if(arrayElmement.has("diagram") && arrayElmement.get("diagram").isObject()) {
                        parseDiagram(currentNode, arrayElmement.get("diagram"));
                    }
                    if(arrayElmement.has("evaluation") && arrayElmement.get("evaluation").isObject()) {
                        parseEvaluation(currentNode, arrayElmement.get("evaluation"));
                    }
                    if(arrayElmement.has("moves")) {
                        if(currentNode.getParent() != null) {
                            JsonNode nodeVariation = arrayElmement.get("moves");
                            parseMoves(currentNode.getParent(), nodeVariation);
                        }
                    }
                }
            }
        }
    }


    public Game read(JsonNode node) throws IllegalArgumentException {

        Game g = new Game();
        // add a default root node
        GameNode root = new GameNode();
        Board rootBoard = new Board(true);
        root.setBoard(rootBoard);
        g.setRoot(root);


        if(node.isObject()) {

            ObjectNode obj = (ObjectNode) node;

            boolean validGame = false;
            if(obj.has("type") && obj.get("type").isTextual()) {
                String gameType = obj.get("type").asText();
                if(gameType.equals("Game")) {
                    validGame = true;
                }
            }
            if(!validGame) {
                throw new IllegalArgumentException("Not a valid JGN game (missing type : Game");
            }
            boolean supportedVariant = false;
            if(obj.has("variant")) {
                if(obj.get("variant").isTextual()) {
                    String variant = obj.get("variant").asText();
                    if(variant.equals("Chess")) {
                        supportedVariant = true;
                    }
                    if(variant.equals("Chess960")) {
                        throw new IllegalArgumentException("Chess 960 not implemented yet");
                    }
                }
            } else { // no explicit variant field; assume chess
                supportedVariant = true;
            }
            if(!supportedVariant) {
                throw new IllegalArgumentException("Chess variant not supported/unknown");
            }

            Iterator<Map.Entry<String, JsonNode>> iter = node.fields();
            while (iter.hasNext()) {
                Map.Entry<String, JsonNode> entry = iter.next();
                //System.out.println(entry.getKey());
                String fieldName = entry.getKey();
                JsonNode subNode = entry.getValue();

                if(fieldName.equals("info")) {
                    parseInfo(g, subNode);
                }

                if(fieldName.equals("moves")) {
                    parseMoves(root, subNode);
                }

            }
        }
        return g;
    }
}