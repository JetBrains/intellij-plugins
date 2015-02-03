package org.jetbrains.training;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.graphics.DetailPanel;

import java.util.Queue;

/**
 * Created by karashevich on 30/01/15.
 */
public abstract class Command {


    private CommandType commandType;
    public enum CommandType {START, TEXT, TRY, ACTION, NOCOMMAND, MOVECARET, TYPETEXT, COPYTEXT, TRAVERSECARET, WAIT}

    public Command(CommandType commandType) {
        this.commandType = commandType;
        System.err.println(">>COMMAND" + commandType);
    }

    protected void updateDescription(Queue<Element> elements, DetailPanel infoPanel, Editor editor){
        if (elements.peek().getAttribute("description") != null) {
            final String description =(elements.peek().getAttribute("description").getValue());
            infoPanel.setText(description);
        }
    }
    protected void updateDescription(Element element, DetailPanel infoPanel, Editor editor){
        if (element.getAttribute("description") != null) {
            final String description =(element.getAttribute("description").getValue());
            infoPanel.setText(description);
        }
    }

    /**
     *
     * @return true if button is updated
     */
    //updateButton(element, elements, lesson, editor, e, document, target, infoPanel);
    protected boolean updateButton(Element element, Queue<Element> elements, Lesson lesson, Editor editor, AnActionEvent e, Document document, String target, DetailPanel infoPanel) throws InterruptedException {
        if (element.getAttribute("btn") != null) {
            final String buttonText =(element.getAttribute("btn").getValue());
            infoPanel.showButton();
            infoPanel.setButtonText(buttonText);
            infoPanel.addButtonAction(elements, lesson, editor, e, document, target, infoPanel);
            return true;
        } else {
            infoPanel.hideButton();
            return false;
        }
    }

    public abstract void execute(Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel) throws InterruptedException;
}
