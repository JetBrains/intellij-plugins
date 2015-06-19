package org.jetbrains.training.util.smalllog.actions;

import org.jetbrains.training.util.smalllog.ClickLabel;
import org.jetbrains.training.util.smalllog.SmallLog;

/**
 * Created by karashevich on 19/06/15.
 */
public class SelectAllAction extends SLAction {

    public SelectAllAction(SmallLog smallLog) {
        super(smallLog);
    }

    @Override
    protected void logic() {
        for (ClickLabel clickLabel : smallLog.getClickLabels()) {
            smallLog.getSemiTransparentPanel().flip(clickLabel, true);
        }
    }
}
