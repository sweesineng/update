package com.homenas.netdrive;

import android.support.v4.provider.DocumentFile;

import java.util.Comparator;
import java.util.Date;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by engss on 7/11/2017.
 */

public class SmbData implements Comparable<SmbData> {
    private String mfileName;
    private boolean isSmb;
    private Object mObject;
    private long mfileSize;
    private Date mlastModified;
    private Object mParent;
    private boolean isDir;
    private static String mSort;
    private static boolean mAccending;

    public SmbData(Object Obj, String sortby, boolean Accending) {
        if(Obj instanceof SmbFile) {
            isSmb = true;
            mfileName = ((SmbFile) Obj).getName();
            try {
                mfileSize = ((SmbFile) Obj).length();
                mlastModified = new Date(((SmbFile) Obj).getLastModified());
                isDir = ((SmbFile) Obj).isDirectory();
                mParent = ((SmbFile) Obj).getParent();
            } catch (SmbException e) {
                e.printStackTrace();
            }
        }
        if(Obj instanceof DocumentFile) {
            isSmb = false;
            mfileName = ((DocumentFile) Obj).getName();
            mfileSize = ((DocumentFile) Obj).length();
            mlastModified = new Date(((DocumentFile) Obj).lastModified());
            isDir = ((DocumentFile) Obj).isDirectory();
            mParent = ((DocumentFile) Obj).getParentFile();
        }
        mObject = Obj;
        mSort = sortby;
        mAccending = Accending;
    }

    public SmbData(Object Obj){
        if(Obj instanceof SmbFile) {
            isSmb = true;
            mfileName = ((SmbFile) Obj).getName();
            try {
                mfileSize = ((SmbFile) Obj).length();
                mlastModified = new Date(((SmbFile) Obj).getLastModified());
                isDir = ((SmbFile) Obj).isDirectory();
            } catch (SmbException e) {
                e.printStackTrace();
            }
        }
        if(Obj instanceof DocumentFile) {
            isSmb = false;
            mfileName = ((DocumentFile) Obj).getName();
            mfileSize = ((DocumentFile) Obj).length();
            mlastModified = new Date(((DocumentFile) Obj).lastModified());
            isDir = ((DocumentFile) Obj).isDirectory();
        }
        mObject = Obj;
    }

    public Object getObj() { return mObject; };

    public boolean isSmbFile() { return isSmb; };

    public boolean isDocumentFile() { return (isSmb)? false:true;}

    public String getObjName() { return mfileName; };

    public long getObjSize() { return mfileSize; };

    public Date getlastMod() { return mlastModified; };

    public boolean isDirectory() { return isDir; };

    public Object getParent() { return mParent; };

    @Override
    public int compareTo(SmbData Obj) {
        return SmbData.Comparators.Sort.compare(this, Obj);
    }

    public static class Comparators {
        public static Comparator<SmbData> Sort = new Comparator<SmbData>() {
            @Override
            public int compare(SmbData o1, SmbData o2) {
                int i;
                if(o1.isDirectory() && !o2.isDirectory()) {
                    return -1;
                }else if (!o1.isDirectory() && o2.isDirectory()){
                    return 1;
                }else{
                    switch (mSort) {
                        case Constants.SortName:
                            i = o1.getObjName().compareTo(o2.getObjName());
                        case Constants.SortSize:
                            i = Long.compare(o1.getObjSize(),o2.getObjSize());
                        case Constants.SortDate:
                            i = o1.getlastMod().compareTo(o2.getlastMod());
                        default:
                            i = o1.getObjName().compareTo(o2.getObjName());
                    }
                }
                return mAccending? i:i*(-1);
            }
        };
    }
}
