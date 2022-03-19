package common;

import java.io.File;
import java.io.Serializable;
import java.util.List;
// Основной "транспорт".
// При помощи объектов этого класса осуществляется переброска команд, путей, файлов, логинов и паролей
public class FileUploadFile implements Serializable {

    private File file;
    private String fileName;
    private Comands comand;
    private String name;
    private List<String> allPaths;
    private String pass;

    public FileUploadFile(List<String> allPaths, String name, Comands comand) {
        this.allPaths = allPaths;
        this.name = name;
        this.comand = comand;
    }

    public FileUploadFile(String name, Comands comand) {
        this.name = name;
        this.comand = comand;
    }

    public FileUploadFile(String name) {
        this.name = name;
    }

    public FileUploadFile(File file, String name, Comands comand) {
        this.name = name;
        this.file = file;
        this.fileName = file.getAbsolutePath();
        this.comand = comand;
    }

    public FileUploadFile(FileUploadFile msg) {
        this.file = msg.getFile();
        this.fileName = msg.getFileName();
        this.comand = msg.getComand();
        this.name = msg.getName();
        this.allPaths = msg.getAllPaths();
        this.pass = msg.pass;
    }

    public List<String> getAllPaths() {
        return allPaths;
    }

    public void setAllPaths(List<String> allPaths) {
        this.allPaths = allPaths;
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

    public Comands getComand() {
        return comand;
    }

    public void setComand(Comands comand) {
        this.comand = comand;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
