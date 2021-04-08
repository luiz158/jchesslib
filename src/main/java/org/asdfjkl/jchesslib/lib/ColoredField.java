package org.asdfjkl.jchesslib.lib;

public class ColoredField {

    public int x = 0;
    public int y = 0;
    public String color = "#FF0000"; // red

    @Override
    public boolean equals(Object o) {

        if (o instanceof ColoredField) {
            ColoredField other = (ColoredField) o;
            if(other.x == x && other.y == y) {
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
        char colFrom = (char) (x + 97);
        char rowFrom = (char) (y + 49);

        String coord = "" + colFrom + rowFrom;
        return coord;
    }

    public void fromPGNString(String s) {

        s = s.toUpperCase();
        if (s.length() != 3) {
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
            int column = Move.alphaToPos(s.charAt(1));
            int row = ((int) s.charAt(2)) - 49;

            if(column >= 0 && column <= 8 && row >= 0 && row <= 8) {
                this.color = color;
                this.x = column;
                this.y = row;
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

}
