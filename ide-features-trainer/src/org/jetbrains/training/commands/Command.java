package org.jetbrains.training.commands;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.commandsEx.util.XmlUtil;
import org.jetbrains.training.eduUI.EduEditor;
import org.jetbrains.training.graphics.DetailPanel;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 30/01/15.
 */
public abstract class Command {


    private CommandType commandType;

        public enum CommandType {START, TEXT, TRY, TRYBLOCK, ACTION, REPLAY, NOCOMMAND, MOVECARET, TYPETEXT, COPYTEXT, TRAVERSECARET, MOUSEBLOCK, MOUSEUNBLOCK, WAIT, WIN}

    public Command(CommandType commandType) {
        this.commandType = commandType;
    }

    protected void updateDescription(String s, EduEditor eduEditor){
        eduEditor.addMessage(s);
    }

    protected void updateHTMLDescription(String htmlText, EduEditor eduEditor){
//        if (element.getAttribute("description") != null) {
//            String description =(element.getAttribute("description").getValue());
//            description = XmlUtil.replaceWithActionShortcut(description);
//
//            updateHTMLDescription(element, infoPanel, editor, description);
//        }
        htmlText = XmlUtil.replaceWithActionShortcut(htmlText);
        eduEditor.addMessage(htmlText);
    }

    protected void updateHTMLDescription(Element element, DetailPanel infoPanel, Editor editor, final String htmlText){

        String inputText = "<html>" + htmlText + "</html>";
        infoPanel.setText(inputText);

    }

    /**
     *
     * @return true if button is updated
     */
    //updateButton(element, elements, lesson, editor, e, document, target, infoPanel);
    protected boolean updateButton(ExecutionList executionList) throws InterruptedException {
        return true;
    }

    protected void initAgainButton(){
    }

    public abstract void execute(ExecutionList executionList) throws InterruptedException, ExecutionException;

    protected void startNextCommand(final ExecutionList executionList){
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    CommandFactory.buildCommand(executionList.getElements().peek()).execute(executionList);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (ExecutionException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
}
