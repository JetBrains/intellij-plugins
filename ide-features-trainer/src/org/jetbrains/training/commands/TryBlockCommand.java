package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.ActionsRecorder;
import org.jetbrains.training.Command;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.graphics.DetailPanel;
import org.jetbrains.training.graphics.HintPanel;
import org.jetbrains.training.graphics.ShowHint;
import org.jetbrains.training.lesson.Lesson;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by karashevich on 30/01/15.
 */
public class TryBlockCommand extends Command {

    public TryBlockCommand(){
        super(CommandType.TRYBLOCK);
    }

    @Override
    public void execute(Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel, MouseListenerHolder mouseListenerHolder) {

        Element element = elements.poll();
//        updateDescription(element, infoPanel, editor);
        try {
            buildHintPanel(element, lesson, editor);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (FontFormatException e1) {
            e1.printStackTrace();
        }

    }



    private void buildHintPanel(Element element, Lesson lesson, Editor editor) throws IOException, FontFormatException {
        ArrayList<String> stringArrayList = new ArrayList<String>();
        for (Element subElement: element.getChildren()) {
            if(subElement.getAttribute("hint") != null) {
                stringArrayList.add(element.getAttribute("hint").getValue());
            }
        }
        String[] strings = new String[stringArrayList.size()];
        strings = stringArrayList.toArray(strings);

        lesson.hintPanel = new HintPanel(strings);
        ShowHint.showHintPanel(lesson.hintPanel, editor);
    }
}
