package org.opensilk.filebrowser;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

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

    public static long getParentId(Context context, String directory) {
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
        long parentId = -1;
        if (c != null) {
            if (c.moveToFirst()) {
                parentId = c.getLong(0);
            }
            c.close();
        }
        return parentId;
    }
}
