package org.jetbrains.training;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.graphics.DetailPanel;

import java.util.Queue;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 30/01/15.
 */
public abstract class Command {


    private CommandType commandType;
    public enum CommandType {START, TEXT, TRY, ACTION, NOCOMMAND, MOVECARET, TYPETEXT, COPYTEXT, TRAVERSECARET, WAIT}

    public Command(CommandType commandType) {
        this.commandType = commandType;
    }

    protected void updateDescription(Element element, DetailPanel infoPanel, Editor editor){
        if (element.getAttribute("description") != null) {
            final String description =(element.getAttribute("description").getValue());
            infoPanel.setText(description);
        }
    }

    protected void updateHTMLDescription(Element element, DetailPanel infoPanel, Editor editor, final String htmlText){

        final String inputText = "<html>" + htmlText + "</html>";
        infoPanel.setText(inputText);

    }

    /**
     *
     * @return true if button is updated
     */
    //updateButton(element, elements, lesson, editor, e, document, target, infoPanel);
    protected boolean updateButton(Element element, final Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, final Document document, final String target, final DetailPanel infoPanel) throws InterruptedException {
        if (element.getAttribute("btn") != null) {
            final String buttonText =(element.getAttribute("btn").getValue());
            infoPanel.showButton();
            infoPanel.setButtonText(buttonText);
            infoPanel.addButtonAction(new Runnable() {
                @Override
                public void run() {
                    startNextCommand(elements, lesson, editor, e, document, target, infoPanel);
                }
            });
            return true;
        } else {
            infoPanel.hideButton();
            return false;
        }
    }

    public abstract void execute(Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel) throws InterruptedException, ExecutionException;

    protected void startNextCommand(final Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, final Document document, final String target, final DetailPanel infoPanel){
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    CommandFactory.buildCommand(elements.peek()).execute(elements, lesson, editor, e, document, target, infoPanel);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (ExecutionException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
}
