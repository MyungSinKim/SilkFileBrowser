/*
 * Copyright (c) 2014 OpenSilk Productions LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.opensilk.filebrowser;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by drew on 6/4/14.
 */
public class FileBrowserArgs implements Parcelable {

    private String path;

    public FileBrowserArgs() {
    }

    public static FileBrowserArgs copy(FileBrowserArgs old) {
        return new FileBrowserArgs()
                .setPath(old.getPath());
    }

    public FileBrowserArgs setPath(String path) {
        this.path = path;
        return this;
    }

    public String getPath() {
        return path;
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
        dest.writeString(path);
    }

    private FileBrowserArgs(Parcel in) {
        path = in.readString();
    }

    public static final Creator<FileBrowserArgs> CREATOR = new Creator<FileBrowserArgs>() {
        @Override
        public FileBrowserArgs createFromParcel(Parcel source) {
            return new FileBrowserArgs(source);
        }

        @Override
        public FileBrowserArgs[] newArray(int size) {
            return new FileBrowserArgs[size];
        }
    };

}
