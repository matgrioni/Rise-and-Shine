package utils;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author - Matias Grioni
 * @created - 8/18/15
 *
 * A class that exports to a provided path on the sd card if possible. The Exporter instance will
 * create the desired path if it does not exist yet. The exporter can then create files inside of
 * this folder and write data to it.
 */
public class Exporter {
    private String exportPath;
    private boolean valid;

    /**
     * Create the exporter object with the path relative to the sd card root.
     *
     * @param path - The path relative to the sd card root to export to.
     */
    public Exporter(String path) {
        this.exportPath = "";

        // Check if this provided path can be made or is already in existence.
        valid = false;
        if (isExternalStorageWritable()) {
            // Create the path and check if it exists. If so success already, otherwise try to make it.
            File f = new File(Environment.getExternalStorageDirectory(), path);
            valid = f.exists();

            // If this call returns false it has to be cause of an error because the path is known
            // to not exist, so the false can not be indicative of the path already existing.
            // The call returns true if it was successful.
            if (!valid)
                valid = f.mkdirs();

            this.exportPath = Environment.getExternalStorageDirectory().getPath() + "/" + path;
        }
    }

    /**
     * Writes the given byte array to the specified file in the folder for this Exporter object. The
     * folder for this object is the path provided in the constructor tacked onto the external
     * storage location. The file is created if it didn't exist prior.
     *
     * Check if Exporter is valid before calling this method.
     *
     * @param filename - The filename to write to.
     * @param data - The data to write to the desired file.
     * @return - True if the operation completed successfully and False otherwise.
     */
    public boolean write(String filename, byte[] data) {
        File output = new File(exportPath, filename);
        boolean success = true;

        try {
            FileOutputStream outputStream = new FileOutputStream(output);
            outputStream.write(data);
            outputStream.close();
        } catch (IOException ex) {
            success = false;
        }

        return success;
    }

    /**
     * Returns whether the provided filename exists in the export folder.
     *
     * Check if Exporter is valid before calling this method.
     *
     * @param filename - The filename to check for existence in the export path.
     * @return - True if the file exists, false otherwise.
     */
    public boolean exists(String filename) {
        return new File(exportPath, filename).exists();
    }

    /**
     * An Exporter object is valid if the constructor was able to create or get the File object for
     * the desired export path on the sd card.
     *
     * @return - True if the object is valid, false otherwise.
     */
    public boolean isValid() {
        return this.valid;
    }

    /**
     * Checks if the external storage is writable.
     *
     * @return - True if the external storage is writable and false otherwise.
     */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}
