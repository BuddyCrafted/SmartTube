package com.liskovsoft.smartyoutubetv2.tv.ui.playback.other;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.leanback.media.PlaybackGlueHost;
import androidx.leanback.media.PlaybackTransportControlGlue;
import androidx.leanback.media.PlayerAdapter;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ObjectAdapter;
import androidx.leanback.widget.PlaybackControlsRow;
import androidx.leanback.widget.PlaybackControlsRow.MultiAction;
import androidx.leanback.widget.PlaybackRowPresenter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.smartyoutubetv2.common.prefs.GeneralData;
import com.liskovsoft.smartyoutubetv2.common.prefs.PlayerTweaksData;
import com.liskovsoft.smartyoutubetv2.tv.R;
import com.liskovsoft.smartyoutubetv2.tv.ui.mod.leanback.playerglue.tweaks.MaxControlsVideoPlayerGlue;
import com.liskovsoft.smartyoutubetv2.tv.ui.mod.leanback.playerglue.tweaks.PlaybackTransportRowPresenter;
import com.liskovsoft.smartyoutubetv2.tv.ui.mod.leanback.widget.OnActionLongClickedListener;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.AFRAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.MoreAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.SpacerAction;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.UiOptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.AppDialogPresenter;
import android.os.Handler;
import android.os.Looper;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.ActionHelpers;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.ChannelAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.ChatAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.ClosedCaptioningAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.ContentBlockAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.FlipAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.HighQualityAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.RotateAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.ScreenDimmingAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.SeekIntervalAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.ShareAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.SoundOffAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.VideoInfoAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.PipAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.PlaybackQueueAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.PlaylistAddAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.PlaybackModeAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.SearchAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.SubscribeAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.TwoStateAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.ThumbsDownAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.ThumbsUpAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.VideoSpeedAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.VideoStatsAction;
import com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions.VideoZoomAction;
import com.liskovsoft.smartyoutubetv2.tv.util.ViewUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Manages customizing the actions in the {@link PlaybackControlsRow}. Adds and manages the
 * following actions to the primary and secondary controls:
 *
 * <ul>
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.RepeatAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.ThumbsDownAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.ThumbsUpAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.SkipPreviousAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.SkipNextAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.FastForwardAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.RewindAction}
 * </ul>
 *
 * Note that the superclass, {@link PlaybackTransportControlGlue}, manages the playback controls
 * row.
 */
public class VideoPlayerGlue extends MaxControlsVideoPlayerGlue<PlayerAdapter> implements OnActionLongClickedListener {
    private static final long TEN_SECONDS = TimeUnit.SECONDS.toMillis(10);
    private static final String TAG = VideoPlayerGlue.class.getSimpleName();
    private final PlaybackControlsRow.SkipPreviousAction mSkipPreviousAction;
    private final PlaybackControlsRow.SkipNextAction mSkipNextAction;
    private final PlaybackControlsRow.FastForwardAction mFastForwardAction;
    private final PlaybackControlsRow.RewindAction mRewindAction;
    private final Map<Integer, Action> mActions = new HashMap<>();
    private final OnActionClickedListener mActionListener;
    private final PlayerTweaksData mPlayerTweaksData;
    private final GeneralData mGeneralData;
    private int mPreviousAction = KeyEvent.ACTION_UP;

    public VideoPlayerGlue(
            Context context,
            PlayerAdapter playerAdapter,
            OnActionClickedListener actionListener) {
        super(context, playerAdapter);

        mPlayerTweaksData = PlayerTweaksData.instance(getContext());
        mGeneralData = GeneralData.instance(getContext());

        mActionListener = actionListener;

        mSkipPreviousAction = new PlaybackControlsRow.SkipPreviousAction(context);
        mSkipNextAction = new PlaybackControlsRow.SkipNextAction(context);
        mFastForwardAction = new PlaybackControlsRow.FastForwardAction(context);
        mRewindAction = new PlaybackControlsRow.RewindAction(context);

        ThumbsUpAction thumbsUpAction = new ThumbsUpAction(context);
        ThumbsDownAction thumbsDownAction = new ThumbsDownAction(context);
        thumbsUpAction.setBoundAction(thumbsDownAction);
        thumbsDownAction.setBoundAction(thumbsUpAction);
        putAction(thumbsUpAction);
        putAction(thumbsDownAction);

        putAction(new HighQualityAction(context));
        putAction(new PlaybackModeAction(context));
        putAction(new ChannelAction(context));
        putAction(new ClosedCaptioningAction(context));
        putAction(new PlaylistAddAction(context));
        putAction(new SubscribeAction(context));
        putAction(new VideoInfoAction(context));
        putAction(new VideoSpeedAction(context));
        putAction(new VideoStatsAction(context));
        putAction(new SearchAction(context));
        putAction(new PipAction(context));
        putAction(new AFRAction(context));
        putAction(new ScreenDimmingAction(context));
        putAction(new ChatAction(context));
        putAction(new PlaybackQueueAction(context));
        putAction(new ContentBlockAction(context));
        putAction(new ShareAction(context));
        putAction(new SeekIntervalAction(context));
        putAction(new VideoZoomAction(context));
        putAction(new RotateAction(context));
        putAction(new FlipAction(context));
        putAction(new SoundOffAction(context));

        // WiiOS: the gear and the row's gaps aren't SmartTube actions of their own.
        mMoreAction = new MoreAction(context);
        mSpacerLeft = new SpacerAction(R.id.action_spacer_left);
        mSpacerRight = new SpacerAction(R.id.action_spacer_right);
    }

