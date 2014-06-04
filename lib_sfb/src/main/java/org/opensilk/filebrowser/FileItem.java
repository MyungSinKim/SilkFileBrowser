package org.opensilk.filebrowser;

import android.content.Context;
import android.os.Environment;
import android.provider.MediaStore.Files.FileColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.Locale;

/**
 * Created by drew on 4/30/14.
 */
public class FileItem {

    public static class MediaType {
        public static final int NONE = FileColumns.MEDIA_TYPE_NONE;
        public static final int IMAGE = FileColumns.MEDIA_TYPE_IMAGE;
        public static final int AUDIO = FileColumns.MEDIA_TYPE_AUDIO;
        public static final int VIDEO = FileColumns.MEDIA_TYPE_VIDEO;
        public static final int DIRECTORY    = 0x00000100;
        public static final int UP_DIRECTORY = 0x00000200;
        public static String toString(int mediaType) {
            switch (mediaType) {
                case NONE:
                    return "NONE";
                case IMAGE:
                    return "IMAGE";
                case AUDIO:
                    return "AUDIO";
                case VIDEO:
                    return "VIDEO";
                case DIRECTORY:
                    return "DIRECTORY";
                case UP_DIRECTORY:
                    return "UP_DIRECTORY";
            }
            return null;
        }
    }

    private String title;
    private String mimeType;
    private long parent;
    private long id;
    private int mediaType;
    private String path;
    private long size;
    private long date;

    public FileItem setTitle(String title) {
        this.title = title;
        return this;
    }

    public FileItem setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public FileItem setParent(long parent) {
        this.parent = parent;
        return this;
    }

    public FileItem setId(long id) {
        this.id = id;
        return this;
    }

    public FileItem setMediaType(int mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public FileItem setPath(String path) {
        this.path = path;
        return this;
    }

    public FileItem setSize(long size) {
        this.size = size;
        return this;
    }

    public FileItem setDate(long date) {
        this.date = date;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getMimeType() {
        return mimeType;
    }


    public long getParent() {
        return parent;
    }

    public long getId() {
        return id;
    }

    public int getMediaType() {
        return mediaType;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public long getDate() {
        return date;
    }

    @Override
    public String toString() {
//        return String.format(Locale.US, "title=%s,mime=%s,parent=%d,id=%d,mediaType=%s,path=%s",
//                title, mimeType, parent, id, MediaType.toString(mediaType), path);
        return title;
    }

    /**
     * Fixes titles for unknown MediaTypes returned value and title arg
     * should only differ if file/dir name has dots in it.
     * @param title
     * @param mediaType
     * @param path
     * @return title if mediaType != NONE else fileName from given path
     */
    public static String resolveTitle(String title, int mediaType, String path) {
        if (mediaType == MediaType.NONE) {
            File f = new File(path);
            return f.getName();
        }
        return title;
    }

    /**
     * Resolves mediaType for directories
     * @param mediaType
     * @param path
     * @return
     */
    public static int resolveType(int mediaType, String path) {
        if (mediaType == MediaType.NONE) {
            File f = new File(path);
            if (f.isDirectory()) {
                return MediaType.DIRECTORY;
            }
        }
        return mediaType;
    }

    /**
     * Guess mime based on file extension
     * @param mimeType existing mime, can be null
     * @param path file path
     * @return if mimeType not null mimeType else resolvedMime from path or
     *      if path is directory null
     */
    public static String resolveMime(String mimeType, String path) {
        if (TextUtils.isEmpty(mimeType)) {
            File f = new File(path);
            if (!f.isDirectory()) {
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FBUtil.getFileExtension(f));
                if (mime == null) {
                    mime = "application/octet-stream";
                }
                return mime;
            }
            return null; //Directories have no mime
        }
        return mimeType;
    }



    /**
     * Creates the special updir item
     * @param rootDir current directory path
     * @return null if rootDir is /sdcard
     */
    public static FileItem upDir(String rootDir, long parentId) {
        if (Environment.getExternalStorageDirectory().getAbsolutePath().equals(rootDir)) {
            return null;// /sdcard is as far up as we can go
        }
        File f = new File(rootDir);
        String parent = f.getParent();
        return new FileItem().setTitle("..")
                .setMediaType(MediaType.UP_DIRECTORY)
                .setId(parentId)
                .setPath(parent);
    }

    public static FileItem upDir(Context context, String directory) {
        if (Environment.getExternalStorageDirectory().getAbsolutePath().equals(directory)) {
            return null;// /sdcard is as far up as we can go
        }
        File f = new File(directory);
        String parent = f.getParent();
        long id = MediaProviderUtil.getDirectoryId(context, parent);
        if (id < 0) {
            return null;
        }
        return new FileItem()
                .setTitle("..")
                .setMediaType(MediaType.UP_DIRECTORY)
                .setId(id)
                .setPath(parent);
    }
}
