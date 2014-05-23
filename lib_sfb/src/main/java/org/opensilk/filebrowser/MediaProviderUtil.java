package org.opensilk.filebrowser;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by drew on 5/1/14.
 */
public class MediaProviderUtil {

    public static final String[] FILES_PROJECTION;

    static {
        FILES_PROJECTION = new String[] {
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.PARENT,
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.DATA,
        };
    }

    @DebugLog
    public static FileItem fileItemFromCursor(Cursor c) {
        final String displayName = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
        final String title = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE));
        final String mime = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE));
        final long parent = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.PARENT));
        final long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
        final int mediaType = c.getInt(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE));
        final String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));

        final String name;
        if (displayName != null) {
            name = displayName;
        } else {
            name = title;
        }
        if (TextUtils.isEmpty(name)) {
            return null; // Its a ghost
        }
        return new FileItem()
                .setTitle(FileItem.resolveTitle(name, mediaType, path))
                .setMimeType(FileItem.resolveMime(mime, path))
                .setParent(parent)
                .setId(id)
                .setMediaType(FileItem.resolveType(mediaType, path))
                .setPath(path);
    }

    public static long getDirectoryId(Context context, String directory) {
        if (Environment.getExternalStorageDirectory().getAbsolutePath().equals(directory)) {
            return 0;
        }
        Cursor c = context.getContentResolver()
                .query(MediaStore.Files.getContentUri("external"),
                        new String[] {
                                MediaStore.Files.FileColumns._ID,
                        },
                        MediaStore.Files.FileColumns.DATA + "=?",
                        new String[] {
                                directory,
                        },
                        null);
        long id = -1;
        if (c != null) {
            if (c.moveToFirst()) {
                id = c.getLong(0);
            }
            c.close();
        }
        return id;
    }

    public static String getPath(Context context, long id) {
        if (id == 0) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        Cursor c = context.getContentResolver()
                .query(MediaStore.Files.getContentUri("external"),
                        new String[] {
                                MediaStore.Files.FileColumns.DATA,
                        },
                        MediaStore.Files.FileColumns._ID + "=?",
                        new String[] {
                                String.valueOf(id),
                        },
                        null);
        String path = null;
        if (c != null) {
            if (c.moveToFirst()) {
                path = c.getString(0);
            }
            c.close();
        }
        return path;
    }

    public static List<FileItem> ls(Context context, String directory) {
        long id = getDirectoryId(context, directory);
        if (id < 0) {
            return null;
        }
        Cursor c = context.getContentResolver()
                .query(MediaStore.Files.getContentUri("external"),
                        MediaProviderUtil.FILES_PROJECTION,
//                        FileColumns.DATA + " like '" + directory + "%'",// and not like '" + directory+"%/%'",
//                        null,
                        MediaStore.Files.FileColumns.PARENT + "=?",
                        new String[] {
                                String.valueOf(id),
                        },
                        MediaStore.Files.FileColumns.DATA
                );
        if (c != null) {
            List<FileItem> items = new ArrayList<>();
            FileItem up = FileItem.upDir(context, directory);
            if (up != null) {
                items.add(up);
            }
            if (c.moveToFirst()) {
                do {
                    FileItem i = MediaProviderUtil.fileItemFromCursor(c);
                    if (i != null) {
                        items.add(i);
                    }
                } while (c.moveToNext());
            }
            c.close();
            return items;
        }
        return null;
    }


}
