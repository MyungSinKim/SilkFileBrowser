package org.opensilk.filebrowser;

import android.content.Context;

import java.util.List;
import java.util.Set;

/**
 * Created by drew on 4/30/14.
 */
public class FileItemArrayLoader extends WrappedAsyncTaskLoader<List<FileItem>> {

    private final String directory;
    private final Set<Integer> mediaTypes;

    public FileItemArrayLoader(Context context, FileBrowserArgs args) {
        super(context);
        final String path = args.getPath();
        this.directory = path.endsWith("/") ? path.substring(0, path.length()-1) : path;
        this.mediaTypes = args.getMediaTypes();
    }

    @Override
    public List<FileItem> loadInBackground() {
        return MediaProviderUtil.ls(getContext(), directory, mediaTypes);
    }

}
