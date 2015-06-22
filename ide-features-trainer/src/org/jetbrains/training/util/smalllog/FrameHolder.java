package org.jetbrains.training.util.smalllog;

import com.intellij.util.containers.BidirectionalMap;

import java.util.ArrayList;

/**
 * Created by karashevich on 17/06/15.
 */
public class FrameHolder {

    BidirectionalMap<Integer, ClickLabel> map;
    ArrayList<Frame> frames;
    int pivot;
    private SmallLog smallLog;

    public FrameHolder(SmallLog smallLog) {
        map = new BidirectionalMap<Integer, ClickLabel>();
        frames = new ArrayList<Frame>();
        this.smallLog = smallLog;
        pivot = 0;
        try {
            snapFrame(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int addClickLabel(ClickLabel cl) {
        if (map.containsValue(cl)) {
            return map.getKeysByValue(cl).get(0);
        } else {
            map.put(map.size(), cl);
            return (map.size() - 1);
        }
    }


    public void snapFrame(ArrayList<ClickLabel> clickLabels) throws Exception {
            if (frames != null) {
                if (clickLabels == null || clickLabels.size() == 0) {
                    frames.add(new Frame(new ArrayList<Integer>(0)));
                    frames.get(0).printFrame();
                    pivot = 1;
                } else {
                    if (pivot != frames.size())
                        if (pivot > frames.size()) throw new Exception();
                        else
                            while (pivot < frames.size())
                                frames.remove(pivot);
                    ArrayList<Integer> uids = new ArrayList<Integer>(clickLabels.size());
                    for (int i = 0; i < clickLabels.size(); i++) {
                        uids.add(i, addClickLabel(clickLabels.get(i)));
                    }

                    final Frame newFrame = new Frame(uids);
                    frames.add(newFrame);
                    newFrame.printFrame();
                    pivot = frames.size();
                }
            }
    }


    public void repaintSmallLog(ArrayList<Integer> uids){
        //convert ArrayList of Integers to ArrayList of ClickLabels
        ArrayList<ClickLabel> clickLabels = new ArrayList<ClickLabel>(uids.size());
        for (int i = 0; i < uids.size(); i++) {
            clickLabels.add(i, map.get(uids.get(i)));
        }
        //repaintSmallLog here

        smallLog.set(clickLabels);
    }

    public void undo(){
        if (pivot == frames.size() && pivot > 0) {
            pivot --;
            repaintSmallLog(frames.get(pivot - 1).getUids());
        } else {
            if (pivot > 1) {
                pivot--;
                 repaintSmallLog(frames.get(pivot - 1).getUids());
            } else if (pivot == 1 && frames.size() > 0) {
                repaintSmallLog(frames.get(pivot - 1).getUids());
            }
        }

    }

    public void redo(){
        if (pivot < frames.size()) {
            pivot++;
            repaintSmallLog(frames.get(pivot).getUids());
        }

    }

    class Frame{
        ArrayList<Integer> uids;

        public Frame(ArrayList<Integer> clickLabels) {
            this.uids = clickLabels;
        }

        public ArrayList<Integer> getUids(){
            final ArrayList<Integer> clone = (ArrayList<Integer>) uids.clone();
            return clone;
        }

        public void printFrame(){
            System.out.println("+++++PRINTING FRAME ("+ pivot + ")++++++");
            for (Integer uid : uids) {
                System.out.println("["+ uid +", ClickLabel(\""+ map.get(uid).getOriginalText() + "\")]");
            }
            System.out.println("========================================");
            System.out.println();
        }

    }
}
