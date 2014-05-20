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
        return MediaProviderUtil.ls(getContext(), directory);
    }

}
