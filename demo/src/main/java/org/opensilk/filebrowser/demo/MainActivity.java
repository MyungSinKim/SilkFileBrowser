package org.opensilk.filebrowser.demo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;

import org.opensilk.filebrowser.FileBrowserArgs;
import org.opensilk.filebrowser.FileItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends FragmentActivity implements FileSelectionListener {

    @InjectView(R.id.edit_text)
    EditText text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        text.setText(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Environment.DIRECTORY_MUSIC);
    }

    @OnClick(R.id.submit)
    protected void launchBrowser() {
        FileBrowserFragment f = FileBrowserFragment.newInstance(new FileBrowserArgs().setPath(text.getText().toString()));
        f.setSelectionListener(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, f, "file_browser")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        // i want to handle up navigation better but i cant think of a way yet
        // for now this work ok
        Fragment f = getSupportFragmentManager().findFragmentByTag("file_browser");
        if (f != null && f.isResumed() && (f instanceof FileBrowser)) {
            FileBrowser b = (FileBrowser) f;
            if (b.popDirStack()) {
                return;
            }
        }
        super.onBackPressed();
    }

    /*
     * Implement IFBSelectionListener
     */

    @Override
    public void onFileItemsSelected(List<FileItem> l) {
        ArrayList<FileItem> al = new ArrayList<>(l);
        SelectedListFragment f = SelectedListFragment.newInstance(al);
        getSupportFragmentManager().popBackStackImmediate();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, f)
                .addToBackStack(null)
                .commit();
    }
}
