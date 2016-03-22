package training.commands;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import training.editor.MouseListenerHolder;
import training.editor.EduEditor;
import training.learn.Lesson;

import java.util.Queue;

/**
 * Created by karashevich on 02/07/15.
 */
public class ExecutionList {

    private Queue<Element> elements;
    final private Lesson lesson;
    final private Project project;
    final private Editor editor;
    final private MouseListenerHolder mouseListenerHolderl;
    @Nullable final private String target;

    public ExecutionList(Queue<Element> elements, Lesson lesson, Project project, Editor editor, MouseListenerHolder mouseListenerHolder, String target) {
        this.elements = elements;
        this.lesson = lesson;
        this.editor = editor;
        this.project = project;
        this.mouseListenerHolderl = mouseListenerHolder;
        this.target = target;
    }

    public Project getProject() {
        return project;
    }

    public Editor getEditor() {
        return editor;
    }

    public Queue<Element> getElements() {
        return elements;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public MouseListenerHolder getMouseListenerHolderl() {
        return mouseListenerHolderl;
    }

    @Nullable
    public String getTarget() {
        return target;
    }
}
