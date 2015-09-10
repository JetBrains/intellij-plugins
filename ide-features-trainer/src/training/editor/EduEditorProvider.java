package training.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import training.lesson.CourseManager;

import java.util.HashMap;

/**
 * Created by karashevich on 23/06/15.
 */
public class EduEditorProvider implements FileEditorProvider, DumbAware {


    public static final String EDITOR_TYPE_ID = "EduEditor";
    final private com.intellij.openapi.fileEditor.FileEditorProvider defaultTextEditorProvider = TextEditorProvider.getInstance();
    HashMap<VirtualFile, EduEditor> fileEduEditorMap;

    public EduEditorProvider() {
        fileEduEditorMap = new HashMap<VirtualFile, EduEditor>();
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return CourseManager.getInstance().isVirtualFileRegistered(virtualFile);
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        if(fileEduEditorMap.containsKey(file)){
            EduEditor eduEditor = fileEduEditorMap.get(file);
            if(!eduEditor.isDisposed() && !eduEditor.getEditor().getProject().isDisposed()) {
                return eduEditor;
            } else {
                eduEditor = new EduEditor(project, file);
                fileEduEditorMap.put(file, eduEditor);
                return eduEditor;
            }
        } else {
            EduEditor eduEditor = new EduEditor(project, file);
            fileEduEditorMap.put(file, eduEditor);
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
