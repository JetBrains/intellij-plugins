package training.commandsEx;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import training.editor.MouseListenerHolder;
import training.graphics.DetailPanel;
import training.lesson.Lesson;

import java.util.Queue;

/**
 * Created by karashevich on 30/01/15.
 */
public class MouseBlockCommandEx extends CommandEx {

    public MouseBlockCommandEx(){
        super(CommandType.MOUSEBLOCK);
    }

    @Override
    public void execute(Queue<Element> elements, Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel, MouseListenerHolder mouseListenerHolder) {
        //Block mouse and perform next
//        mouseListenerHolder.grabListeners(editor);

        elements.poll();
        startNextCommand(elements, lesson, editor, e, document, target, infoPanel, mouseListenerHolder);

    }
}
