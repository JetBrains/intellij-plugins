package training.commandsEx;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import training.util.XmlUtil;
import training.editor.MouseListenerHolder;
import training.graphics.DetailPanel;
import training.lesson.Lesson;

import java.util.Queue;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 30/01/15.
 */
public abstract class CommandEx {


    private CommandType commandType;

        public enum CommandType {START, TEXT, TRY, TRYBLOCK, ACTION, REPLAY, NOCOMMAND, MOVECARET, TYPETEXT, COPYTEXT, TRAVERSECARET, MOUSEBLOCK, MOUSEUNBLOCK, WAIT, WIN}

    public CommandEx(CommandType commandType) {
        this.commandType = commandType;
    }

    protected void updateDescription(Element element, DetailPanel infoPanel, Editor editor){
        if (element.getAttribute("description") != null) {
            String description =(element.getAttribute("description").getValue());
            description = XmlUtil.replaceWithActionShortcut(description);
            infoPanel.setText(description);
        }
    }

    protected void updateHTMLDescription(Element element, DetailPanel infoPanel, Editor editor){
        if (element.getAttribute("description") != null) {
            String description =(element.getAttribute("description").getValue());
            description = XmlUtil.replaceWithActionShortcut(description);

            updateHTMLDescription(element, infoPanel, editor, description);
        }
    }

    protected void updateHTMLDescription(Element element, DetailPanel infoPanel, Editor editor, final String htmlText){

        String inputText = "<html>" + htmlText + "</html>";
        inputText = XmlUtil.replaceWithActionShortcut(inputText);

        infoPanel.setText(inputText);

    }

    /**
     *
     * @return true if button is updated
     */
    //updateButton(element, elements, lesson, editor, e, document, target, infoPanel);
    protected boolean updateButton(Element element, final Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, final Document document, final String target, final DetailPanel infoPanel, final MouseListenerHolder mouseListenerHolder) throws InterruptedException {
        if (element.getAttribute("btn") != null) {
            final String buttonText =(element.getAttribute("btn").getValue());
            infoPanel.showButton();
            infoPanel.setButtonText(buttonText);
            infoPanel.addButtonAction(new Runnable() {
                @Override
                public void run() {
                    startNextCommand(elements, lesson, editor, e, document, target, infoPanel, mouseListenerHolder);
                }
            });
            return true;
        } else {
            infoPanel.hideButton();
            return false;
        }
    }

    protected void initAgainButton(Element element, DetailPanel infoPanel){
        if(element.getAttribute(CommandType.REPLAY.toString().toUpperCase()) != null) {
            if (element.getAttribute(CommandType.REPLAY.toString().toUpperCase()).getValue().toUpperCase().equals("true".toUpperCase()))
            infoPanel.showReplayButton();
            else if (element.getAttribute(CommandType.REPLAY.toString().toUpperCase()).getValue().toUpperCase().equals("false".toUpperCase()))
            infoPanel.hideReplayButton();
        } else {
            infoPanel.hideReplayButton();
        }
    }

    public abstract void execute(Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel, MouseListenerHolder mouseListenerHolder) throws InterruptedException, ExecutionException;

    protected void startNextCommand(final Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, final Document document, final String target, final DetailPanel infoPanel, final MouseListenerHolder mouseListenerHolder){
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    CommandFactoryEx.buildCommand(elements.peek()).execute(elements, lesson, editor, e, document, target, infoPanel, mouseListenerHolder);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                } catch (ExecutionException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
}
