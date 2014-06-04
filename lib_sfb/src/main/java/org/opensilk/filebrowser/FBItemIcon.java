package org.opensilk.filebrowser;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by drew on 4/30/14.
 */
public class FBItemIcon extends TextView {

    public FBItemIcon(Context context) {
        super(context);
    }

    public FBItemIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FBItemIcon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Initializes the view
     * @param mediaType {@link FBItem.MediaType}
     * @param mimeType fallback file type, used for NONE type
     */
    public void forType(int mediaType, String mimeType) {

        switch (mediaType) {
            case FBItem.MediaType.AUDIO:
                setText("\u266A");
                setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case FBItem.MediaType.IMAGE:
                setText("IMG");
                setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
                break;
            case FBItem.MediaType.VIDEO:
                setText("VID");
                setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case FBItem.MediaType.DIRECTORY:
                setText("DIR");
                setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case FBItem.MediaType.UP_DIRECTORY:
                setText("UP");
                setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                break;
            case FBItem.MediaType.NONE:
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
