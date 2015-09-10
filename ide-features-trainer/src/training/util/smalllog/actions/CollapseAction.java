package training.util.smalllog.actions;

import training.util.smalllog.ClickLabel;
import training.util.smalllog.SmallLog;
import training.util.smalllog.Type;

/**
 * Created by karashevich on 19/06/15.
 */
public class CollapseAction extends SLAction {

    public CollapseAction(SmallLog smallLog){
        super(smallLog);
    }


    @Override
    protected void logic() {
        String result = "";
        int inner_pivot = 0;

        for (ClickLabel clickLabel : smallLog.getClickLabels()) {
            if (clickLabel.isSelected()) {
                if (clickLabel.getType() == Type.TYPING) {
                    if (inner_pivot < result.length())
                        result = result.substring(0, inner_pivot) + clickLabel.getOriginalText() + result.substring(inner_pivot, result.length());
                    else
                        result += clickLabel.getOriginalText();
                    inner_pivot += clickLabel.getOriginalText().length();
                } else if (clickLabel.getType() == Type.ACTION) {
                    String text = clickLabel.getOriginalText();
                    if (text.contains("EditorBackSpace")) {
                        if (inner_pivot == result.length()) inner_pivot--;
                        result = result.substring(0, Math.max(0, result.length() - 1));

                    } else if (text.contains("EditorEnter")) {
                        result += "\n";
                        inner_pivot++;
                    } else if (text.contains("EditorLeft")) {
                        inner_pivot = Math.max(0, inner_pivot - 1);
                    } else if (text.contains("EditorRight")) {
                        inner_pivot = Math.min(result.length(), inner_pivot + 1);
                    }

                }
            }
        }
        System.out.println(result);
        ClickLabel cl = new ClickLabel(smallLog, smallLog.getSemiTransparentPanel().colorizeCommand(PROMPT + TYPING + result, TYPING, "green"), result, Type.TYPING);
        smallLog.getSemiTransparentPanel().setClickable(cl);
        smallLog.getSemiTransparentPanel().setDraggable(cl);
        smallLog.getSemiTransparentPanel().putClickLabel(smallLog.getPivot().getPosition() + 1, cl);
        multipleDelete();
        smallLog.getSemiTransparentPanel().update();

    }
}


