/*
 * Copyright (C) 2014 OpenSilk Productions LLC
 *
 * This file is part of QwikBeam
 *
 * QwikBeam is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * QwikBeam is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QwikBeam.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.opensilk.filebrowser;

import java.io.File;

/**
 * Created by drew on 5/22/14.
 */
public class FBUtil {
    /**
     * Check existence of file and increment name as needed.
     */
    public static File incrementFileName(String directory, String fullName) {
        int ii = 1;
        File dest;
        String ext, name, dot = ".";

        ext = getFileExtension(fullName);
        if ("".equals(ext)) {
            dot = "";
            name = fullName;
        } else {
            name = fullName.substring(0, fullName.lastIndexOf(ext)-1);
        }
        dest = new File(directory, fullName);

        while (dest.exists()) {
            dest = new File(directory, name+"-"+String.valueOf(ii)+dot+ext);
            ii++;
        }
        return dest;
    }

    /**
     * Gets extension from file
     * @param f
     * @return
     */
    public static String getFileExtension(File f) {
        return getFileExtension(f.getName());
    }

    /**
     *
     * @param name
     * @return
     */
    public static String getFileExtension(String name) {
        String ext;
        int lastDot = name.lastIndexOf('.');
        int secondLastDot = name.lastIndexOf('.', lastDot-1);
        if (secondLastDot > 0 ) { // Double extension
            ext = name.substring(secondLastDot + 1);
            if (!ext.startsWith("tar")) {
                ext = name.substring(lastDot + 1);
            }
        } else if (lastDot > 0) { // Single extension
            ext = name.substring(lastDot + 1);
        } else { // No extension
            ext = "";
        }
        return ext;
    }
}
