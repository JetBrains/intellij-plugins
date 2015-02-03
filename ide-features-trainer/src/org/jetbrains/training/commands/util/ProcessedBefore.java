package org.jetbrains.training.commands.util;

/**
 * Created by karashevich on 03/02/15.
 */
public class ProcessedBefore {
    private boolean prBefore;

    public ProcessedBefore(boolean prBefore) {
        this.prBefore = prBefore;
    }

    public void setTrue(){
        this.prBefore = true;
    }

    public void setFalse() {
        this.prBefore = false;
    }

    public boolean isPrBefore(){
        return this.prBefore;
    }
}
