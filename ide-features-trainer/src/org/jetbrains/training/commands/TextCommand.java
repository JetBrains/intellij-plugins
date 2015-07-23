package org.jetbrains.training.commands;

import org.jdom.Element;
import org.jetbrains.training.ActionsRecorder;
import org.jetbrains.training.commandsEx.ActionCommandEx;
import org.jetbrains.training.keymap.KeymapUtil;
import org.jetbrains.training.keymap.SubKeymapUtil;
import org.jetbrains.training.lesson.Lesson;
import org.jetbrains.training.util.MyClassLoader;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

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
//        updateDescription(element, infoPanel, editor);

        String htmlText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
        if (htmlText.isEmpty()) htmlText = element.getAttribute("description").getValue();

        if (htmlText.equals("")) {
            updateDescription(htmlText, executionList.getEduEditor());
        } else {
            updateHTMLDescription(htmlText, executionList.getEduEditor());
        }

        startNextCommand(executionList);

    }

}
