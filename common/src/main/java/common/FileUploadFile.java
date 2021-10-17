package common;

import java.io.File;
import java.io.Serializable;

public class FileUploadFile implements Serializable {

    private File file;
    private String fileName;
    private String comand;
    private String name;

    public FileUploadFile(File file, String name) {
        this.name = name;
        this.file = file;
        this.fileName = file.getAbsolutePath();
    }

    public FileUploadFile(String name, String comand) {
        this.name = name;
        this.comand = comand;
        this.fileName = name + comand;
    }

    public FileUploadFile(String comand) {
        this.comand = comand;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public String getComand() {
        return comand;
    }

    public void setComand(String comand) {
        this.comand = comand;
    }
}
