package org.jetbrains.training;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.impl.event.DocumentEventImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.PomManager;
import com.intellij.pom.PomModel;
import com.intellij.pom.PomModelAspect;
import com.intellij.pom.event.PomModelListener;
import com.intellij.pom.tree.TreeAspect;
import com.intellij.psi.*;
import com.intellij.psi.impl.DocumentCommitProcessor;
import com.intellij.psi.impl.migration.PsiMigrationManager;
import com.intellij.psi.impl.source.tree.ChangeUtil;
import com.intellij.psi.util.PsiEditorUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.training.check.Check;

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

    DocumentListener myDocumentListener;
    AnActionListener myAnActionListener;

    private boolean disposed = false;
    private Runnable doWhenDone;
    @Nullable
    Check check = null;

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

    public void startRecording(final Runnable doWhenDone, final @Nullable String actionId, @Nullable Check check) {
        final String[] stringArray = {actionId};
        startRecording(doWhenDone, stringArray, check);

    }
    public void startRecording(final Runnable doWhenDone, final String[] actionIdArray, @Nullable Check check){
        if (check != null) this.check = check;
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
                return (triggerQueue.size() == 0 && (check == null ? true : check.check()));
            } else return (triggerActivated && (check == null ? true : check.check()));
        } else {

            List<String> expected = computeTrimmedLines(target);
            List<String> actual = computeTrimmedLines(current.getText());

            if (triggerQueue !=null) {
                return ((expected.equals(actual) && (triggerQueue.size() == 0)) && (check == null ? true : check.check()));
            } else return ((expected.equals(actual) && triggerActivated ) && (check == null ? true : check.check()));
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

    private void checkAction() {
        final ActionManager actionManager = ActionManager.getInstance();
        if(actionManager == null) return;



        myAnActionListener = new AnActionListener() {
            @Override
            public void beforeActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
            }

            @Override
            public void afterActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
                PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(document);
                final String actionId = ActionManager.getInstance().getId(action);

                if(actionId == null) return;
                if(triggerQueue.size() == 0) return;
                if (actionId.toUpperCase().equals(triggerQueue.peek().toUpperCase())) {
//                    System.out.println("Action trigger has been activated.");
                    triggerQueue.poll();
                    if (triggerQueue.size() == 0) {
                        if (isTaskSolved(document, target)) {
                            actionManager.removeAnActionListener(this);
                            if (doWhenDone != null)
                                dispose();
                            doWhenDone.run();
                        }
                    }
                }
            }

            @Override
            public void beforeEditorTyping(char c, DataContext dataContext) {
            }
        };


        myDocumentListener = new DocumentListener() {
            @Override
            public void beforeDocumentChange(DocumentEvent event) {

            }

            @Override
            public void documentChanged(final DocumentEvent event) {
                if (PsiDocumentManager.getInstance(project).isUncommited(document)) {
                    PsiDocumentManager.getInstance(project).commitAndRunReadAction(new Runnable() {
                        @Override
                        public void run() {
                            if (triggerQueue.size() == 0) {
                                if (isTaskSolved(document, target)) {
                                    removeListeners(document, actionManager);
                                    if (doWhenDone != null)
                                        dispose();
                                    doWhenDone.run();
                                }
                            }
                        }
                    });
                }
            }
        };

        document.addDocumentListener(myDocumentListener);
        actionManager.addAnActionListener(myAnActionListener);

    }

    private void removeListeners(Document document, ActionManager actionManager){
        document.removeDocumentListener(myDocumentListener);
        actionManager.removeAnActionListener(myAnActionListener);
    }
}

