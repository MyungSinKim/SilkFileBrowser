package org.opensilk.filebrowser;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

//import hugo.weaving.DebugLog;

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
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED
        };
    }

    //@DebugLog
    public static FileItem fileItemFromCursor(Cursor c) {
        final String displayName = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
        final String title = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE));
        final String mime = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE));
        final long parent = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.PARENT));
        final long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
        final int mediaType = c.getInt(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE));
        final String path = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
        final long size = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE));
        final long date = c.getLong(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED));

        final String name;
        if (!TextUtils.isEmpty(displayName)) {
            name = displayName;
        } else {
            name = title;
        }
        if (TextUtils.isEmpty(name)) {
            return null; // Its a ghost
        }

        final int realMediaType = FileItemUtil.resolveType(mediaType, path);
        final String realTitle = FileItemUtil.resolveTitle(name, realMediaType, path);
        final String realMime = FileItemUtil.resolveMime(mime, path);
        final long realSize = FileItemUtil.resolveSize(size, mediaType, path);

        return new FileItem()
                .setTitle(realTitle)
                .setMimeType(realMime)
                .setParent(parent)
                .setId(id)
                .setMediaType(realMediaType)
                .setPath(path)
                .setSize(realSize)
                .setDate(date);
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

    public static long[] getChildFiles(Context context, long parentId, int... mediaTypes) {
        StringBuilder projection = new StringBuilder();
        projection.append(MediaStore.Files.FileColumns.PARENT).append("=?");
        if (mediaTypes!= null && mediaTypes.length > 0) {
            projection.append(" AND (");
            for(int ii=0; ii<mediaTypes.length; ii++) {
                projection.append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=").append(mediaTypes[ii]);
                if (ii<mediaTypes.length-1) {
                    projection.append(" OR ");
                }
            }
            projection.append(")");
        }
        Log.d("TAG", "projection=" + projection.toString());
        Cursor c = context.getContentResolver()
                .query(MediaStore.Files.getContentUri("external"),
                        new String[]{ MediaStore.Files.FileColumns._ID },
                        projection.toString(),
                        new String[]{String.valueOf(parentId)},
                        MediaStore.Files.FileColumns.DATA);
        if (c != null) {
            long[] ids = new long[c.getCount()];
            if (c.moveToFirst()) {
                int ii=0;
                do {
                    ids[ii++] = c.getLong(0);
                } while (c.moveToNext());
            }
            c.close();
            return ids;
        }
        return new long[0];
    }

    public static List<FileItem> ls(Context context, String directory, Set<Integer> mediaTypes) {
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
            FileItem up = FileItemUtil.upDir(context, directory);
            if (up != null) {
                items.add(up);
            }
            if (c.moveToFirst()) {
                do {
                    FileItem i = MediaProviderUtil.fileItemFromCursor(c);
                    if (i != null && in(i.getMediaType(), mediaTypes)) {
                        items.add(i);
                    }
                } while (c.moveToNext());
            }
            c.close();
            return items;
        }
        return null;
    }

    static boolean in(int type, Collection<Integer> types) {
        if (types == null || types.size() == 0) {
            return true;
        }
        for (int t : types) {
            if (type == t) {
                return true;
            }
        }
        return false;
    }

    public static MediaScannerFuture addFile(Context context, String path) {
        MediaScannerFuture f = new MediaScannerFuture(path);
        MediaScannerConnection.scanFile(context, new String[]{path}, null, f);
        return f;
    }

    public static void moveFile(Context context, long id, String newDir) {
        String path = getPath(context, id);
        File f = new File(path);
        if (f.isDirectory()) {
            moveDir(context, f, newDir);
        } else {
            moveFile(context, f, newDir);
        }
        context.getContentResolver().delete(MediaStore.Files.getContentUri("external"),
                MediaStore.Files.FileColumns._ID+"=?",
                new String[]{String.valueOf(id)});
    }

    public static void moveFile(Context context, File oldFile, String newDir) {
        try {
            FileUtils.moveFileToDirectory(oldFile, new File(newDir), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void moveDir(Context context, File oldDir, String newDir) {

    }

    public static class MediaScannerFuture implements Future<Uri>, OnScanCompletedListener {
        private final String path;
        private Uri uri;
        private boolean complete = false;
        public MediaScannerFuture(String path) {
            this.path = path;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false; //STUB
        }

        @Override
        public boolean isCancelled() {
            return false; //STUB
        }

        @Override
        public boolean isDone() {
            return complete;
        }

        @Override
        public Uri get() throws InterruptedException, ExecutionException {
            try {
                return doGet(null);
            } catch (TimeoutException e) {
                throw new AssertionError(e);
            }
        }

        @Override
        public Uri get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return doGet(TimeUnit.MILLISECONDS.convert(timeout, unit));
        }

        @Override
        public synchronized void onScanCompleted(String path, Uri uri) {
            if (this.path.equals(path)) {
                this.uri = uri;
                this.complete = true;
                notifyAll();
            }
        }

        private synchronized Uri doGet(Long timeoutMs) throws InterruptedException, TimeoutException {
            if (complete) {
                return this.uri;
            }

            if (timeoutMs == null) {
                wait(0);
            } else if (timeoutMs > 0) {
                wait(timeoutMs);
            }

            if (!complete) {
                throw new TimeoutException();
            }

            return this.uri;
        }
    }

}
