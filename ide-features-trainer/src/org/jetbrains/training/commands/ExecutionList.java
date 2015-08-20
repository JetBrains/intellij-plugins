package org.jetbrains.training.commands;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.editor.EduEditor;
import org.jetbrains.training.lesson.Lesson;

import java.util.Queue;

/**
 * Created by karashevich on 02/07/15.
 */
public class ExecutionList {

    private Queue<Element> elements;
    final private Lesson lesson;
    final private Editor editor;
    final private Project project;
    final private EduEditor eduEditor;
    final private MouseListenerHolder mouseListenerHolderl;
    @Nullable final private String target;

    public ExecutionList(Queue<Element> elements, Lesson lesson, Project project, EduEditor eduEditor, MouseListenerHolder mouseListenerHolder, String target) {
        this.elements = elements;
        this.lesson = lesson;
        this.eduEditor = eduEditor;
        this.editor = eduEditor.getEditor();
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

    public EduEditor getEduEditor() {
        return eduEditor;
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
