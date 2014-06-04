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
public class FBItemArrayLoader extends WrappedAsyncTaskLoader<List<FBItem>> {

    private final String directory;

    public FBItemArrayLoader(Context context, FBBrowserArgs args) {
        super(context);
        final String path = args.getPath();
        this.directory = path.endsWith("/") ? path.substring(0, path.length()-1) : path;
    }

    @Override
    public List<FBItem> loadInBackground() {
        return MediaProviderUtil.ls(getContext(), directory);
    }

}
