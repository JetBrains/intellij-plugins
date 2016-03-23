package training.editor.eduUI;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;

import java.awt.*;

public class EduBalloonBuilder {

    private final Editor myEditor;
    private final Project myProject;

    private int myDelay;
    private String myText;

    private int lastOffset = -1;
    private boolean reuseLastBalloon = false;
    private Balloon lastBalloon = null;

    public EduBalloonBuilder(Editor editor, int delay, String text) {
        myDelay = delay;
        myText = text;

        myEditor = editor;
        myProject = myEditor.getProject();
    }

    public void showBalloon() throws InterruptedException {

        int offset = myEditor.getCaretModel().getCurrentCaret().getOffset();
        if(lastOffset == offset && lastBalloon != null && !lastBalloon.isDisposed()) {
            reuseLastBalloon = true;
        } else {
            lastOffset = offset;
            VisualPosition position = myEditor.offsetToVisualPosition(offset);
            final Point point = myEditor.visualPositionToXY(position);

            BalloonBuilder builder =
                    JBPopupFactory.getInstance().
                            createHtmlTextBalloonBuilder(myText, null, UIUtil.getLabelBackground(), null)
                            .setHideOnClickOutside(false)
                            .setCloseButtonEnabled(true)
                            .setHideOnKeyOutside(false)
                            .setAnimationCycle(0);
            final Balloon myBalloon = builder.createBalloon();
            lastBalloon = myBalloon;
            myBalloon.setAnimationEnabled(false);
            myProject.getMessageBus().connect(myProject).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerAdapter() {
                @Override
                public void selectionChanged(FileEditorManagerEvent event) {
                    myBalloon.hide();
                    myBalloon.dispose();
                }
            });

            myBalloon.show(new RelativePoint(myEditor.getContentComponent(), point), Balloon.Position.above);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        do {
                            reuseLastBalloon = false;
                            Thread.sleep(myDelay);
                        } while (reuseLastBalloon);
                        if (!myBalloon.isDisposed()) {
                            myBalloon.hide();
                            myBalloon.dispose();
                        }
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                }
            }).start();
        }
    }
}