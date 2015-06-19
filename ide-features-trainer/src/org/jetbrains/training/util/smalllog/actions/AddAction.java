package org.jetbrains.training.util.smalllog.actions;

import org.jetbrains.training.util.smalllog.ClickLabel;
import org.jetbrains.training.util.smalllog.SmallLog;
import org.jetbrains.training.util.smalllog.Type;

/**
 * Created by karashevich on 19/06/15.
 */
public class AddAction extends SLAction {

    private Type type;
    private String text;

    public AddAction(SmallLog smallLog, Type type, String text) {
        super(smallLog);
        this.type = type;
        this.text = text;
    }

    @Override
    protected void logic() throws Exception {
        addLine(type, text);
    }


    public synchronized void addLine(Type type, String text) throws Exception {

        if (!smallLog.getSemiTransparentPanel().getCharBuffer().isEmpty()) {
            String cb = smallLog.getSemiTransparentPanel().flushCharBuffer();
            ClickLabel cLabel1 = new ClickLabel(smallLog, PROMPT + smallLog.getSemiTransparentPanel().colorizeCommand(TYPING, TYPING, "green") + " " + cb, cb, Type.TYPING);
            smallLog.getClickLabels().add(cLabel1);
            smallLog.getSemiTransparentPanel().setClickable(cLabel1);
            smallLog.getSemiTransparentPanel().add(cLabel1);
            smallLog.getSemiTransparentPanel().update();
            smallLog.getSemiTransparentPanel().update();

        }
        if(type == Type.ACTION){
            ClickLabel cLabel = new ClickLabel(smallLog, PROMPT + smallLog.getSemiTransparentPanel().colorizeCommand(ACTION, ACTION, "red") + " " + text, text, Type.ACTION);
            smallLog.getClickLabels().add(cLabel);
            smallLog.getSemiTransparentPanel().setClickable(cLabel);
            smallLog.getSemiTransparentPanel().setDraggable(cLabel);
            smallLog.getSemiTransparentPanel().add(cLabel);
            smallLog.getSemiTransparentPanel().update();

        } else {
            ClickLabel cLabel = new ClickLabel(smallLog, PROMPT + text, text, Type.UNKNOWN);
            smallLog.getClickLabels().add(cLabel);
            smallLog.getSemiTransparentPanel().setClickable(cLabel);
            smallLog.getSemiTransparentPanel().add(cLabel);
            smallLog.getSemiTransparentPanel().update();

        }

    }
}
