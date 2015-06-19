package org.jetbrains.training.util.smalllog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Created by karashevich on 17/06/15.
 */
public class Pivot {
    private ArrayList<ClickLabel> clickLabels;
    private ClickLabel pivotClickLabel;
    private int p;

    public Pivot(int p, @Nullable ClickLabel pivotClickLabel, @NotNull ArrayList<ClickLabel> clickLabels) {
        this.clickLabels = clickLabels;
        this.p = p;
        this.pivotClickLabel = pivotClickLabel;
    }

    public int getPosition(){
        if (clickLabels.contains(pivotClickLabel)) {
            p = clickLabels.indexOf(pivotClickLabel);
            return p;
        } else {
            return -1;
        }
    }

    public void move(int position){
        //check current position
        if (pivotClickLabel !=null && clickLabels.contains(pivotClickLabel)) {
            unmark(pivotClickLabel);
            if (position >= clickLabels.size() || position < 0) {
                pivotClickLabel = null;
                p = -1;
            } else {
                p = position;
                pivotClickLabel = clickLabels.get(p);
                mark(pivotClickLabel);
            }
        } else {
            if (position >= clickLabels.size() || position < 0) {
                pivotClickLabel = null;
                p = -1;
            } else {
                p = position;
                pivotClickLabel = clickLabels.get(p);
                mark(pivotClickLabel);
            }
        }
    }

    public void move_and_select(int position){
        if (position < clickLabels.size() && position >= 0) {
            move(position);
            clickLabels.get(position).setSelected(true);
        }
    }

    public ClickLabel getPivotClickLabel() { return pivotClickLabel;}

    private void mark(ClickLabel clickLabel){
        clickLabel.setBackground(clickLabel.getPivotColor());
    }

    private void unmark(ClickLabel clickLabel){
        if (clickLabel.isSelected())
            clickLabel.setBackground(clickLabel.getSelectedColor());
        else
            clickLabel.setBackground(clickLabel.getBackground());
    }

}

