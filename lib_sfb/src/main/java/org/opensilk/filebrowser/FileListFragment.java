package org.opensilk.filebrowser;


import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * Created by drew on 4/13/14.
 */
public class FileListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<FileItem>> {

    public static FileListFragment newInstance(String path) {
        FileListFragment f = new FileListFragment();
        Bundle b = new Bundle(1);
        b.putString("path", path);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, getArguments(), this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        FileItem item = (FileItem) getListAdapter().getItem(position);
        if (item.getMediaType() == FileItem.MediaType.UP_DIRECTORY) {
            if (getParentFragment().getChildFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragment().getChildFragmentManager().popBackStackImmediate();
            } else {
                getParentFragment().getChildFragmentManager().beginTransaction()
                        .replace(R.id.browser_container, newInstance(item.getPath()))
                        // no backstack
                        .commit();
            }
        } else if (item.getMediaType() == FileItem.MediaType.DIRECTORY) {
            getParentFragment().getChildFragmentManager().beginTransaction()
                    .replace(R.id.browser_container, newInstance(item.getPath()))
                    .addToBackStack(null)
                    .commit();
        } else {
            //TODO
        }
    }

    /*
     * Loader callbacks
     */

    @Override
    public Loader<List<FileItem>> onCreateLoader(int id, Bundle args) {
        return new FileItemArrayLoader(getActivity(), args.getString("path"));
    }

    @Override
    public void onLoadFinished(Loader<List<FileItem>> loader, List<FileItem> data) {
        ArrayAdapter<FileItem> adapter = new FileItemArrayAdapter(getActivity());
        if (data != null) {
            adapter.addAll(data);
        }
        setListAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<List<FileItem>> loader) {
        setListAdapter(null);
    }
}
