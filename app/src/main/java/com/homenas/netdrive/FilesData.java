package com.homenas.netdrive;

import android.support.v4.provider.DocumentFile;

import java.util.Comparator;
import java.util.Date;

import jcifs.smb.SmbFile;

/**
 * Created by engss on 7/11/2017.
 */

public class FilesData implements Comparable<FilesData>{
    private DocumentFile mfile;
    private SmbFile sfile;
    private String mfileName;
    private long mfileSize;
    private Date mlastModified;
    private static String mSort;
    private static boolean mAccending;

    public FilesData(DocumentFile file, String sortby, boolean Accending) {
        mfile = file;
        mfileName = file.getName();
        mfileSize = file.length();
        mlastModified = new Date(file.lastModified());
        mSort = sortby;
        mAccending = Accending;
    }

    public DocumentFile getDoc() { return mfile; }

    public String getDocName() {
        return mfileName;
    }

    public long getDocSize() { return mfileSize; }

    public Date getLastMod() { return mlastModified; }

    @Override
    public int compareTo(FilesData file) {
        return Comparators.Sort.compare(this, file);
    }

    public static class Comparators {
        public static Comparator<FilesData> Sort = new Comparator<FilesData>() {
            @Override
            public int compare(FilesData o1, FilesData o2) {
                int i;
                if(o1.getDoc().isDirectory() && !o2.getDoc().isDirectory()) {
                    return -1;
                }else if (!o1.getDoc().isDirectory() && o2.getDoc().isDirectory()){
                    return 1;
                }else{
                    switch (mSort) {
                        case "Name":
                            i = o1.getDocName().compareTo(o2.getDocName());
                            break;
                        case "Size":
                            i = Long.compare(o1.getDocSize(),o2.getDocSize());
                            break;
                        default:
                            i = o1.getDocName().compareTo(o2.getDocName());
                    }
                    if(mAccending) {
                        return i;
                    }else{
                        return i * (-1);
                    }
                }
            }
        };
    }
}
