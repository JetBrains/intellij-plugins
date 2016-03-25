package training.commands;

import com.intellij.openapi.application.ApplicationManager;
import training.ui.Message;
import training.learn.Lesson;
import training.learn.LessonManager;
import training.util.XmlUtil;

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

    void updateDescription(String s, Lesson lesson){
        LessonManager lessonManager = LessonManager.getInstance(lesson);
        if (lessonManager != null) lessonManager.addMessage(s);
    }

    void updateHTMLDescription(String htmlText, Lesson lesson){
//        if (element.getAttribute("description") != null) {
//            String description =(element.getAttribute("description").getValue());
//            description = XmlUtil.extractActions(description);
//
//            updateHTMLDescription(element, infoPanel, editor, description);
//        }
        final Message[] messages = XmlUtil.extractAll(new Message[]{new Message(htmlText, Message.MessageType.TEXT_REGULAR)});
        LessonManager lessonManager = LessonManager.getInstance(lesson);
        if (lessonManager != null) lessonManager.addMessage(messages);
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

    public abstract void execute(ExecutionList executionList) throws Exception;

    protected void startNextCommand(final ExecutionList executionList){
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                CommandFactory.buildCommand(executionList.getElements().peek()).execute(executionList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