    private final Action mMoreAction, mSpacerLeft, mSpacerRight;

    /** WiiOS: what the gear lists, in this order (each opens or toggles exactly as its old button did). */
    private static final int[] MORE_IDS = {
            R.id.lb_control_high_quality, R.id.action_video_speed, R.id.action_repeat,
            R.id.action_playback_queue, R.id.action_content_block, R.id.action_video_stats,
            R.id.action_search, R.id.action_share, R.id.action_pip, R.id.action_screen_dimming,
            R.id.action_seek_interval, R.id.action_video_zoom, R.id.action_rotate, R.id.action_flip,
            R.id.action_sound_off, R.id.action_afr};

    /** WiiOS: the gear's list of every player feature that isn't in the row. */
    private void showMoreMenu() {
        AppDialogPresenter dialog = AppDialogPresenter.instance(getContext());
        addMoreItem(dialog, mSkipPreviousAction);
        addMoreItem(dialog, mSkipNextAction);
        for (int id : MORE_IDS) {
            addMoreItem(dialog, mActions.get(id));
        }
        dialog.showDialog(getContext().getString(R.string.wiios_more));
    }

    private void addMoreItem(AppDialogPresenter dialog, Action action) {
        if (action == null) {
            return;
        }
        CharSequence label = action.getLabel1() != null ? action.getLabel1() : action.getLabel2();
        if (label == null) {
            return;
        }
        dialog.appendSingleButton(UiOptionItem.from(label, item -> {
            dialog.closeDialog();
            // After the list has gone, so a feature that opens its own list (quality, speed) can show it.
            new Handler(Looper.getMainLooper()).post(() -> onActionClicked(action));
        }));
    }

    /**
     * WiiOS: one row laid out like YouTube's own TV player. Channel, description and subscribe on the left,
     * play in the middle, like, dislike, comments, save, captions and the gear (everything else) on the right.
     */
    @Override
    protected void onCreatePrimaryActions(ArrayObjectAdapter adapter) {
        addIfPresent(adapter, R.id.action_channel);
        addIfPresent(adapter, R.id.action_info);
        addIfPresent(adapter, R.id.action_subscribe);
        adapter.add(mSpacerLeft);
        super.onCreatePrimaryActions(adapter);   // play / pause
        adapter.add(mSpacerRight);
        addIfPresent(adapter, R.id.action_thumbs_up);
        addIfPresent(adapter, R.id.action_thumbs_down);
        addIfPresent(adapter, R.id.action_chat);   // comments (live chat on a live stream)
        addIfPresent(adapter, R.id.action_playlist_add);
        addIfPresent(adapter, R.id.lb_control_closed_captioning);
        adapter.add(mMoreAction);
    }

    private void addIfPresent(ArrayObjectAdapter adapter, int id) {
        Action action = mActions.get(id);
        if (action != null) {
            adapter.add(action);
        }
    }

    @Override
    protected void onCreateSecondaryActions(ArrayObjectAdapter adapter) {
        // WiiOS: no second row; its buttons are in the gear.
        super.onCreateSecondaryActions(adapter);
    }

    @Override
    protected PlaybackRowPresenter onCreateRowPresenter() {
        PlaybackRowPresenter rowPresenter = super.onCreateRowPresenter();

        ((PlaybackTransportRowPresenter) rowPresenter).setOnActionLongClickedListener(this);

        return rowPresenter;
    }

    @Override
    public void onActionClicked(Action action) {
        if (!dispatchAction(action)) {
            // Super class handles play/pause and delegates to abstract methods next()/previous().
            super.onActionClicked(action);
        }
    }

    @Override
    public boolean onActionLongClicked(Action action) {
        return dispatchLongClickAction(action);
    }

