package com.dkl;

import java.util.HashMap;

public class PgnEntry {

    public HashMap<String, String> header;
    public long offset;

    public  PgnEntry() {
        this.header = new HashMap<>();
        this.offset = 0;
    }
}
