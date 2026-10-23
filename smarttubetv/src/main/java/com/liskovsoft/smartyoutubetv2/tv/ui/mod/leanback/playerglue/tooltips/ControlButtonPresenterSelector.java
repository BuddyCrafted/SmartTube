/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.liskovsoft.smartyoutubetv2.tv.ui.mod.leanback.playerglue.tooltips;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.leanback.R;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.PlaybackControlsRow;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.PaddingAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.SpacerAction;
import android.graphics.PorterDuff;
import android.widget.LinearLayout;

/**
 * Displays primary and secondary controls for a {@link PlaybackControlsRow}.
 *
 * Binds to items of type {@link Action}.
 */
public class ControlButtonPresenterSelector extends PresenterSelector {
    private final ControlButtonPresenter mPrimaryPresenter =
            new ControlButtonPresenter(R.layout.lb_control_button_primary);
    private final ControlButtonPresenter mSecondaryPresenter =
            new ControlButtonPresenter(R.layout.lb_control_button_secondary);
    private final Presenter[] mPresenters = new Presenter[]{mPrimaryPresenter};

    public ControlButtonPresenterSelector(boolean tooltipsEnabled) {
        mPrimaryPresenter.tooltipsEnabled = mSecondaryPresenter.tooltipsEnabled = tooltipsEnabled;
    }

    /**
     * Returns the presenter for primary controls.
     */
    public Presenter getPrimaryPresenter() {
        return mPrimaryPresenter;
    }

    /**
     * Returns the presenter for secondary controls.
     */
    public Presenter getSecondaryPresenter() {
        return mSecondaryPresenter;
    }

    /**
     * Always returns the presenter for primary controls.
     */
    @Override
    public Presenter getPresenter(Object item) {
        return mPrimaryPresenter;
    }

    @Override
    public Presenter[] getPresenters() {
        return mPresenters;
    }

    static class ActionViewHolder extends Presenter.ViewHolder {
        ImageView mIcon;
        TextView mLabel;
        View mFocusableView;

        boolean mPlay;

        public ActionViewHolder(View view) {
            super(view);
            mIcon = (ImageView) view.findViewById(R.id.icon);
            mLabel = (TextView) view.findViewById(R.id.label);
            mFocusableView = view.findViewById(R.id.button);
        }

        /** WiiOS: dark icons on the white buttons (the focused one, and play), white ones elsewhere. */
        void applyTint() {
            if (mIcon == null) {
                return;
            }
            if (mPlay || mFocusableView.isFocused()) {
                mIcon.setColorFilter(0xFF0F0F0F, PorterDuff.Mode.SRC_IN);
            } else {
                mIcon.clearColorFilter();   // keeps the highlight colour of a liked or switched-on button
            }
        }
    }

    public static class ControlButtonPresenter extends Presenter {
        private int mLayoutResourceId;
        private boolean tooltipsEnabled;

        ControlButtonPresenter(int layoutResourceId) {
            mLayoutResourceId = layoutResourceId;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(mLayoutResourceId, parent, false);
            ActionViewHolder vh = new ActionViewHolder(v);
            View.OnFocusChangeListener old = vh.mFocusableView.getOnFocusChangeListener();
            vh.mFocusableView.setOnFocusChangeListener((view, hasFocus) -> {
                if (old != null) {
                    old.onFocusChange(view, hasFocus);
                }
                vh.applyTint();
            });
            return vh;
        }

