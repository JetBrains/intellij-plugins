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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by karashevich on 18/12/14.
 */
public class ActionsRecorder implements Disposable {


    private Project project;
    private Document document;
    private String target;
    private boolean triggerActivated;
    private HashMap<String, Boolean> triggerMap;

    private boolean disposed = false;
    private Runnable doWhenDone;
    DocumentListener documentListener;

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

        triggerMap = new HashMap<String, Boolean>(actionIdArray.length);
        //set triggerMap
        for (String anActionIdArray : actionIdArray) {
            triggerMap.put(anActionIdArray, false);
        }
        checkAction(actionIdArray);

    }



    public boolean isTaskSolved(Document current, String target){
        if (disposed) return false;

        if (target == null){
            if (triggerMap !=null) {
                boolean result = true;
                for (Boolean aBoolean : triggerMap.values()) {
                    if (!aBoolean) result = false;
                }
                return result;
            } else return triggerActivated;
        } else {
            List<String> expected = computeTrimmedLines(target);
            List<String> actual = computeTrimmedLines(current.getText());

            if (triggerMap !=null) {
                boolean result = true;
                for (Boolean aBoolean : triggerMap.values()) {
                    if (!aBoolean) result = false;
                }
                return (expected.equals(actual) && result);
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
                    if (triggerMap != null) {
                        triggerMap.put(actionTriggerId, true);
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

    private void checkAction(String[] actionIdArray) {
        for (String actionId : actionIdArray) {
            checkAction(actionId);
        }
    }
}

