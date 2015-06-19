package org.jetbrains.training.util.smalllog.actions;

import org.jetbrains.training.util.smalllog.ClickLabel;
import org.jetbrains.training.util.smalllog.FrameHolder;
import org.jetbrains.training.util.smalllog.SmallLog;

import java.util.ArrayList;

/**
 * Created by karashevich on 19/06/15.
 */
public abstract class SLAction {

    final public static String ACTION = "[Action System]";
    final public static String TYPING = "[Typing]";
    final public static String PROMPT = "> ";

    protected SmallLog smallLog;
    public Runnable runnable;

    public SLAction(SmallLog smallLog){
        this.smallLog = smallLog;
        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    protected abstract void logic() throws Exception;

    public void execute() throws Exception {

        logic();
        smallLog.getFrameHolder().snapFrame(smallLog.getClickLabels());

    }




    protected void multipleDelete() {
        final ArrayList<ClickLabel> markedClickLabels = new ArrayList<ClickLabel>();
        for (ClickLabel clickLabel: smallLog.getClickLabels()) if(clickLabel.isSelected()) markedClickLabels.add(clickLabel);

        for (ClickLabel clickLabel: markedClickLabels) {
            smallLog.getSemiTransparentPanel().deleteClickLabel(clickLabel);
        }
        while(!markedClickLabels.isEmpty()) {
            markedClickLabels.remove(0);
        }

        smallLog.getPivot().move(-1);
        smallLog.getSemiTransparentPanel().update();

        smallLog.getSemiTransparentPanel().setLastClicked(-1);

    }

}
