package org.jetbrains.training.editor;

import com.intellij.openapi.editor.Editor;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Created by karashevich on 24/02/15.
 */
public class MouseListenerHolder {
    private MouseListener[] myMouseListeners;
    private MouseMotionListener[] myMouseMotionListeners;

    public MouseListenerHolder() {
    }

    public void grabListeners(Editor editor){
        MouseListener[] mouseListeners = editor.getContentComponent().getMouseListeners();
        myMouseListeners = new MouseListener[mouseListeners.length];

        int i = 0;
        for (MouseListener mouseListener : mouseListeners) {
            myMouseListeners[i] = mouseListener;
            editor.getContentComponent().removeMouseListener(mouseListener);
            i++;
        }

        //kill all mouse (motion) listeners
        i = 0;
        MouseMotionListener[] mouseMotionListeners = editor.getContentComponent().getMouseMotionListeners();
        myMouseMotionListeners = new MouseMotionListener[mouseMotionListeners.length];
        for (MouseMotionListener mouseMotionListener : mouseMotionListeners) {
            myMouseMotionListeners[i] = mouseMotionListener;
            editor.getContentComponent().removeMouseMotionListener(mouseMotionListener);
            i++;
        }
    }

    public void restoreListeners(Editor editor){
        if (myMouseListeners != null) {
            for (MouseListener myMouseListener : myMouseListeners) {
                editor.getContentComponent().addMouseListener(myMouseListener);
            }
        }

        if (myMouseMotionListeners != null) {
            for (MouseMotionListener myMouseMotionListener : myMouseMotionListeners) {
                editor.getContentComponent().addMouseMotionListener(myMouseMotionListener);
            }
        }
    }
}
