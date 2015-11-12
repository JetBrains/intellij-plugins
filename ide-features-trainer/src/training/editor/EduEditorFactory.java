package training.editor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.editor.impl.EditorFactoryImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.lesson.CourseManager;

/**
 * Created by karashevich on 11/11/15.
 */
public class EduEditorFactory extends EditorFactoryImpl implements ApplicationComponent {

    final private static String doubleBuildStringKey = "edu-editor-double-build";
    final Key<Boolean> doubleBuildKey = Key.create(doubleBuildStringKey);

    private EditorFactoryImpl myDefaultEditorFactory;

    public EduEditorFactory(ProjectManager projectManager) {
        super(projectManager);
        myDefaultEditorFactory = new EditorFactoryImpl(projectManager);
    }

    @Override
    public void initComponent() {
        myDefaultEditorFactory.initComponent();
    }

    @Override
    public void disposeComponent() {
        myDefaultEditorFactory.disposeComponent();
    }

    @NotNull
    @Override
    public Document createDocument(@NotNull CharSequence text) {
        return myDefaultEditorFactory.createDocument(text);
    }

    @NotNull
    @Override
    public Document createDocument(@NotNull char[] text) {
        return myDefaultEditorFactory.createDocument(text);
    }

    @Override
    public Editor createEditor(@NotNull Document document) {
        return myDefaultEditorFactory.createEditor(document);
    }

    @Override
    public Editor createViewer(@NotNull Document document) {
        return myDefaultEditorFactory.createViewer(document);
    }

    @Override
    public Editor createEditor(@NotNull Document document, @Nullable Project project) {
        final VirtualFile vf = FileDocumentManager.getInstance().getFile(document);
        if (CourseManager.getInstance().isVirtualFileRegistered(vf) && (document.getUserData(doubleBuildKey) == null)){
            document.putUserData(doubleBuildKey, true);
            EduEditor eduEditor = (EduEditor) (new EduEditorProvider()).createEditor(project, vf);
            return eduEditor.getEditor();
        }
        else {
            return myDefaultEditorFactory.createEditor(document, project);
        }

    }

    @Override
    public Editor createEditor(@NotNull Document document, Project project, @NotNull FileType fileType, boolean isViewer) {
        return myDefaultEditorFactory.createEditor(document, project, fileType, isViewer);
    }

    @Override
    public Editor createEditor(@NotNull Document document, Project project, @NotNull VirtualFile file, boolean isViewer) {
        return myDefaultEditorFactory.createEditor(document, project, file, isViewer);
    }

    @Override
    public Editor createViewer(@NotNull Document document, @Nullable Project project) {
        return myDefaultEditorFactory.createViewer(document, project);
    }

    @Override
    public void releaseEditor(@NotNull Editor editor) {
        myDefaultEditorFactory.releaseEditor(editor);
    }

    @NotNull
    @Override
    public Editor[] getEditors(@NotNull Document document, @Nullable Project project) {
        return myDefaultEditorFactory.getEditors(document, project);
    }

    @NotNull
    @Override
    public Editor[] getEditors(@NotNull Document document) {
        return myDefaultEditorFactory.getEditors(document);
    }

    @NotNull
    @Override
    public Editor[] getAllEditors() {
        return myDefaultEditorFactory.getAllEditors();
    }

    @Override
    public void addEditorFactoryListener(@NotNull EditorFactoryListener listener) {
        myDefaultEditorFactory.addEditorFactoryListener(listener);

    }

    @Override
    public void addEditorFactoryListener(@NotNull EditorFactoryListener listener, @NotNull Disposable parentDisposable) {
        myDefaultEditorFactory.addEditorFactoryListener(listener, parentDisposable);
    }

    @Override
    public void removeEditorFactoryListener(@NotNull EditorFactoryListener listener) {
        myDefaultEditorFactory.removeEditorFactoryListener(listener);
    }

    @NotNull
    @Override
    public EditorEventMulticaster getEventMulticaster() {
        return myDefaultEditorFactory.getEventMulticaster();
    }

    @Override
    public void refreshAllEditors() {
        myDefaultEditorFactory.refreshAllEditors();
    }

    @NotNull
    @Override
    public String getComponentName() {
        return null;
    }
}
