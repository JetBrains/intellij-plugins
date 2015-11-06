package training.commands;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import training.editor.EduEditor;
import training.editor.eduUI.Message;
import training.util.XmlUtil;

import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 30/01/15.
 */
public abstract class Command {


    private CommandType commandType;

        public enum CommandType {START, TEXT, TRY, TRYBLOCK, ACTION, REPLAY, NOCOMMAND, MOVECARET, TYPETEXT, COPYTEXT, TRAVERSECARET, MOUSEBLOCK, MOUSEUNBLOCK, WAIT, CARETBLOCK, CARETUNBLOCK, SHOWLINENUMBER, EXPANDALLBLOCKS, WIN, TEST, SETSELECTION}

    public Command(CommandType commandType) {
        this.commandType = commandType;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    protected void updateDescription(String s, EduEditor eduEditor){
        eduEditor.addMessage(s);
    }

    protected void updateHTMLDescription(String htmlText, EduEditor eduEditor){
//        if (element.getAttribute("description") != null) {
//            String description =(element.getAttribute("description").getValue());
//            description = XmlUtil.extractActions(description);
//
//            updateHTMLDescription(element, infoPanel, editor, description);
//        }
        final Message[] messages = XmlUtil.extractAll(new Message[]{new Message(htmlText, Message.MessageType.TEXT_REGULAR)});
        eduEditor.addMessage(messages);
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

    public abstract void execute(ExecutionList executionList) throws InterruptedException, ExecutionException, BadCommandException;

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
                } catch (BadCommandException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
