package common;

import java.io.File;
import java.io.Serializable;

public class FileUploadFile implements Serializable {

    private File file;
    private int starPos;
    private byte[] bytes;
    private int endPos;

    public FileUploadFile(File file) {
        this.file = file;
        this.starPos = 0;
        this.bytes = new byte[(int) file.length()];
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getStartPos() {
        return starPos;
    }

    public void setStarPos(int starPos) {
        this.starPos = starPos;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

}
