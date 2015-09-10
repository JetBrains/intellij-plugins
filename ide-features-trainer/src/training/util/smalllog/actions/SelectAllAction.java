package training.util.smalllog.actions;

import training.util.smalllog.ClickLabel;
import training.util.smalllog.SmallLog;

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
