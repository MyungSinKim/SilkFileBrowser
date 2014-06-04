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

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.ButterKnife;
import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by drew on 6/4/14.
 */
public class FBCardListItem extends Card {

    private final FileItem file;

    public FBCardListItem(Context context, FileItem f) {
        this(context, f, R.layout.fb__card_list_item_inner);
    }

    public FBCardListItem(Context context, FileItem f, int innerLayout) {
        super(context, innerLayout);
        file = f;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        FileIcon icon = ButterKnife.findById(view, R.id.fb__item_icon);
        if (icon != null) {
            icon.forType(file.getMediaType(), file.getMimeType());
        }
        TextView title = ButterKnife.findById(view, R.id.fb__item_title);
        if (title != null) {
            if (!TextUtils.isEmpty(file.getTitle())) {
                title.setText(file.getTitle());
            } else {
                title.setVisibility(View.INVISIBLE);
            }
        }
        TextView info = ButterKnife.findById(view, R.id.fb__item_info);
        if (info != null) {
            if (file.getSize() >= 0) {
                info.setText(FBUtil.prettyPrintSize(file.getSize()));
            } else {
                info.setVisibility(View.INVISIBLE);
            }
        }
        TextView date = ButterKnife.findById(view, R.id.fb__item_date);
        if (date != null) {
            if (file.getDate() > 0) {
                date.setText(FBUtil.prettyPrintDate(getContext(), file.getDate()));
            } else {
                date.setVisibility(View.INVISIBLE);
            }
        }
    }

}
