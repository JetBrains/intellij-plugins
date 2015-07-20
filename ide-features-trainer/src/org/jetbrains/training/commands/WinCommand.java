package org.jetbrains.training.commands;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import org.jdom.Element;
import org.jetbrains.training.BadCourseException;
import org.jetbrains.training.BadLessonException;
import org.jetbrains.training.LessonIsOpenedException;
import org.jetbrains.training.commandsEx.CommandEx;
import org.jetbrains.training.editor.MouseListenerHolder;
import org.jetbrains.training.graphics.DetailPanel;
import org.jetbrains.training.lesson.Lesson;

import java.awt.*;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 30/01/15.
 */
public class WinCommand extends Command {

    public WinCommand(){
        super(CommandType.WIN);
    }

    @Override
    public void execute(ExecutionList executionList) throws InterruptedException {

        Element element = executionList.getElements().poll();
        executionList.getLesson().setPassed(true);
        executionList.getEduEditor().passLesson(executionList.getLesson());

    }
}
