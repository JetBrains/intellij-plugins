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
import org.jetbrains.training.keymap.KeymapUtil;
import org.jetbrains.training.keymap.SubKeymapUtil;
import org.jetbrains.training.lesson.Lesson;

import javax.swing.*;
import javax.xml.bind.SchemaOutputResolver;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by karashevich on 30/01/15.
 */
public class TryBlockCommand extends Command {

    public TryBlockCommand(){
        super(CommandType.TRYBLOCK);
    }

    private Queue<Element> myElements = null;

    @Override
    public void execute(Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel, MouseListenerHolder mouseListenerHolder) {

        Element element = elements.poll();
//        updateDescription(element, infoPanel, editor);
        try {
            buildHintPanel(element, lesson, editor);
            addTryCommands(elements, element);

            startNextCommand(myElements, lesson, editor, e, document, target, infoPanel, mouseListenerHolder);

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
                String hintText = subElement.getAttribute("hint").getValue();
                if (subElement.getAttribute("action") != null) {
                    hintText = resolveShortcut(subElement.getAttribute("hint").getValue(),subElement.getAttribute("action").getValue());
                }
                stringArrayList.add(hintText);
            }
        }
        String[] strings = new String[stringArrayList.size()];
        strings = stringArrayList.toArray(strings);

        lesson.hintPanel = new HintPanel(strings);
        lesson.hintPanel.showIt(editor);
    }

    private void addTryCommands(Queue<Element> elements, Element element){

        myElements = new LinkedBlockingQueue<Element>();

        for (Element subElement: element.getChildren()) {
            if(subElement.getName().toUpperCase().equals(CommandType.TRY.toString())) {
                myElements.add(subElement);
            }

        }

        for (Element el: elements) {
            myElements.add(el);
        }
    }

    private String resolveShortcut(String text, String actionId){
        final KeyStroke shortcutByActionId = KeymapUtil.getShortcutByActionId(actionId);
        final String shortcutText = SubKeymapUtil.getKeyStrokeTextSub(shortcutByActionId);
        return substitution(text, shortcutText);
    }

    public static String substitution(String text, String shortcutString){
        if (text.contains(ActionCommand.SHORTCUT)) {
            return text.replace(ActionCommand.SHORTCUT, shortcutString);
        } else {
            return text;
        }
    }
}
