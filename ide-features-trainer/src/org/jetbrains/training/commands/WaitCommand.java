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
public class WaitCommand extends Command {

    public WaitCommand(){
        super(CommandType.WAIT);
    }

    @Override
    public void execute(final Queue<Element> elements, final Lesson lesson, final Editor editor, final AnActionEvent e, final Document document, final String target, final DetailPanel infoPanel) throws InterruptedException {

        Element element = elements.poll();
        updateDescription(element, infoPanel, editor);
        int delay = 1000;

        if (element.getAttribute("delay") != null) {
            delay = Integer.parseInt(element.getAttribute("delay").getValue());
        }

        final int finalDelay = delay;
//                (new Alarm(Alarm.ThreadToUse.POOLED_THREAD)).addRequest(new Runnable() {
//
//                    @Override
//
//                    public void run() {
//                        synchronized (editor) {
//                            try {
//                                CommandFactory.buildCommand(elements.peek()).execute(elements, lesson, editor, e, document, target, infoPanel);
//                            } catch (InterruptedException e1) {
//                                e1.printStackTrace();
//                            }
//                        }
//                    }
//                }, finalDelay);
//                synchronized (editor) {
//                    editor.wait();
//                }

        final int finalDelay1 = delay;
        Thread sleepThread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (editor) {
                    try {
                        System.err.println("run");
                        Thread.sleep(finalDelay1);
                        editor.notifyAll();
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
            startNextCommand(elements, lesson, editor, e, document, target ,infoPanel);
            System.err.println("stop wait");
        }
        sleepThread.join();

    }
}
