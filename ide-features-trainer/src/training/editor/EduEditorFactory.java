package training.editor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.editor.ex.EditReadOnlyListener;
import com.intellij.openapi.editor.ex.ErrorStripeListener;
import com.intellij.openapi.editor.ex.FocusChangeListener;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.editor.impl.EditorFactoryImpl;
import com.intellij.openapi.editor.impl.event.EditorEventMulticasterImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.text.CharArrayCharSequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.lesson.CourseManager;
import training.lesson.Lesson;
import training.lesson.LessonProcessor;

import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by karashevich on 11/11/15.
 */
public class EduEditorFactory extends EditorFactoryImpl implements ApplicationComponent {

    final private static String doubleBuildStringKey = "edu-editor-double-build";
    final Key<Boolean> doubleBuildKey = Key.create(doubleBuildStringKey);

    private EditorFactoryImpl myDefaultEditorFactory;

    public EduEditorFactory() throws CloneNotSupportedException {
        super(ProjectManager.getInstance());
        //        this.myDefaultEditorFactory = new EditorFactoryImpl(projectManager);
        this.myDefaultEditorFactory = (EditorFactoryImpl) EditorFactoryImpl.getInstance();
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
    public Document createDocument(@NotNull char[] text) {
        return myDefaultEditorFactory.createDocument(new CharArrayCharSequence(text));
    }

    @NotNull
    @Override
    public Document createDocument(@NotNull CharSequence text) {
        DocumentImpl document = new DocumentImpl(text);
        ((EditorEventMulticasterImpl) myDefaultEditorFactory.getEventMulticaster()).registerDocument(document);
        return document;
    }

    @NotNull
    @Override
    public Document createDocument(boolean allowUpdatesWithoutWriteAction) {
        DocumentImpl document = new DocumentImpl("", allowUpdatesWithoutWriteAction);
        ((EditorEventMulticasterImpl) myDefaultEditorFactory.getEventMulticaster()).registerDocument(document);
        return document;
    }

    @NotNull
    @Override
    public Document createDocument(@NotNull CharSequence text, boolean acceptsSlashR, boolean allowUpdatesWithoutWriteAction) {
        DocumentImpl document = new DocumentImpl(text, acceptsSlashR, allowUpdatesWithoutWriteAction);
        ((EditorEventMulticasterImpl) myDefaultEditorFactory.getEventMulticaster()).registerDocument(document);
        return document;
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
        if (CourseManager.getInstance().isVirtualFileRegistered(vf) && (document.getUserData(doubleBuildKey) == null || !document.getUserData(doubleBuildKey))){
            document.putUserData(doubleBuildKey, true);
            EduEditor eduEditor = (EduEditor) (new EduEditorProvider()).createEditor(project, vf);
            return eduEditor.getEditor();
        }
        else {
            Editor resultEditor = myDefaultEditorFactory.createEditor(document, project, vf, false);
            document.putUserData(doubleBuildKey, false);
            return resultEditor;
        }

    }

    @Override
    public Editor createViewer(@NotNull Document document, @Nullable Project project) {
        return myDefaultEditorFactory.createViewer(document, project);
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

    public void cloneEventMulticaster(Project project) throws Exception {

    }

    @Override
    public void releaseEditor(@NotNull Editor editor) {
        if (!(myDefaultEditorFactory instanceof EduEditorFactory))

            myDefaultEditorFactory.releaseEditor(editor);
//        else
//            super.releaseEditor(editor);
    }

    public void cloneEventMulticasterEx(Project project) throws Exception {
        final EditorEventMulticasterImpl clonedEventMulticaster =  (EditorEventMulticasterImpl) EditorFactoryImpl.getInstance().getEventMulticaster();
        final Map<Class, List> listeners = clonedEventMulticaster.getListeners();
        final Collection<List> collectionOfListOfListeners = listeners.values();
        EditorEventMulticasterImpl myEventMulticaster = (EditorEventMulticasterImpl) getEventMulticaster();
        for (List listOfListeners : collectionOfListOfListeners) {
            if (listOfListeners.size() > 0) {
                if (listOfListeners.get(0) instanceof DocumentListener) {
                    for (Object eventListener: listOfListeners)
                        myEventMulticaster.addDocumentListener((DocumentListener) eventListener, project);
                } else
                if (listOfListeners.get(0) instanceof CaretListener) {
                    for (Object eventListener: listOfListeners)
                        myEventMulticaster.addCaretListener((CaretListener) eventListener, project);
                } else
                if (listOfListeners.get(0) instanceof EditorMouseListener) {
                    for (Object eventListener: listOfListeners)
                        myEventMulticaster.addEditorMouseListener((EditorMouseListener) eventListener, project);
                } else
                if (listOfListeners.get(0) instanceof EditorMouseMotionListener) {
                    for (Object eventListener: listOfListeners)
                        myEventMulticaster.addEditorMouseMotionListener((EditorMouseMotionListener) eventListener, project);
                } else
                if (listOfListeners.get(0) instanceof SelectionListener) {
                    for (Object eventListener: listOfListeners)
                        myEventMulticaster.addSelectionListener((SelectionListener) eventListener, project);
                } else
                if (listOfListeners.get(0) instanceof VisibleAreaListener) {
                    for (Object eventListener: listOfListeners)
                        myEventMulticaster.addVisibleAreaListener((VisibleAreaListener) eventListener);
                } else
                if (listOfListeners.get(0) instanceof ErrorStripeListener) {
                    for (Object eventListener: listOfListeners)
                        myEventMulticaster.addErrorStripeListener((ErrorStripeListener) eventListener);
                } else
                if (listOfListeners.get(0) instanceof EditReadOnlyListener) {
                    for (Object eventListener: listOfListeners)
                        myEventMulticaster.addEditReadOnlyListener((EditReadOnlyListener) eventListener);
                } else
                if (listOfListeners.get(0) instanceof PropertyChangeListener) {
                    for (Object eventListener: listOfListeners)
                        myEventMulticaster.addPropertyChangeListener((PropertyChangeListener) eventListener);
                } else
                if (listOfListeners.get(0) instanceof FocusChangeListener) {
                    for (Object eventListener: listOfListeners)
                        myEventMulticaster.addFocusChangeListner((FocusChangeListener) eventListener, project);
                } else {
                    //if this exception is thrown you shoukd update list of "if statements" please see EditorEventMulticasterImpl class
                    throw new Exception("Unexpected type of event listener: " + listOfListeners.get(0).getClass());
                }
            }
        }
    }
}