    @Override
    public void play() {
        super.play();
    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void next() {
        mActionListener.onNext();
    }

    @Override
    public void previous() {
        mActionListener.onPrevious();
    }

    public void togglePlayback() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    /** Skips backwards 10 seconds. */
    public void rewind() {
        long newPosition = getCurrentPosition() - TEN_SECONDS;
        newPosition = (newPosition < 0) ? 0 : newPosition;
        getPlayerAdapter().seekTo(newPosition);
    }

    /** Skips forward 10 seconds. */
    public void fastForward() {
        if (getDuration() > -1) {
            long newPosition = getCurrentPosition() + TEN_SECONDS;
            newPosition = Math.min(newPosition, getDuration());
            getPlayerAdapter().seekTo(newPosition);
        }
    }

    public int getButtonState(int buttonId) {
        return getActionIndex(mActions.get(buttonId));
    }

    public void setButtonState(int buttonId, int buttonState) {
        setActionIndex(mActions.get(buttonId), buttonState);
    }

    public void setChannelIcon(String iconUrl) {
        ChannelAction channelAction = (ChannelAction) mActions.get(R.id.action_channel);

        if (channelAction == null) {
            return;
        }

        if (iconUrl == null) {
            channelAction.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.action_channel));
            invalidateUi(channelAction);
            return;
        }

