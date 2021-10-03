package common;

import java.io.File;
import java.io.Serializable;

public class FileUploadFile implements Serializable {

    private final File file;
    private final String fileName;

    public FileUploadFile(File file) {
        this.file = file;
        this.fileName = file.getAbsolutePath();
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }
}
