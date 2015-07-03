package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.eduUI.EduEditor;
import org.jetbrains.training.lesson.Lesson;

import java.util.Queue;

/**
 * Created by karashevich on 02/07/15.
 */
public class ExecutionList {

    private Queue<Element> elements;
    final private Lesson lesson;
    final private Editor editor;
    final private AnActionEvent anActionEvent;
    final private EduEditor eduEditor;
    final private MouseListenerHolder mouseListenerHolderl;
    @Nullable final private String target;

    public ExecutionList(Queue<Element> elements, Lesson lesson,AnActionEvent anActionEvent, EduEditor eduEditor, MouseListenerHolder mouseListenerHolderl, String target) {
        this.elements = elements;
        this.lesson = lesson;
        this.eduEditor = eduEditor;
        this.editor = eduEditor.getEditor();
        this.anActionEvent = anActionEvent;
        this.mouseListenerHolderl = mouseListenerHolderl;
        this.target = target;
    }

    public AnActionEvent getAnActionEvent() {
        return anActionEvent;
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
