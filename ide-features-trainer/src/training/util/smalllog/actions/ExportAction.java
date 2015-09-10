package training.util.smalllog.actions;

import training.util.smalllog.ClickLabel;
import training.util.smalllog.SmallLog;

/**
 * Created by karashevich on 19/06/15.
 */
public class ExportAction extends SLAction {

    public ExportAction(SmallLog smallLog) {
        super(smallLog);
    }

    @Override
    protected void logic() {
        for (ClickLabel clickLabel : smallLog.getClickLabels()) {
            if (clickLabel.isSelected() || clickLabel.equals(smallLog.getPivot().getPivotClickLabel()))
                System.out.println(clickLabel.getOriginalText());
        }

    }
}
