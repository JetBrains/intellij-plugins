package org.jetbrains.training.util.smalllog.actions;

import org.jetbrains.training.util.smalllog.SmallLog;

/**
 * Created by karashevich on 19/06/15.
 */
public class DeleteAction extends SLAction{

    public DeleteAction(SmallLog smallLog){
        super(smallLog);
    }

    @Override
    protected void logic() {
        if (smallLog.getPivot().getPosition() != -1) {
            if (smallLog.getClickLabels().size() > smallLog.getPivot().getPosition()) {
                int deleted = smallLog.getPivot().getPosition();
                smallLog.getSemiTransparentPanel().deleteClickLabel(smallLog.getClickLabels().get(smallLog.getPivot().getPosition()));
                //init pivot again
                if (deleted == smallLog.getClickLabels().size()) smallLog.getPivot().move(smallLog.getClickLabels().size() - 1);
                else smallLog.getPivot().move(deleted);
                smallLog.getSemiTransparentPanel().update();
            }
        }
    }
}
