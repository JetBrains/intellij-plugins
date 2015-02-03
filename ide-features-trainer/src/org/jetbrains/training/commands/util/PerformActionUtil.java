package org.jetbrains.training.commands.util;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.ide.PooledThreadExecutor;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by karashevich on 02/02/15.
 */
public class PerformActionUtil {

    /**
     * Some util method for <i>performAction</i> method
     * @param actionName - please see it in <i>performAction</i> method
     * @return
     */
    public static InputEvent getInputEvent(String actionName) {
        final Shortcut[] shortcuts = KeymapManager.getInstance().getActiveKeymap().getShortcuts(actionName);
        KeyStroke keyStroke = null;
        for (Shortcut each : shortcuts) {
            if (each instanceof KeyboardShortcut) {
                keyStroke = ((KeyboardShortcut)each).getFirstKeyStroke();
                if (keyStroke != null) break;
            }
        }

        if (keyStroke != null) {
            return new KeyEvent(JOptionPane.getRootFrame(),
                    KeyEvent.KEY_PRESSED,
                    System.currentTimeMillis(),
                    keyStroke.getModifiers(),
                    keyStroke.getKeyCode(),
                    keyStroke.getKeyChar(),
                    KeyEvent.KEY_LOCATION_STANDARD);
        } else {
            return new MouseEvent(JOptionPane.getRootFrame(), MouseEvent.MOUSE_PRESSED, 0, 0, 0, 0, 1, false, MouseEvent.BUTTON1);
        }
    }

    /**
     * performing internal platform action
     * @param actionName - name of IntelliJ Action. For full list please see http://git.jetbrains.org/?p=idea/community.git;a=blob;f=platform/platform-api/src/com/intellij/openapi/actionSystem/IdeActions.java;hb=HEAD
     */
    public static void performAction(String actionName, Runnable actionCallBack){
        final ActionManager am = ActionManager.getInstance();
        final AnAction targetAction = am.getAction(actionName);
        final InputEvent inputEvent = getInputEvent(actionName);

        am.tryToExecute(targetAction, inputEvent, null, null, false).doWhenDone(actionCallBack);
    }

    public static void performAction(String actionName, final Editor editor) throws InterruptedException {
        final ActionManager am = ActionManager.getInstance();
        final AnAction targetAction = am.getAction(actionName);
        final InputEvent inputEvent = getInputEvent(actionName);


        WriteCommandAction.runWriteCommandAction(editor.getProject(), new Runnable() {
            @Override
            public void run() {
                am.tryToExecute(targetAction, inputEvent, null, null, false).doWhenDone(new Runnable() {
                    @Override
                    public void run() {
                        System.err.println("try to execute");
                        synchronized (editor) {
                            editor.notify();
                        }
                    }
                });
            }
        });
        synchronized (editor) {
            editor.wait();
        }
    }

    public static void sleepHere(final Editor editor, final int delay) throws InterruptedException {

        Thread sleepThread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (editor) {
                    try {
                        System.err.println("run");
                        Thread.sleep(delay);
                        editor.notify();
                        System.err.println("stop");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        sleepThread.start();
        synchronized(editor){
            System.err.println("wait");
            editor.wait();
            System.err.println("stop wait");
        }
        sleepThread.join();


//        alarm.addRequest(new Runnable() {
//            @Override
//            public void run() {
//                synchronized (editor) {
//                    System.err.println("run");
//                    editor.notifyAll();
//                }
//            }
//        }, delay);
//
//        synchronized(editor) {
//                try {
//                    System.err.println("waited");
//                    editor.wait();
//                    System.err.println("stop waited");
//                } catch (InterruptedException e) {
//            }
//        }
    }
}
