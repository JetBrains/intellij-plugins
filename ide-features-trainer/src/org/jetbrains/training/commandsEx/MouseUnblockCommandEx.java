package org.jetbrains.training.commandsEx;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.graphics.DetailPanel;
import org.jetbrains.training.lesson.Lesson;

import java.util.Queue;

/**
 * Created by karashevich on 30/01/15.
 */
public class MouseUnblockCommandEx extends CommandEx {

    public MouseUnblockCommandEx(){
        super(CommandType.MOUSEUNBLOCK);
    }

    @Override
    public void execute(Queue<Element> elements, Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel, MouseListenerHolder mouseListenerHolder) {
        //Unblock mouse and perform next
//        mouseListenerHolder.restoreListeners(editor);
        elements.poll();
        startNextCommand(elements, lesson, editor, e, document, target ,infoPanel, mouseListenerHolder);
    }
}
