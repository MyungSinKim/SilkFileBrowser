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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by drew on 6/4/14.
 */
public class FBItemCard extends Card {

    private final FBItem file;
    private boolean selected;

    private FBItemIcon displayIcon;
    private ImageView checkedIcon;

    public FBItemCard(Context context, FBItem f) {
        this(context, f, R.layout.fb__card_list_item_inner);
    }

    public FBItemCard(Context context, FBItem f, int innerLayout) {
        super(context, innerLayout);
        file = f;
        selected = false;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        displayIcon = ButterKnife.findById(view, R.id.fb__item_icon);
        if (displayIcon != null) {
            displayIcon.forType(file.getMediaType(), file.getMimeType());
            if (selected) {
                displayIcon.setVisibility(View.GONE);
            } else {
                displayIcon.setVisibility(View.VISIBLE);
            }
        }
        checkedIcon = ButterKnife.findById(view, R.id.fb__item_checked);
        if (checkedIcon != null) {
            if (selected) {
                checkedIcon.setVisibility(View.VISIBLE);
            } else {
                checkedIcon.setVisibility(View.GONE);
            }
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
                info.setText(FBItemUtil.prettyPrintSize(getContext(), file.getSize(), file.getMediaType()));
            } else {
                info.setVisibility(View.INVISIBLE);
            }
        }
        TextView date = ButterKnife.findById(view, R.id.fb__item_date);
        if (date != null) {
            if (file.getDate() > 0) {
                date.setText(FBItemUtil.prettyPrintDate(getContext(), file.getDate()));
            } else {
                date.setVisibility(View.INVISIBLE);
            }
        }
    }

    public FBItem getFile() {
        return file;
    }

    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            flipit();
        }
        this.selected = selected;
    }

    public boolean getSelected() {
        return selected;
    }

    private static final int TRANSITION_DURATION = 200;
    private Interpolator accelerator = new AccelerateInterpolator();
    private Interpolator decelerator = new DecelerateInterpolator();
    private void flipit() {
        if (checkedIcon == null || displayIcon == null) {
            return;
        }
        final View visibleLayout;
        final View invisibleLayout;
        if (checkedIcon.getVisibility() == View.GONE) {
            visibleLayout = displayIcon;
            invisibleLayout = checkedIcon;
        } else {
            visibleLayout = checkedIcon;
            invisibleLayout = displayIcon;
        }
        ObjectAnimator visToInvis = ObjectAnimator.ofFloat(visibleLayout, "rotationX", 0f, 90f);
        visToInvis.setDuration(TRANSITION_DURATION);
        visToInvis.setInterpolator(accelerator);
        final ObjectAnimator invisToVis = ObjectAnimator.ofFloat(invisibleLayout, "rotationX",
                -90f, 0f);
        invisToVis.setDuration(TRANSITION_DURATION);
        invisToVis.setInterpolator(decelerator);
        visToInvis.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator anim) {
                visibleLayout.setVisibility(View.GONE);
                invisToVis.start();
                invisibleLayout.setVisibility(View.VISIBLE);
            }
        });
        visToInvis.start();
    }
}
