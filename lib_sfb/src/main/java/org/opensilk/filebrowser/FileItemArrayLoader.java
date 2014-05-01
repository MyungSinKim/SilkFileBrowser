package org.opensilk.filebrowser;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by drew on 4/30/14.
 */
public class FileItemArrayLoader extends WrappedAsyncTaskLoader<List<FileItem>> {

    private final String directory;

    public FileItemArrayLoader(Context context, String directory) {
        super(context);
        this.directory = directory.endsWith("/") ? directory.substring(0, directory.length()-1) : directory;
    }

    @Override
    public List<FileItem> loadInBackground() {
        long parentId = MediaProviderUtil.getParentId(getContext(), directory);
        if (parentId < 0) {
            return null;
        }
        Cursor c = getContext().getContentResolver()
                .query(MediaStore.Files.getContentUri("external"),
                        MediaProviderUtil.FILES_PROJECTION,
//                        FileColumns.DATA + " like '" + directory + "%'",// and not like '" + directory+"%/%'",
//                        null,
                        FileColumns.PARENT + "=?",
                        new String[] {
                                String.valueOf(parentId),
                        },
                        FileColumns.DATA
                );
        if (c != null) {
            List<FileItem> items = new ArrayList<>();
            if (c.moveToFirst()) {
                FileItem up = FileItem.upDir(directory);
                if (up != null) {
                    items.add(up);
                }
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
