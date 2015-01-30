package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.Command;
import org.jetbrains.training.Lesson;
import org.jetbrains.training.graphics.DetailPanel;

/**
 * Created by karashevich on 30/01/15.
 */
public class NoCommand extends Command {

    public NoCommand(){
        super(CommandType.NOCOMMAND);
    }

    @Override
    public void execute(Element element, Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel) {
        //do nothing
    }
}
