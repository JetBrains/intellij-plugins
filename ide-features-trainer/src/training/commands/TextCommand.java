package training.commands;

import org.jdom.Element;
import training.learn.Lesson;

/**
 * Created by karashevich on 30/01/15.
 */
public class TextCommand extends Command {

    public TextCommand(){
        super(CommandType.TEXT);
    }

    @Override
    public void execute(ExecutionList executionList) throws InterruptedException {


        Element element = executionList.getElements().poll();
        Lesson lesson = executionList.getLesson();

        String htmlText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
        if (htmlText.isEmpty()) htmlText = element.getAttribute("description").getValue();

        if (htmlText.equals("")) {
            updateDescription(htmlText, lesson);
        } else {
            updateHTMLDescription(htmlText, lesson);
        }

        startNextCommand(executionList);

    }

}
