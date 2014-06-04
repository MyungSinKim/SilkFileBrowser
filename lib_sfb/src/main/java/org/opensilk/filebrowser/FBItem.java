package org.opensilk.filebrowser;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore.Files.FileColumns;
import android.text.TextUtils;

/**
 * Created by drew on 4/30/14.
 */
public class FBItem implements Parcelable {

    public static class MediaType {
        public static final int NONE = FileColumns.MEDIA_TYPE_NONE;
        public static final int IMAGE = FileColumns.MEDIA_TYPE_IMAGE;
        public static final int AUDIO = FileColumns.MEDIA_TYPE_AUDIO;
        public static final int VIDEO = FileColumns.MEDIA_TYPE_VIDEO;
        public static final int DIRECTORY    = 0x00000100;
        public static final int UP_DIRECTORY = 0x00000200;
        public static String toString(int mediaType) {
            switch (mediaType) {
                case NONE:
                    return "NONE";
                case IMAGE:
                    return "IMAGE";
                case AUDIO:
                    return "AUDIO";
                case VIDEO:
                    return "VIDEO";
                case DIRECTORY:
                    return "DIRECTORY";
                case UP_DIRECTORY:
                    return "UP_DIRECTORY";
            }
            return null;
        }
    }

    private String title;
    private String mimeType;
    private long parent;
    private long id;
    private int mediaType;
    private String path;
    private long size;
    private long date;

    public FBItem() {

    }

    public FBItem setTitle(String title) {
        this.title = title;
        return this;
    }

    public FBItem setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public FBItem setParent(long parent) {
        this.parent = parent;
        return this;
    }

    public FBItem setId(long id) {
        this.id = id;
        return this;
    }

    public FBItem setMediaType(int mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    public FBItem setPath(String path) {
        this.path = path;
        return this;
    }

    public FBItem setSize(long size) {
        this.size = size;
        return this;
    }

    public FBItem setDate(long date) {
        this.date = date;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getMimeType() {
        return mimeType;
    }


    public long getParent() {
        return parent;
    }

    public long getId() {
        return id;
    }

    public int getMediaType() {
        return mediaType;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public long getDate() {
        return date;
    }

    @Override
    public String toString() {
//        return String.format(Locale.US, "title=%s,mime=%s,parent=%d,id=%d,mediaType=%s,path=%s",
//                title, mimeType, parent, id, MediaType.toString(mediaType), path);
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (!(o instanceof FBItem)) return false;
        FBItem i = (FBItem) o;
        if (!TextUtils.equals(i.title, this.title)) return false;
        if (!TextUtils.equals(i.mimeType, this.mimeType)) return false;
        if (i.parent != this.parent) return false;
        if (i.id != this.id) return false;
        if (i.mediaType != this.mediaType) return false;
        if (!TextUtils.equals(i.path, this.path)) return false;
        if (i.size != this.size) return false;
        if (i.date != this.date) return false;
        return true;
    }

    /*
     * Implement Parcelable Interface
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(mimeType);
        dest.writeLong(parent);
        dest.writeLong(id);
        dest.writeInt(mediaType);
        dest.writeString(path);
        dest.writeLong(size);
        dest.writeLong(date);
    }

    private FBItem(Parcel in) {
        this.title = in.readString();
        this.mimeType = in.readString();
        this.parent = in.readLong();
        this.id = in.readLong();
        this.mediaType = in.readInt();
        this.path = in.readString();
        this.size = in.readLong();
        this.date = in.readLong();
    }

    public static final Parcelable.Creator<FBItem> CREATOR = new Creator<FBItem>() {
        @Override
        public FBItem createFromParcel(Parcel source) {
            return new FBItem(source);
        }

        @Override
        public FBItem[] newArray(int size) {
            return new FBItem[size];
        }
    };

}
