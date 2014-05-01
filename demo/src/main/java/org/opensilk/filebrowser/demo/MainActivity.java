package org.opensilk.filebrowser.demo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.EditText;

import org.opensilk.filebrowser.BrowserFragment;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends FragmentActivity {

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
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, BrowserFragment.newInstance(text.getText().toString()), "file_browser")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        // onBackPressed will not pop stack for childFragmentManager
        Fragment f = getSupportFragmentManager().findFragmentByTag("file_browser");
        if (f != null) {
            if (f.getChildFragmentManager().getBackStackEntryCount() > 0) {
                f.getChildFragmentManager().popBackStackImmediate();
                return;
            }
        }
        super.onBackPressed();
    }
}
