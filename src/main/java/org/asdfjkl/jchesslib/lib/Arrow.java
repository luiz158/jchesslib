package org.asdfjkl.jchesslib.lib;

public class Arrow {

    public int xFrom = 0;
    public int yFrom = 0;
    public int xTo = 0;
    public int yTo = 0;
    String color = "#00FF00"; // green

    @Override
    public boolean equals(Object o) {

        if (o instanceof Arrow) {
            Arrow other = (Arrow) o;
            if (other.xFrom == xFrom && other.yFrom == yFrom
                    && other.xTo == xTo && other.yTo == yTo) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        char colFrom = (char) (xFrom + 97);
        char rowFrom = (char) (yFrom + 49);

        char colTo = (char) (xTo + 97);
        char rowTo = (char) (yTo + 49);

        String coord = "" + colFrom + rowFrom + colTo + rowTo;
        return coord;
    }

    public String toPGNColor() {
        if(color.equals("#FF0000")) {
            return "R";
        }
        if(color.equals("#00FF00")) {
            return "G";
        }
        if(color.equals("#FFFF00")) {
            return "Y";
        }
        return "B";
    }

    public void fromPGNString(String s) {

        s = s.toUpperCase();
        if (s.length() != 5) {
            throw new IllegalArgumentException();
        } else {
            String color = null;
            if (s.substring(0, 1).equals("R")) {
                color = "#FF0000";
            } else if (s.substring(0, 1).equals("G")) {
                color = "#00FF00";
            } else if (s.substring(0, 1).equals("Y")) {
                color = "#FFFF00";
            } else if (s.substring(0, 1).equals("B")) {
                color = "#000000";
            } else {
                color = "#000000";
            }
            int fromColumn = Move.alphaToPos(s.charAt(1)) - 1;
            int fromRow = ((int) s.charAt(2)) - 49;

            int toColumn = Move.alphaToPos(s.charAt(3)) - 1;
            int toRow = ((int) s.charAt(4)) - 49;

            if(fromColumn >= 0 && fromColumn <= 8 && fromRow >= 0 && fromRow <= 8
                && toColumn >= 0 && toColumn <= 8 && toRow >= 0 && toRow <= 8) {
                this.color = color;
                this.xFrom = fromColumn;
                this.yFrom = fromRow;
                this.xTo = toColumn;
                this.yTo = toRow;
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

}
