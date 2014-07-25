/*
 * Copyright (c) 2014 OpenSilk Productions LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.opensilk.filebrowser;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.opensilk.filebrowser.FileItem.MediaType;

/**
 * Created by drew on 6/4/14.
 */
public class FileItemUtil {

    /**
     * Check existence of file and increment name as needed.
     */
    public static File incrementFileName(String directory, String fullName) {
        int ii = 1;
        File dest;
        String ext, name, dot = ".";

        ext = getFileExtension(fullName);
        if ("".equals(ext)) {
            dot = "";
            name = fullName;
        } else {
            name = fullName.substring(0, fullName.lastIndexOf(ext)-1);
        }
        dest = new File(directory, fullName);

        while (dest.exists()) {
            dest = new File(directory, name+"-"+String.valueOf(ii)+dot+ext);
            ii++;
        }
        return dest;
    }

    /**
     * Gets extension from file
     * @param f
     * @return
     */
    public static String getFileExtension(File f) {
        return getFileExtension(f.getName());
    }

    /**
     *
     * @param name
     * @return
     */
    public static String getFileExtension(String name) {
        String ext;
        int lastDot = name.lastIndexOf('.');
        int secondLastDot = name.lastIndexOf('.', lastDot-1);
        if (secondLastDot > 0 ) { // Double extension
            ext = name.substring(secondLastDot + 1);
            if (!ext.startsWith("tar")) {
                ext = name.substring(lastDot + 1);
            }
        } else if (lastDot > 0) { // Single extension
            ext = name.substring(lastDot + 1);
        } else { // No extension
            ext = "";
        }
        return ext;
    }

    /**
     * @param size
     * @param mediaType
     * @return human readable size string
     */
    public static String prettyPrintSize(Context ctx, long size, int mediaType) {
        if (mediaType == MediaType.DIRECTORY) {
            return ctx.getResources().getQuantityString(R.plurals.Nitems, (int)size, size);
        }

        String suffix = "Bytes";
        double sz = (double) size;
        if (sz > 1024) {
            sz /= 1024;
            suffix = "KiB";
        }
        if (sz > 1024) {
            sz /= 1024;
            suffix = "MiB";
        }
        if (sz > 1024) {
            sz /= 1024;
            suffix = "GiB";
        }
        return String.format(Locale.US, "%.02f %s", sz, suffix);
    }

    /**
     * Returns human readable date string
     * @param ctx
     * @param sec
     * @return
     */
    public static String prettyPrintDate(Context ctx, long sec) {
        DateFormat df = android.text.format.DateFormat.getDateFormat(ctx);
        return df.format(new Date(sec*1000));
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
        if (mediaType == MediaType.NONE || mediaType == MediaType.DIRECTORY) {
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
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(f));
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
     * Repurposes size with item count for directories
     * @param size
     * @param mediaType
     * @param path
     * @return size if file else item count in directory given by path
     */
    public static long resolveSize(long size, int mediaType, String path) {
        long sz;
        if (mediaType == MediaType.DIRECTORY || mediaType == MediaType.NONE) {
            File f = new File(path);
            if (f.isDirectory() && f.canRead()) {
                File[] files = f.listFiles();
                if (files != null) {
                    sz = files.length;
                } else {
                    sz = 0;
                }
            } else { //file
                sz = size;
            }
        } else {
            sz = size;
        }
        return sz;
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
                .setPath(parent)
                .setSize(-1);
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
                .setPath(parent)
                .setSize(-1);
    }
}
