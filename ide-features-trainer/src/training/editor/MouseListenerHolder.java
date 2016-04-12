package training.editor;

import com.intellij.openapi.editor.Editor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Created by karashevich on 24/02/15.
 */
public class MouseListenerHolder {
    private MouseListener[] myMouseListeners;
    private MouseMotionListener[] myMouseMotionListeners;
    private MouseListener myMouseDummyListener;
    private Editor myEditor;

    private boolean mouseBlocked = false;

    public MouseListenerHolder(Editor editor) {
        myEditor = editor;
        myMouseListeners = null;
        myMouseMotionListeners = null;
        myMouseDummyListener = null;
    }

    public void grabMouseActions(Runnable runWhenMouseAction) {
        MouseListener[] mouseListeners = myEditor.getContentComponent().getMouseListeners();
        myMouseListeners = myEditor.getContentComponent().getMouseListeners();


        for (MouseListener mouseListener : mouseListeners) {
            myEditor.getContentComponent().removeMouseListener(mouseListener);
        }

        //kill all mouse (motion) listeners
        MouseMotionListener[] mouseMotionListeners = myEditor.getContentComponent().getMouseMotionListeners();
        myMouseMotionListeners = myEditor.getContentComponent().getMouseMotionListeners();

        for (MouseMotionListener mouseMotionListener : mouseMotionListeners) {
            myEditor.getContentComponent().removeMouseMotionListener(mouseMotionListener);
        }

        myMouseDummyListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                runWhenMouseAction.run();
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                runWhenMouseAction.run();

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                runWhenMouseAction.run();
            }
        };
        myEditor.getContentComponent().addMouseListener(myMouseDummyListener);
        mouseBlocked = true;
    }

    public void restoreMouseActions(Editor editor) {

        if (!mouseBlocked || myEditor == null || !editor.equals(myEditor)) return; //do not restore mouse aciotns for disposed editors

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

        if (myMouseDummyListener != null) editor.getContentComponent().removeMouseListener(myMouseDummyListener);

        myMouseListeners = null;
        myMouseMotionListeners = null;
        myMouseDummyListener = null;
        mouseBlocked = false;
    }

}
