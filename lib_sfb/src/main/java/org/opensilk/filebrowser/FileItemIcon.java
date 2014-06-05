package org.opensilk.filebrowser;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by drew on 4/30/14.
 */
public class FileItemIcon extends TextView {

    public FileItemIcon(Context context) {
        super(context);
    }

    public FileItemIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FileItemIcon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Initializes the view
     * @param mediaType {@link FileItem.MediaType}
     * @param mimeType fallback file type, used for NONE type
     */
    public void forType(int mediaType, String mimeType) {

        switch (mediaType) {
            case FileItem.MediaType.AUDIO:
                setText("\u266A");
                setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case FileItem.MediaType.IMAGE:
                setText("IMG");
                setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
                break;
            case FileItem.MediaType.VIDEO:
                setText("VID");
                setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case FileItem.MediaType.DIRECTORY:
                setText("DIR");
                setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case FileItem.MediaType.UP_DIRECTORY:
                setText("UP");
                setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case FileItem.MediaType.NONE:
                switch (mimeType) {
                    //TODO more
                    default:
                        setText("!");
                        setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                        break;
                }
            default:
                break;
        }
    }

}
