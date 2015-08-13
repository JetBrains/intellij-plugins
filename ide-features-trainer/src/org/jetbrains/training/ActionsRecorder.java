package org.jetbrains.training;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by karashevich on 18/12/14.
 */
public class ActionsRecorder implements Disposable {


    private Project project;
    private Document document;
    private String target;
    private boolean triggerActivated;
    Queue<String> triggerQueue;

    private boolean disposed = false;
    private Runnable doWhenDone;

    public ActionsRecorder(Project project, Document document, String target) {
        this.project = project;
        this.document = document;
        this.target = target;
        this.triggerActivated = false;
        this.doWhenDone = null;
    }

    @Override
    public void dispose() {
        disposed = true;
    }

    public void startRecording(final Runnable doWhenDone){

        if (disposed) return;
        this.doWhenDone = doWhenDone;

//        documentListener = new DocumentListener() {
//            @Override
//            public void beforeDocumentChange(DocumentEvent event) {
//
//            }
//
//            @Override
//            public void documentChanged(DocumentEvent event) {
//
//                Notification notification = new Notification("IDEA Global Help", "document changed", "document changed", NotificationType.INFORMATION);
//                Notifications.Bus.notify(notification);
//
//                if (isTaskSolved(document, target)) {
//                    dispose();
//                    doWhenDone.run();
//                }
//            }
//        };


//        document.addDocumentListener(documentListener, this);
    }

    public void startRecording(final Runnable doWhenDone, final @Nullable String actionId) {
        if(actionId != null && !actionId.equals("") && ActionManager.getInstance().getAction(actionId) != null) {
            checkAction(actionId);
            startRecording(doWhenDone);
        } else {
            triggerActivated = true;
            startRecording(doWhenDone);
        }

    }
    public void startRecording(final Runnable doWhenDone, final String[] actionIdArray){

        if (disposed) return;
        this.doWhenDone = doWhenDone;

//        triggerMap = new HashMap<String, Boolean>(actionIdArray.length);
        triggerQueue = new LinkedList<String>();
        //set triggerMap
        for (String actionString : actionIdArray) {
            triggerQueue.add(actionString);
        }
        checkAction();

    }



    public boolean isTaskSolved(Document current, String target){
        if (disposed) return false;

        if (target == null){
            if (triggerQueue !=null) {
                return (triggerQueue.size() == 0);
            } else return triggerActivated;
        } else {

            List<String> expected = computeTrimmedLines(target);
            List<String> actual = computeTrimmedLines(current.getText());

            if (triggerQueue !=null) {
                return (expected.equals(actual) && (triggerQueue.size() == 0));
            } else return (expected.equals(actual) && triggerActivated);
        }

    }

    private List<String> computeTrimmedLines(String s) {
        ArrayList<String> ls = new ArrayList<String>();

        for (String it :StringUtil.splitByLines(s) ) {
            String[] splitted = it.split("[ ]+");
            if (splitted != null) {
                for(String element: splitted)
                if (!element.equals("")) {
                    ls.add(element);
                }
            }
        }
        return ls;
    }

    private void checkAction(final String actionTriggerId){

        final ActionManager actionManager = ActionManager.getInstance();
        if(actionManager == null) return;

        final AnActionListener anActionListener = new AnActionListener() {
            @Override
            public void beforeActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
            }

            @Override
            public void afterActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
                final String actionId = ActionManager.getInstance().getId(action);

                if(actionId == null) return;
                if (actionId.toUpperCase().equals(actionTriggerId.toUpperCase())) {
//                    System.out.println("Action trigger has been activated.");
                    if (triggerQueue != null) {
                        triggerQueue.add(actionTriggerId);
                    } else {
                        triggerActivated = true;
                    }
                    actionManager.removeAnActionListener(this);
                    if(isTaskSolved(document, target)) {
                        if(doWhenDone != null)
                            dispose();
                            doWhenDone.run();
                    }
                }
//                System.out.println("ACTION PERFORMED: " + actionId);
            }

            @Override
            public void beforeEditorTyping(char c, DataContext dataContext) {
            }
        };

        actionManager.addAnActionListener(anActionListener);
    }

    private void checkAction() {
        final ActionManager actionManager = ActionManager.getInstance();
        if(actionManager == null) return;

        final AnActionListener anActionListener = new AnActionListener() {
            @Override
            public void beforeActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
            }

            @Override
            public void afterActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
                final String actionId = ActionManager.getInstance().getId(action);

                if(actionId == null) return;
                if (actionId.toUpperCase().equals(triggerQueue.peek().toUpperCase())) {
//                    System.out.println("Action trigger has been activated.");
                    triggerQueue.poll();
                    if (triggerQueue.size() == 0) {
                        actionManager.removeAnActionListener(this);
                        if (isTaskSolved(document, target)) {
                            if (doWhenDone != null)
                                dispose();
                            doWhenDone.run();
                        }
                    }
                }
//                System.out.println("ACTION PERFORMED: " + actionId);
            }

            @Override
            public void beforeEditorTyping(char c, DataContext dataContext) {
            }
        };

        actionManager.addAnActionListener(anActionListener);

    }
}

