package org.jetbrains.training;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.graphics.DetailPanel;

/**
 * Created by karashevich on 30/01/15.
 */
public abstract class Command {


    private CommandType commandType;
    public enum CommandType {START, TEXT, TRY, ACTION, NOCOMMAND, MOVECARET, TYPETEXT, COPYTEXT}

    public Command(CommandType commandType) {
        this.commandType = commandType;
    }

    protected void updateDescription(Element element, DetailPanel infoPanel, Editor editor){
        if (element.getAttribute("description") != null) {
            final String description =(element.getAttribute("description").getValue().toString());
            infoPanel.setText(description);
        }
    }

    /**
     *
     * @return true if button is updated
     */
    protected boolean updateButton(Element element, DetailPanel infoPanel, Editor editor){
        if (element.getAttribute("btn") != null) {
            final String buttonText =(element.getAttribute("btn").getValue().toString());
            infoPanel.showButton();
            infoPanel.setButtonText(buttonText);
            infoPanel.addWaitToButton(editor);
            return true;
        } else {
            infoPanel.hideButton();
            return false;
        }
    }

    public abstract void execute(Element element, final Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel) throws InterruptedException;
}