        // Used inside: com.liskovsoft.smartyoutubetv2.tv.ui.mod.leanback.playerglue.tweaks.ControlBarPresenter.ViewHolder.bindControlToAction()
        // Restore focus: com.liskovsoft.smartyoutubetv2.tv.ui.mod.leanback.playerglue.tweaks.ControlBar.onRequestFocusInDescendants()
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, Object item) {
            Action action = (Action) item;
            ActionViewHolder vh = (ActionViewHolder) viewHolder;

            styleForYouTube(vh, action);
            vh.mIcon.setImageDrawable(action.getIcon());
            if (action instanceof PaddingAction) {
                int padding = ((PaddingAction) action).getPadding();
                if (padding > 0) {
                    vh.mIcon.setPadding(padding, padding, padding, padding);
                }
            }
            if (vh.mLabel != null) {
                if (action.getIcon() == null) {
                    vh.mLabel.setText(action.getLabel1());
                } else {
                    vh.mLabel.setText(null);
                }
            }
            CharSequence contentDescription = TextUtils.isEmpty(action.getLabel2())
                    ? action.getLabel1() : action.getLabel2();
            if (!TextUtils.equals(vh.mFocusableView.getContentDescription(), contentDescription)) {
                vh.mFocusableView.setContentDescription(contentDescription);
                vh.mFocusableView.sendAccessibilityEvent(
                        AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);

                // MOD: enable control tooltips
                if (tooltipsEnabled) {
                    TooltipCompatHandler.setTooltipText(vh.mFocusableView, action.getLabel1());
                }
            }
        }

        /**
         * WiiOS: a spacer stretches and can't be focused; play is the big white button, where focus starts.
         */
        private void styleForYouTube(ActionViewHolder vh, Action action) {
            boolean spacer = action instanceof SpacerAction;
            ViewGroup.LayoutParams lp = vh.view.getLayoutParams();
            if (lp instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) lp;
                llp.width = spacer ? 0 : ViewGroup.LayoutParams.WRAP_CONTENT;
                llp.weight = spacer ? 1f : 0f;
                vh.view.setLayoutParams(llp);
            }
            vh.mFocusableView.setFocusable(!spacer);
            vh.mFocusableView.setVisibility(spacer ? View.INVISIBLE : View.VISIBLE);
            vh.mIcon.setVisibility(spacer ? View.INVISIBLE : View.VISIBLE);

            boolean play = action instanceof PlaybackControlsRow.PlayPauseAction;
            android.content.res.Resources res = vh.view.getResources();
            int size = res.getDimensionPixelSize(play ? com.liskovsoft.smartyoutubetv2.tv.R.dimen.yt_play_size : com.liskovsoft.smartyoutubetv2.tv.R.dimen.yt_button_size);
            int icon = res.getDimensionPixelSize(play ? com.liskovsoft.smartyoutubetv2.tv.R.dimen.yt_play_icon_size : com.liskovsoft.smartyoutubetv2.tv.R.dimen.yt_icon_size);
            ViewGroup.LayoutParams blp = vh.mFocusableView.getLayoutParams();
            if (blp.width != size) {
                blp.width = blp.height = size;
                vh.mFocusableView.setLayoutParams(blp);
                ViewGroup.LayoutParams ilp = vh.mIcon.getLayoutParams();
                ilp.width = ilp.height = icon;
                vh.mIcon.setLayoutParams(ilp);
            }
            if (play != vh.mPlay) {
                vh.mFocusableView.setBackgroundResource(play ? com.liskovsoft.smartyoutubetv2.tv.R.drawable.yt_play_bg : com.liskovsoft.smartyoutubetv2.tv.R.drawable.yt_button_bg);
            }
            vh.mPlay = play;
            vh.view.setTag(com.liskovsoft.smartyoutubetv2.tv.R.id.yt_default_focus, play ? Boolean.TRUE : null);
            vh.applyTint();
        }

        @Override
        public void onUnbindViewHolder(ViewHolder viewHolder) {
            ActionViewHolder vh = (ActionViewHolder) viewHolder;
            vh.mIcon.setImageDrawable(null);
            if (vh.mLabel != null) {
                vh.mLabel.setText(null);
            }
            vh.mFocusableView.setContentDescription(null);
        }

        @Override
        public void setOnClickListener(ViewHolder viewHolder,
                                       View.OnClickListener listener) {
            ((ActionViewHolder) viewHolder).mFocusableView.setOnClickListener(listener);
        }

        public void setOnLongClickListener(ViewHolder viewHolder,
                                       View.OnLongClickListener listener) {
            ((ActionViewHolder) viewHolder).mFocusableView.setOnLongClickListener(listener);
        }
    }
}