        Drawable originIcon = channelAction.getIcon();
        Glide.with(getContext())
                .load(iconUrl)
                .apply(ViewUtil.glideOptions())
                .circleCrop() // resize image
                .into(new SimpleTarget<Drawable>(originIcon.getIntrinsicWidth(), originIcon.getIntrinsicHeight()) {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        channelAction.setIcon(resource);
                        channelAction.setPadding(3);
                        invalidateUi(channelAction);
                    }
                });
    }

    public void setNextTitle(CharSequence title) {
        mSkipNextAction.setLabel1(title != null ? title : getContext().getString(R.string.lb_playback_controls_skip_next));
        invalidateUi(mSkipNextAction);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (!isSingleKeyDown(event.getAction())) {
            return false;
        }

        boolean handled = mActionListener.onKeyDown(keyCode);

        if (!handled) {
            Action action = findAction(keyCode);

            handled = dispatchAction(action);
        }

        // Ignore result to give a chance to handle this event in
        // com.liskovsoft.smartyoutubetv2.tv.ui.mod.leanback.playerglue.maxcontrols.PlaybackTransportRowPresenter.ViewHolder
        return handled || super.onKey(v, keyCode, event);
    }

    /**
     * Fixing sticky key press? <br/>
     * Notify key down only when there are paired action available.
     */
    private boolean isSingleKeyDown(int action) {
        boolean result = action == KeyEvent.ACTION_DOWN && mPreviousAction == KeyEvent.ACTION_UP;
        mPreviousAction = action;
        return result;
    }

    private boolean dispatchAction(Action action) {
        if (action == null) {
            return false;
        }

        if (checkShortActionDisabled(action)) {
            return true;
        }

        boolean handled = false;

        if (action == mMoreAction) {
            showMoreMenu();
            return true;
        }
        if (action == mSpacerLeft || action == mSpacerRight) {
            return true;
        }

        // Primary actions are handled manually.
        if (action == mRewindAction) {
            rewind();
            handled = true;
        } else if (action == mFastForwardAction) {
            fastForward();
            handled = true;
        } else if (mActions.containsKey((int) action.getId())) {
            mActionListener.onAction((int) action.getId(), getActionIndex(action));
            handled = true;
        }

        if (handled) {
            invalidateUi(action);

            if (action instanceof TwoStateAction) {
                invalidateUi(((TwoStateAction) action).getBoundAction());
            }
        }

        return handled;
    }

    private boolean dispatchLongClickAction(Action action) {
        if (action == null) {
            return false;
        }

        if (checkLongActionDisabled(action)) {
            return false;
        }

        boolean handled = false;

        if (mActions.containsKey((int) action.getId())) {
            mActionListener.onLongAction((int) action.getId(), getActionIndex(action));
            handled = true;
        }

        return handled;
    }

    private void incrementActionIndex(Action action) {
        if (action instanceof PlaybackControlsRow.MultiAction) {
            PlaybackControlsRow.MultiAction multiAction = (PlaybackControlsRow.MultiAction) action;
            multiAction.nextIndex();
        }
    }

    /**
     * Properly handle ui changes of multi-action buttons
     */
    private void invalidateUi(Action action) {
        if (action != null) {
            // Notify adapter of action changes to handle primary actions, such as, play/pause.
            notifyActionChanged(
                    action,
                    (ArrayObjectAdapter) getControlsRow().getPrimaryActionsAdapter());

            // Notify adapter of action changes to handle secondary actions, such as, thumbs up/down and repeat.
            notifyActionChanged(
                    action,
                    (ArrayObjectAdapter) getControlsRow().getSecondaryActionsAdapter());
        }
    }

    private void notifyActionChanged(
            Action action, ArrayObjectAdapter adapter) {
        if (adapter != null) {
            int index = adapter.indexOf(action);
            if (index >= 0) {
                adapter.notifyArrayItemRangeChanged(index, 1);
            }
        }
    }

    private void removePrimaryAction(Action action) {
        ArrayObjectAdapter adapter = (ArrayObjectAdapter) getControlsRow().getPrimaryActionsAdapter();
        if (adapter != null) {
            adapter.remove(action);
        }
    }

    private void removeSecondaryAction(Action action) {
        ArrayObjectAdapter adapter = (ArrayObjectAdapter) getControlsRow().getSecondaryActionsAdapter();
        if (adapter != null) {
            adapter.remove(action);
        }
    }

    private void addPrimaryAction(Action action, int position) {
        ArrayObjectAdapter adapter = (ArrayObjectAdapter) getControlsRow().getPrimaryActionsAdapter();
        addAction(action, position, adapter);
    }

    private void addSecondaryAction(Action action, int position) {
        ArrayObjectAdapter adapter = (ArrayObjectAdapter) getControlsRow().getSecondaryActionsAdapter();
        addAction(action, position, adapter);
    }

    private void addAction(Action action, int position, ArrayObjectAdapter adapter) {
        if (adapter != null) {
            int index = adapter.indexOf(action);
            if (index == -1) {
                int size = adapter.size();
                adapter.add(Math.min(position, size), action);
            }
        }
    }

    private Action findAction(int keyCode) {
        Action action = null;
        PlaybackControlsRow controlsRow = getControlsRow();

        if (controlsRow != null) {
            final ObjectAdapter primaryActionsAdapter = controlsRow.getPrimaryActionsAdapter();
            action = controlsRow.getActionForKeyCode(primaryActionsAdapter, keyCode);

            if (action == null) {
                action = controlsRow.getActionForKeyCode(controlsRow.getSecondaryActionsAdapter(),
                        keyCode);
            }
        }

        return action;
    }

    private void putAction(Action action) {
        mActions.put((int) action.getId(), action);
    }

    private int getActionIndex(Action action) {
        if (action instanceof PlaybackControlsRow.MultiAction) {
            PlaybackControlsRow.MultiAction multiAction = (PlaybackControlsRow.MultiAction) action;
            return multiAction.getIndex();
        }

        return 0;
    }

    private void setActionIndex(Action action, int actionIndex) {
        if (actionIndex == -1) { // button disabled
            disableAction(action);
        } else if (action instanceof MultiAction) {
            ((MultiAction) action).setIndex(actionIndex);
            invalidateUi(action);
        }
    }

    private void disableAction(Action action) {
        Drawable icon = action.getIcon();
        action.setIcon(ActionHelpers.createDrawable(getContext(), (BitmapDrawable) icon, ActionHelpers.getIconGrayedOutColor(getContext())));
        invalidateUi(action);
    }

    /**
     * Long press actions usually more important than short ones. So, try to use it first in case long click is disabled.
     */
    private boolean checkShortActionDisabled(Action action) {
        if (!mGeneralData.isOkButtonLongPressDisabled() && mPlayerTweaksData.isButtonLongClickEnabled()) {
            return false;
        }

        return (action == mActions.get(R.id.lb_control_closed_captioning) || action == mActions.get(R.id.action_video_speed)) &&
                dispatchLongClickAction(action); // replace short with long
    }

    private boolean checkLongActionDisabled(Action action) {
        if (!mGeneralData.isOkButtonLongPressDisabled() && mPlayerTweaksData.isButtonLongClickEnabled()) {
            return false;
        }

        return action.getId() == R.id.action_chat;
    }

    @Override
    protected void onAttachedToHost(PlaybackGlueHost host) {
        super.onAttachedToHost(host);

        Log.d(TAG, "On attached to host");
    }

    @Override
    public void onTopEdgeFocused() {
        mActionListener.onTopEdgeFocused();
    }

    /** Listens for when skip to next and previous actions have been dispatched. */
    public interface OnActionClickedListener {
        /** Skip to the previous item in the queue. */
        void onPrevious();

        /** Skip to the next item in the queue. */
        void onNext();

        void onPlay();

        void onPause();

        void onAction(int actionId, int actionIndex);

        void onLongAction(int actionId, int actionIndex);

        void onTopEdgeFocused();

        boolean onKeyDown(int keyCode);
    }
}
