package org.opensilk.filebrowser;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment container for the file lists. FileListFragment will use this fragments
 * ChildFragmentManager to manage list add/remove on tree traversal.
 *
 * Created by drew on 4/30/14.
 */
public class BrowserFragment extends Fragment {

    /**
     * Creates new instance
     * @param path directory path to pass to initial {@link org.opensilk.filebrowser.FileListFragment}
     * @return
     */
    public static BrowserFragment newInstance(String path) {
        BrowserFragment f = new BrowserFragment();
        Bundle b = new Bundle(1);
        b.putString("path", path);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fb__browser_container, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null) {
            // Load the initial file list
//            FileListFragment f = FileListFragment.newInstance(getArguments().getString("path"));
            FBCardListFragment f = FBCardListFragment.newInstance(getArguments().getString("path"));
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.browser_container, f)
                    .commit();
        }
    }
}
