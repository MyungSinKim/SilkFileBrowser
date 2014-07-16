package org.opensilk.filebrowser.demo;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;

import org.opensilk.filebrowser.FileItem;

import java.util.ArrayList;

/**
 * Created by drew on 6/4/14.
 */
public class SelectedListFragment extends ListFragment {

    protected ArrayAdapter<FileItem> mAdapter;

    public static SelectedListFragment newInstance(ArrayList<FileItem> l) {
        SelectedListFragment f = new SelectedListFragment();
        Bundle b = new Bundle();
        b.putParcelableArrayList("items", l);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<FileItem> l = getArguments().getParcelableArrayList("items");
        mAdapter = new ArrayAdapter<FileItem>(getActivity(), android.R.layout.simple_list_item_1, l);
        setListAdapter(mAdapter);
    }

}
