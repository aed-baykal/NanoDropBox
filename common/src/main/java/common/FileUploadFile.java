package common;

import java.io.File;
import java.io.Serializable;

public class FileUploadFile implements Serializable {

    private File file;
    private String fileName;
    private String comand;

    public FileUploadFile(File file) {
        this.file = file;
        this.fileName = file.getAbsolutePath();
        this.comand = "OK";
    }

    public FileUploadFile() {
        this.comand = "OK";
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    public String getComand() {
        return comand;
    }

    public void setComand(String comand) {this.comand = comand;}

}
