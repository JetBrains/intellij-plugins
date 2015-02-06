package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.Command;
import org.jetbrains.training.Lesson;
import org.jetbrains.training.graphics.DetailPanel;

import java.util.Queue;

/**
 * Created by karashevich on 30/01/15.
 */
public class TextCommand extends Command {

    public TextCommand(){
        super(CommandType.TEXT);
    }

    @Override
    public void execute(Queue<Element> elements, Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel) throws InterruptedException {

        Element element = elements.poll();
        updateDescription(element, infoPanel, editor);
        updateButton(element, elements, lesson, editor, e, document, target, infoPanel);

        final String htmlText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
        if (htmlText.equals("")) {
            updateDescription(element, infoPanel, editor);
        } else {
            updateHTMLDescription(element, infoPanel, editor, htmlText);
        }



    }


}
