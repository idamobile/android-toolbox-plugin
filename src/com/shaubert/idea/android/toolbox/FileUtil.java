package com.shaubert.idea.android.toolbox;

public class FileUtil {

    public static String removeExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if (index > 0) {
            return filename.substring(0, index);
        } else {
            return filename;
        }
    }

}
