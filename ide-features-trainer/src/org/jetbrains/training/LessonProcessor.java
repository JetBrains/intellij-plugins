package org.jetbrains.training;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.awt.RelativePoint;
import org.jdom.Element;
import org.jetbrains.training.graphics.DetailPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Created by karashevich on 30/01/15.
 */
public class LessonProcessor {

//    RECORDING FOR DISPOSABLE
//    private static boolean isRecording = false;

    public static void processLesson(final Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel) throws InterruptedException {
        if (lesson.getScn().equals(null)) {
            System.err.println("Scenario is empty or cannot be read!");
            return;
        }
        if (lesson.getScn().getRoot().equals(null)) {
            System.err.println("Scenario is empty or cannot be read!");
            return;
        }

        for (final Element element : lesson.getScn().getRoot().getChildren()) {

            Command cmd = CommandFactory.buildCommand(element);
            cmd.execute(element, lesson, editor, e, document, target, infoPanel);

        }
    }

}
