package org.jetbrains.training.util.smalllog;

import com.intellij.util.containers.BidirectionalMap;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by karashevich on 17/06/15.
 */
public class FrameHolder {

    BidirectionalMap<Integer, ClickLabel> map;
    ArrayList<Frame> frames;
    int pivot;

    public FrameHolder() {
        map = new BidirectionalMap<Integer, ClickLabel>();
        frames = new ArrayList<Frame>();
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
                if (pivot != frames.size())
                    if (pivot > frames.size()) throw new Exception();
                    else
                        while (pivot > frames.size())
                            frames.remove(pivot);
                ArrayList<Integer> uids = new ArrayList<Integer>(clickLabels.size());
                for (int i = 0; i < clickLabels.size(); i++) {
                    uids.add(i, addClickLabel(clickLabels.get(i)));
                }

                final Frame newFrame = new Frame(uids);
                frames.add(newFrame);
                pivot = frames.size();
                printInfographics();
                newFrame.printFrame();
            }
    }

    public void printInfographics(){
        System.out.print("FrameHolder:");
        for (int i = 0; i < (pivot - 1); i++) {
            System.out.print("#");
        }
        System.out.println("V");
        for (int i = pivot; i < frames.size(); i++) {
            System.out.print("#");
        }
        System.out.println();
    }

    public void repaintSmallLog(ArrayList<Integer> uids){
        //convert ArrayList of Integers to ArrayList of ClickLabels
        ArrayList<ClickLabel> clickLabels = new ArrayList<ClickLabel>(uids.size());
        for (int i = 0; i < uids.size(); i++) {
            clickLabels.add(i, map.get(i));
        }
        //repaintSmallLog here
        //smallLog.repaintFor(clickLabels);
    }

    public void undo(){
        if (pivot > 0) {
            pivot--;
            repaintSmallLog(frames.get(pivot).getUids());
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
