package org.jetbrains.training.lesson;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.commands.Command;
import org.jetbrains.training.commands.CommandFactory;
import org.jetbrains.training.commands.ExecutionList;
import org.jetbrains.training.commandsEx.CommandEx;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.eduUI.EduEditor;

import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by karashevich on 30/01/15.
 */
public class LessonProcessor {

    public static void process(final Lesson lesson, final EduEditor eduEditor, final AnActionEvent e, Document document, @Nullable String target) throws InterruptedException, ExecutionException {

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
                        elements.add(el1); //add inner elements
                    }
                    elements.add(new Element(Command.CommandType.MOUSEUNBLOCK.toString())); //add unblock element
                }
            } else {
                elements.add(el);
            }
        }

        //Perform first action, all next perform like a chain reaction
        MouseListenerHolder mouseListenerHolder = new MouseListenerHolder();


        Command cmd = CommandFactory.buildCommand(elements.peek());
        ExecutionList executionList = new ExecutionList(elements, lesson, e, eduEditor, mouseListenerHolder, target);

        cmd.execute(executionList);

    }

    private static boolean isMouseBlock(Element el){
        return el.getName().toUpperCase().equals(CommandEx.CommandType.MOUSEBLOCK.toString());
    }


}
