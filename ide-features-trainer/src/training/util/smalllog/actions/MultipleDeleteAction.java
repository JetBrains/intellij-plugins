package training.util.smalllog.actions;

import training.util.smalllog.SmallLog;

/**
 * Created by karashevich on 19/06/15.
 */
public class MultipleDeleteAction extends SLAction{

    public MultipleDeleteAction(SmallLog smallLog){
        super(smallLog);
    }

    @Override
    protected void logic() {
        multipleDelete();
    }
}
