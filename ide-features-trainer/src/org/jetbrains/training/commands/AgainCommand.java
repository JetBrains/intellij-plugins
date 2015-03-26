package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.Command;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.graphics.DetailPanel;
import org.jetbrains.training.lesson.Lesson;

import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by karashevich on 30/01/15.
 *
 * From this command to TextCommand with an attribute again="true" all actions will be cached to Queue<Element> againChain
 *
 */
public class AgainCommand extends Command {

    public AgainCommand() {
        super(CommandType.AGAIN);
    }
    private Queue<Element> myElements;

    @Override
    public void execute(final Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, final Document document, final String target, final DetailPanel infoPanel, final MouseListenerHolder mouseListenerHolder) throws InterruptedException, ExecutionException {

//        againChain = new LinkedList<Element>(elements);

        final Element againElement = elements.peek();
        updateHTMLDescription(againElement, infoPanel, editor);

        //Show and program againButton
        infoPanel.showAgainButton();
        infoPanel.addAgainButtonAction(new Runnable() {
            @Override
            public void run() {
                infoPanel.hideAgainButton();
                addAgainToElements(againElement, elements);
                startNextCommand(myElements, lesson, editor, e, document, target, infoPanel, mouseListenerHolder);
            }
        });

        infoPanel.showButton();
        if (againElement.getAttribute("btn") != null) {
            final String buttonText = (againElement.getAttribute("btn").getValue());
            infoPanel.setButtonText(buttonText);
        }
        infoPanel.removeButtonActions();
        infoPanel.addButtonAction(new Runnable() {
            @Override
            public void run() {
                elements.poll();
                infoPanel.hideAgainButton();
                startNextCommand(elements, lesson, editor, e, document, target, infoPanel, mouseListenerHolder);
            }
        });

//        updateButton(againElement, elements, lesson, editor, e, document, target, infoPanel, mouseListenerHolder);

//        final String htmlText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
//        if (htmlText.equals("")) {
//            updateDescription(element, infoPanel, editor);
//        } else {
//            updateHTMLDescription(element, infoPanel, editor, htmlText);
//        }


    }

    private void addAgainToElements(Element element, Queue<Element> elements){
        myElements = new LinkedBlockingQueue<Element>();

        //add AgainBlock
        for (final Element el : element.getChildren()) {
            //if element is MouseBlocked (blocks all mouse events) than add all children inside it.
            if(isMouseBlock(el)) {
                if (el.getChildren() != null) {
                    myElements.add(el); //add block element
                    for(Element el1 : el.getChildren()){
                        myElements.add(el1); //add inner elements
                    }
                    myElements.add(new Element(Command.CommandType.MOUSEUNBLOCK.toString())); //add unblock element
                }
            } else {
                myElements.add(el);
            }
        }

        for (Element el: elements) {
            myElements.add(el);
        }

    }

    private static boolean isMouseBlock(Element el){
        return el.getName().toUpperCase().equals(Command.CommandType.MOUSEBLOCK.toString());
    }

}



