package org.jetbrains.training.lesson;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.commands.Command;
import org.jetbrains.training.commands.CommandFactory;
import org.jetbrains.training.commands.ExecutionList;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.editor.EduEditor;

import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by karashevich on 30/01/15.
 */
public class LessonProcessor {

    public static void process(final Lesson lesson, final EduEditor eduEditor, final Project project, Document document, @Nullable String target) throws InterruptedException, ExecutionException {

        Queue<Element> elements = new LinkedBlockingQueue<Element>();
        if (lesson.getScn().equals(null)) {
            System.err.println("Scenario is empty or cannot be read!");
            return;
        }

        if (lesson.getScn().getRoot().equals(null)) {
            System.err.println("Scenario is empty or cannot be read!");
            return;
        }

        //Create queue of Actions
        for (final Element el : lesson.getScn().getRoot().getChildren()) {
            //if element is MouseBlocked (blocks all mouse events) than add all children inside it.
            if(isMouseBlock(el)) {
                if (el.getChildren() != null) {
                    elements.add(el); //add block element
                    for(Element el1 : el.getChildren()){
                        if (isCaretBlock(el1)) {
                            if (el1.getChildren() != null) {
                                elements.add(el1); //add block element
                                for (Element el2 : el1.getChildren()) {
                                    elements.add(el2); //add inner elements
                                }
                                elements.add(new Element(Command.CommandType.CARETUNBLOCK.toString())); //add unblock element
                            }
                        } else {
                            elements.add(el1); //add inner elements
                        }
                    }
                    elements.add(new Element(Command.CommandType.MOUSEUNBLOCK.toString())); //add unblock element
                }
            } else if (isCaretBlock(el)) {
                if (el.getChildren() != null) {
                    elements.add(el); //add block element
                    for(Element el1 : el.getChildren()){
                        elements.add(el1); //add inner elements
                    }
                    elements.add(new Element(Command.CommandType.CARETUNBLOCK.toString())); //add unblock element
                }
            } else {
                elements.add(el);
            }
        }

        MouseListenerHolder mouseListenerHolder = new MouseListenerHolder();

        //Initialize ALL LESSONS in EduEditor in this course
        eduEditor.initLesson(lesson);



        //Perform first action, all next perform like a chain reaction
        Command cmd = CommandFactory.buildCommand(elements.peek());
        ExecutionList executionList = new ExecutionList(elements, lesson, project, eduEditor, mouseListenerHolder, target);

        cmd.execute(executionList);

    }

    private static boolean isMouseBlock(Element el){
        return el.getName().toUpperCase().equals(Command.CommandType.MOUSEBLOCK.toString());
    }

    private static boolean isCaretBlock(Element el){
        return el.getName().toUpperCase().equals(Command.CommandType.CARETBLOCK.toString());
    }


}
