package training.editor;

import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import training.learn.CourseManager;

/**
 * Created by karashevich on 23/06/15.
 */
public class EduEditorProvider implements FileEditorProvider, DumbAware {


    public static final String EDITOR_TYPE_ID = "EduEditor";
    final private com.intellij.openapi.fileEditor.FileEditorProvider defaultTextEditorProvider = TextEditorProvider.getInstance();

    public EduEditorProvider() {

    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return CourseManager.getInstance().isVirtualFileRegistered(virtualFile);
    }


    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        final EduEditorManager eduEditorManager = EduEditorManager.getInstance();
        //Under unit test mode create a new EduEditor with a new EventMulticaster
//        if (ApplicationManager.getApplication().isUnitTestMode()) {
//            if (eduEditorManager.fileEduEditorMap.containsKey(file)) {
//                EduEditor eduEditor = eduEditorManager.fileEduEditorMap.get(file);
//                FileEditorManager.getInstance(project).closeFile(file);
//                eduEditorManager.fileEduEditorMap.remove(file);
//            }
//        }
        if(eduEditorManager.fileEduEditorMap.containsKey(file)){
            EduEditor eduEditor = eduEditorManager.fileEduEditorMap.get(file);
            if(!eduEditor.isDisposed() && !eduEditor.getEditor().getProject().isDisposed()) {
                return eduEditor;
            } else {
                eduEditor = new EduEditor(project, file);
                eduEditorManager.registerEduEditor(eduEditor, file);
                return eduEditor;
            }
        } else {
            EduEditor eduEditor = new EduEditor(project, file);
            eduEditorManager.registerEduEditor(eduEditor, file);
            return eduEditor;
        }
    }

    @Override
    public void disposeEditor(@NotNull FileEditor editor) {
        defaultTextEditorProvider.disposeEditor(editor);
    }

    @NotNull
    @Override
    public FileEditorState readState(@NotNull Element sourceElement, @NotNull Project project, @NotNull VirtualFile file) {
        return defaultTextEditorProvider.readState(sourceElement, project, file);
    }

    @Override
    public void writeState(@NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement) {
        defaultTextEditorProvider.writeState(state, project, targetElement);
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }


}
