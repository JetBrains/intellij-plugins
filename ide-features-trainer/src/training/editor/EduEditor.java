package training.editor;

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
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.openapi.wm.impl.IdeRootPane;
import com.intellij.pom.Navigatable;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.editor.eduUI.DemoModeUI;
import training.editor.eduUI.EduBalloonBuilder;
import training.learn.ActionsRecorder;
import training.editor.actions.BlockCaretAction;
import training.editor.actions.EduActions;
import training.editor.eduUI.EduPanel;
import training.editor.eduUI.Message;
import training.learn.*;
import training.learn.exceptons.BadCourseException;
import training.learn.exceptons.BadLessonException;
import training.learn.exceptons.LessonIsOpenedException;
import training.util.HighlightComponent;
import training.util.LearnUiUtil;

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
    private final EduBalloonBuilder eduBalloonBuilder;

    private Project myProject;
    private FileEditor myDefaultEditor;
    private JComponent myComponent;
    final private EduPanel eduPanel;
    private HashSet<ActionsRecorder> actionsRecorders;
    private VirtualFile vf;

    private boolean isDisposed = false;
    Course myCourse;

    //Demo Mode infrastructure
    private boolean demoMode = false;
    HighlightComponent highlightedEditor;
    DemoModeUI demoModeUI = null;

    private boolean blockCaretBalloonIsShown = false;

    private boolean mouseBlocked;


    public EduEditor(@NotNull final Project project, @NotNull final VirtualFile file) {

        myProject = project;
        vf = file;
        myDefaultEditor = TextEditorProvider.getInstance().createEditor(myProject, file);
        Disposer.register(myDefaultEditor, this); //dispose EduEditor when default has been already disposed
        myComponent = myDefaultEditor.getComponent();
        eduPanel = new EduPanel(275);
        myComponent.add(eduPanel, BorderLayout.WEST);
        actionsRecorders = new HashSet<ActionsRecorder>();



        mouseBlocked = false;
        eduBalloonBuilder = new EduBalloonBuilder(null, balloonDelay, "Caret is blocked in this lesson");
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
        return "EduEditor";
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
        if(demoMode) {
            if(highlightedEditor != null) {
                final Container parent = highlightedEditor.getParent();
                if (parent != null) {
                    parent.remove(highlightedEditor);
                    highlightedEditor = null;
                    demoMode = false;
                    parent.repaint();
                }
            }
        }
        isDisposed = true;
        if (myCourse != null) CourseManager.getInstance().unregisterCourse(myCourse);
        EduEditorManager.getInstance().disposeEduEditor(this);
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
            return ((TextEditor)myDefaultEditor).canNavigateTo(navigatable);
        }
        return false;
    }

    @Override
    public void navigateTo(@NotNull Navigatable navigatable) {
        if (myDefaultEditor instanceof TextEditor) {
            ((TextEditor)myDefaultEditor).navigateTo(navigatable);
        }
    }

    public boolean isDisposed() {
        return isDisposed;
    }




    public void selectIt() {
        HashSet<FileEditor> selectedEditors = new HashSet<FileEditor>(Arrays.asList(FileEditorManager.getInstance(myProject).getSelectedEditors()));
        if (!selectedEditors.contains(this)) {
//            FileEditorManager.getInstance(myProject).setSelectedEditor(vf, EduEditorProvider.EDITOR_TYPE_ID);
            FileEditorManager.getInstance(myProject).openEditor(new OpenFileDescriptor(myProject, vf), true);

        }
    }

    public void activateDemoMode() throws Exception {
        if(demoMode) return;
        final JBColor demoCurtainColor = DemoModeUI.getDemoCurtainColor();

        final IdeFrameImpl frame = WindowManagerEx.getInstanceEx().getFrame(myProject);
        final IdeRootPane ideRootPane = (IdeRootPane)frame.getRootPane();
        final JComponent glassPane = (JComponent) ideRootPane.getGlassPane();

        Component editorComponent = null;
        for (Component component : eduPanel.getParent().getComponents()) {
            if (!(component instanceof EduPanel)){
               if (component instanceof JPanel){
                   editorComponent = component;
               }
            }
        }
        if (editorComponent == null) throw new Exception("Unable to highlight editor component (editor component cannot be found).");

        highlightedEditor = LearnUiUtil.highlightComponent(editorComponent, "Edu Editor", ideRootPane, glassPane, demoCurtainColor, false, false);

        //add Demo mode to Status bar
        if(demoModeUI == null) {
            demoModeUI = new DemoModeUI();
        }
        demoMode = true;





        demoModeUI.addDemoModeWidget(myProject, this);

    }

    public void deactivateDemoMode(){
        if(!demoMode) return;
        if (highlightedEditor == null) return;
        final Container parent = highlightedEditor.getParent();
        if(parent == null) return;
        parent.remove(highlightedEditor);
        parent.repaint();
        demoMode = false;
        demoModeUI.updateDemoModeWidget();
    }

    public boolean isDemoModeOn(){
        return demoMode;
    }
}
