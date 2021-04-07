package org.asdfjkl.jchesslib.lib;

public class Attachment {

    public String url = "";
    public String mediaType = "";

    @Override
    public boolean equals(Object o) {

        if (o instanceof Attachment) {
            Attachment other = (Attachment) o;
            if (other.url.equals(url) && other.mediaType.equals(mediaType)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
