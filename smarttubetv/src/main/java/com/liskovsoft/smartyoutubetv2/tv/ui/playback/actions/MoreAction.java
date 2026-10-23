package com.liskovsoft.smartyoutubetv2.tv.ui.playback.actions;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.leanback.widget.Action;
import com.liskovsoft.smartyoutubetv2.tv.R;

/**
 * WiiOS: the gear at the right of the player. Opens a list of every other player feature (quality, speed,
 * repeat, SponsorBlock and the rest), so the row itself stays as short as YouTube's.
 */
public class MoreAction extends Action {
    public MoreAction(Context context) {
        super(R.id.action_more);
        setIcon(ContextCompat.getDrawable(context, R.drawable.action_more));
        setLabel1(context.getString(R.string.wiios_more));
    }
}
