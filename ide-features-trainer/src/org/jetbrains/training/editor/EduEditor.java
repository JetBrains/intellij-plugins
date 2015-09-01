package org.jetbrains.training.editor;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorImpl;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import org.jdesktop.swingx.action.ActionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.ActionsRecorder;
import org.jetbrains.training.editor.actions.BlockCaretAction;
import org.jetbrains.training.editor.actions.LearnActions;
import org.jetbrains.training.editor.eduUI.EduPanel;
import org.jetbrains.training.editor.eduUI.Message;
import org.jetbrains.training.lesson.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class EduEditor implements TextEditor {

    public final int balloonDelay = 3000;

    private Project myProject;
    private FileEditor myDefaultEditor;
    private JComponent myComponent;
    final private EduPanel eduPanel;
    private HashSet<ActionsRecorder> actionsRecorders;
    private VirtualFile vf;
    Course myCourse;

    private MouseListener[] myMouseListeners;
    private MouseMotionListener[] myMouseMotionListeners;
    private MouseListener myMouseDummyListener;
    private boolean mouseBlocked;

    public void setMyMouseListeners(MouseListener[] myMouseListeners) {
        this.myMouseListeners = myMouseListeners;
    }

    public void setMyMouseMotionListeners(MouseMotionListener[] myMouseMotionListeners) {
        this.myMouseMotionListeners = myMouseMotionListeners;
    }

    public MouseListener[] getMyMouseListeners() {
        return myMouseListeners;
    }

    public MouseMotionListener[] getMyMouseMotionListeners() {
        return myMouseMotionListeners;
    }

    public boolean isMouseBlocked() {
        return mouseBlocked;
    }

    public void setMouseBlocked(boolean mouseBlocked) {
        this.mouseBlocked = mouseBlocked;
    }

    ArrayList<LearnActions> myLearnActions;

    public EduEditor(@NotNull final Project project, @NotNull final VirtualFile file) {

        myProject = project;
        vf = file;
        myDefaultEditor = TextEditorProvider.getInstance().createEditor(myProject, file);
        myComponent = myDefaultEditor.getComponent();
        eduPanel = new EduPanel(this, 275);
        myComponent.add(eduPanel, BorderLayout.WEST);
        actionsRecorders = new HashSet<ActionsRecorder>();

        if (myLearnActions == null) {
            myLearnActions = new ArrayList<LearnActions>();
        }

        mouseBlocked = false;
    }

    private FileEditor getDefaultEditor() {
        return myDefaultEditor;
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return myComponent;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return myDefaultEditor.getPreferredFocusedComponent();
    }

    @NotNull
    @Override
    public String getName() {
        return "Edu Editor";
    }

    @NotNull
    @Override
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return myDefaultEditor.getState(level);
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
        myDefaultEditor.setState(state);
    }

    @Override
    public boolean isModified() {
        return myDefaultEditor.isModified();
    }

    @Override
    public boolean isValid() {
        return myDefaultEditor.isValid();
    }

    @Override
    public void selectNotify() {
        myDefaultEditor.selectNotify();
    }

    @Override
    public void deselectNotify() {
        myDefaultEditor.deselectNotify();
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
        myDefaultEditor.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
        myDefaultEditor.removePropertyChangeListener(listener);
    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return myDefaultEditor.getBackgroundHighlighter();
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return myDefaultEditor.getCurrentLocation();
    }

    @Nullable
    @Override
    public StructureViewBuilder getStructureViewBuilder() {
        return myDefaultEditor.getStructureViewBuilder();
    }

    @Override
    public void dispose() {
        Disposer.dispose(myDefaultEditor);
    }

    @Nullable
    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        return myDefaultEditor.getUserData(key);
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        myDefaultEditor.putUserData(key, value);
    }


    @Nullable
    public static EduEditor getSelectedEduEditor(@NotNull final Project project) {
        try {
            final FileEditor fileEditor = FileEditorManagerEx.getInstanceEx(project).getSplitters().getCurrentWindow().
                    getSelectedEditor().getSelectedEditorWithProvider().getFirst();
            if (fileEditor instanceof EduEditor) {
                return (EduEditor)fileEditor;
            }
        }
        catch (Exception e) {
            return null;
        }
        return null;
    }

    @Nullable
    public static Editor getSelectedEditor(@NotNull final Project project) {
        final EduEditor eduEditor = getSelectedEduEditor(project);
        if (eduEditor != null) {
            FileEditor defaultEditor = eduEditor.getDefaultEditor();
            if (defaultEditor instanceof PsiAwareTextEditorImpl) {
                return ((PsiAwareTextEditorImpl)defaultEditor).getEditor();
            }
        }
        return null;
    }



    @NotNull
    @Override
    public Editor getEditor() {
        if (myDefaultEditor instanceof TextEditor) {
            return ((TextEditor)myDefaultEditor).getEditor();
        }
        return EditorFactory.getInstance().createViewer(new DocumentImpl(""), myProject);
    }

    @Override
    public boolean canNavigateTo(@NotNull Navigatable navigatable) {
        if (myDefaultEditor instanceof TextEditor) {
            ((TextEditor)myDefaultEditor).canNavigateTo(navigatable);
        }
        return false;
    }

    @Override
    public void navigateTo(@NotNull Navigatable navigatable) {
        if (myDefaultEditor instanceof TextEditor) {
            ((TextEditor)myDefaultEditor).navigateTo(navigatable);
        }
    }

    public static void deleteGuardedBlocks(@NotNull final Document document) {
        if (document instanceof DocumentImpl) {
            final DocumentImpl documentImpl = (DocumentImpl)document;
            List<RangeMarker> blocks = documentImpl.getGuardedBlocks();
            for (final RangeMarker block : blocks) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction(new Runnable() {
                            @Override
                            public void run() {
                                document.removeGuardedBlock(block);
                            }
                        });
                    }
                });
            }
        }
    }


    public void addMessage(String message){
        eduPanel.addMessage(message);
    }

    public void addMessage(Message[] messages) {
        eduPanel.addMessage(messages);
    }

    public void passExercise() {
        eduPanel.setPreviousMessagesPassed();
    }

    public void passLesson(Lesson lesson) {
        eduPanel.setLessonPassed();
        if(lesson.getCourse()!=null && lesson.getCourse().hasNotPassedLesson()){
            final Lesson notPassedLesson = lesson.getCourse().giveNotPassedLesson();
            eduPanel.setNextButtonAction(new Runnable() {
                @Override
                public void run() {
                    try {
                        CourseManager.getInstance().openLesson(myProject, notPassedLesson);
                    } catch (BadCourseException e) {
                        e.printStackTrace();
                    } catch (BadLessonException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (FontFormatException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (LessonIsOpenedException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            eduPanel.hideNextButton();
        }
        eduPanel.updateLessonPanel(lesson);
    }

    public void initAllLessons(Lesson lesson) {
        eduPanel.setAllLessons(lesson);
    }

    public void clearEditor() {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                final Editor editor = getEditor();
                if (editor != null) {
                    final Document document = getEditor().getDocument();
                    if (document != null) {
                        try {
                            document.setText("");
                        } catch (Exception e) {
                            System.err.println("Unable to update text in EduEdutor!");
                        }
                    }
                }
            }
        });
    }

    public void clearLessonPanel() {
        eduPanel.clearLessonPanel();
    }

    public void registerActionsRecorder(ActionsRecorder recorder){
        actionsRecorders.add(recorder);
    }

    public void removeActionsRecorders(){
        for (ActionsRecorder actionsRecorder : actionsRecorders) {
            actionsRecorder.dispose();
        }
        actionsRecorders.clear();
    }

    public void initLesson(Lesson lesson) {
        eduPanel.setLessonName(lesson.getId());
        hideButtons();
        initAllLessons(lesson);
        clearEditor();
        clearLessonPanel();
        removeActionsRecorders();
        if (isMouseBlocked()) restoreMouseActions();
        if (myLearnActions != null) {
            for (LearnActions myLearnAction : myLearnActions) {
                myLearnAction.unregisterAction();
            }
            myLearnActions.clear();
        }

    }


    private void hideButtons() {
        eduPanel.hideButtons();
    }


    public void updateMyDefaultEditor() {
        myDefaultEditor = TextEditorProvider.getInstance().createEditor(myProject, vf);
        myDefaultEditor.getComponent().setVisible(true);
        myComponent = myDefaultEditor.getComponent();
        myComponent.add(eduPanel, BorderLayout.WEST);
    }

    public void blockCaret() {

//        try {
//            LearnUiUtil.getInstance().drawIcon(myProject, getEditor());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        for (LearnActions myLearnAction : myLearnActions) {
            if(myLearnAction instanceof BlockCaretAction) return;
        }

        BlockCaretAction blockCaretAction = new BlockCaretAction(getEditor());
        blockCaretAction.addActionHandler(new Runnable() {
            @Override
            public void run() {
                try {
                    showCaretBlockedBalloon();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        myLearnActions.add(blockCaretAction);
    }

    public void unblockCaret() {
        ArrayList<BlockCaretAction> myBlockActions = new ArrayList<BlockCaretAction>();

        for (LearnActions myLearnAction : myLearnActions) {
            if(myLearnAction instanceof BlockCaretAction) {
                myBlockActions.add((BlockCaretAction) myLearnAction);
                ((BlockCaretAction) myLearnAction).unregisterAction();
            }
        }

        myLearnActions.removeAll(myBlockActions);
    }

    public void grabMouseActions(){
        MouseListener[] mouseListeners = getEditor().getContentComponent().getMouseListeners();
        setMyMouseListeners(getEditor().getContentComponent().getMouseListeners());

        for (MouseListener mouseListener : mouseListeners) {
            getEditor().getContentComponent().removeMouseListener(mouseListener);
        }

        //kill all mouse (motion) listeners
        MouseMotionListener[] mouseMotionListeners = getEditor().getContentComponent().getMouseMotionListeners();
        setMyMouseMotionListeners(getEditor().getContentComponent().getMouseMotionListeners());

        for (MouseMotionListener mouseMotionListener : mouseMotionListeners) {
            getEditor().getContentComponent().removeMouseMotionListener(mouseMotionListener);
        }

        myMouseDummyListener  = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                try {
                    showCaretBlockedBalloon();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                try {
                    showCaretBlockedBalloon();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                try {
                    showCaretBlockedBalloon();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        getEditor().getContentComponent().addMouseListener(myMouseDummyListener);

        setMouseBlocked(true);
    }

    public void restoreMouseActions(){
        if (getMyMouseListeners() != null) {
            for (MouseListener myMouseListener : getMyMouseListeners()) {
                getEditor().getContentComponent().addMouseListener(myMouseListener);
            }
        }

        if (getMyMouseMotionListeners() != null) {
            for (MouseMotionListener myMouseMotionListener : getMyMouseMotionListeners()) {
                getEditor().getContentComponent().addMouseMotionListener(myMouseMotionListener);
            }
        }

        if(myMouseDummyListener != null) getEditor().getContentComponent().removeMouseListener(myMouseDummyListener);

        setMyMouseListeners(null);
        setMyMouseMotionListeners(null);
        setMouseBlocked(false);
    }

    private static void showBalloon(Editor editor, String text, final int delay) throws InterruptedException {
        if (editor == null) return;

        int offset = editor.getCaretModel().getCurrentCaret().getOffset();
        VisualPosition position = editor.offsetToVisualPosition(offset);
        Point point = editor.visualPositionToXY(position);

        String balloonText = text;


        BalloonBuilder builder =
                JBPopupFactory.getInstance().
                        createHtmlTextBalloonBuilder(balloonText, null, UIUtil.getLabelBackground(), null)
                        .setHideOnClickOutside(false)
                        .setCloseButtonEnabled(true)
                        .setHideOnKeyOutside(false);
        final Balloon myBalloon = builder.createBalloon();

        myBalloon.show(new RelativePoint(editor.getContentComponent(), point), Balloon.Position.above);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(delay);
                    myBalloon.hide();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

            }
        }).start();

    }

    private void showCaretBlockedBalloon() throws InterruptedException {
        showBalloon(getEditor(), "Caret is blocked in this lesson", balloonDelay);
    }

    public void selectIt() {
        HashSet<FileEditor> selectedEditors = new HashSet<FileEditor>(Arrays.asList(FileEditorManager.getInstance(myProject).getSelectedEditors()));
        if (!selectedEditors.contains(this)) {
//            FileEditorManager.getInstance(myProject).setSelectedEditor(vf, EduEditorProvider.EDITOR_TYPE_ID);
            FileEditorManager.getInstance(myProject).openEditor(new OpenFileDescriptor(myProject, vf), true);

        }
    }


}
