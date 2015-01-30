package org.jetbrains.training.commands;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Disposer;
import org.jdom.Element;
import org.jetbrains.training.ActionsRecorder;
import org.jetbrains.training.Command;
import org.jetbrains.training.Lesson;
import org.jetbrains.training.graphics.DetailPanel;

/**
 * Created by karashevich on 30/01/15.
 */
public class TryCommand extends Command {

    public TryCommand(){
        super(CommandType.TRY);
    }

    @Override
    public void execute(Element element, final Lesson lesson, final Editor editor, final AnActionEvent e, Document document, String target, final DetailPanel infoPanel) throws InterruptedException {

        updateDescription(element, infoPanel, editor);
        updateButton(element, infoPanel, editor);

        final ActionsRecorder recorder = new ActionsRecorder(e.getProject(), document, target);
        //TODO: Make recorder disposable

        recorder.startRecording(new Runnable() {
            @Override
            public void run() {
                infoPanel.setText("Awesome, now you know how to duplicate lines easily!");
                infoPanel.greenalize();
                lesson.pass();
            }
        });


    }


}
