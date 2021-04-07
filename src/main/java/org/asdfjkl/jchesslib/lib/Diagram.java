package org.asdfjkl.jchesslib.lib;

import java.util.ArrayList;

public class Diagram {

    public String label = "";
    public ArrayList<Arrow> arrows = new ArrayList<>();
    public ArrayList<ColoredField> coloredFields = new ArrayList<>();

    @Override
    public boolean equals(Object o) {

        if (o instanceof Diagram) {
            Diagram other = (Diagram) o;
            if (other.label.equals(label)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
