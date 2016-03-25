package training.learn;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;
import org.jetbrains.annotations.Nullable;
import training.check.Check;

import java.util.*;

/**
 * Created by karashevich on 18/12/14.
 */
public class ActionsRecorder implements Disposable {


    private Project project;
    private Document document;
    private Editor editor;
    private String target;
    private boolean triggerActivated;
    private Queue<String> triggerQueue;

    private DocumentListener myDocumentListener;
    private AnActionListener myAnActionListener;

    private boolean disposed = false;
    private Runnable doWhenDone;
    @Nullable
    private Check check = null;

    public ActionsRecorder(Project project, Document document, String target, Editor editor) {
        this.project = project;
        this.document = document;
        this.target = target;
        this.triggerActivated = false;
        this.doWhenDone = null;
        this.editor = editor;

        //TODO: add disposer to ActionRecorder
//        Disposer.register(editor, this);
    }

    @Override
    public void dispose() {
        removeListeners(document, ActionManager.getInstance());
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

    public void startRecording(final Runnable doWhenDone, final @Nullable String actionId, @Nullable Check check) throws Exception {
        final String[] stringArray = {actionId};
        startRecording(doWhenDone, stringArray, check);

    }
    public void startRecording(final Runnable doWhenDone, final String[] actionIdArray, @Nullable Check check) throws Exception {
        if (check != null) this.check = check;
        if (disposed) return;
        this.doWhenDone = doWhenDone;

//        triggerMap = new HashMap<String, Boolean>(actionIdArray.length);
        triggerQueue = new LinkedList<>();
        //set triggerMap
        if (actionIdArray != null) {
            Collections.addAll(triggerQueue, actionIdArray);
        }
        addActionAndDocumentListeners();
    }



    private boolean isTaskSolved(Document current, String target){
        if (disposed) return false;

        if (target == null){
            if (triggerQueue !=null) {
                return ((triggerQueue.size() == 1 || triggerQueue.size() == 0 ) && (check == null || check.check()));
            } else return (triggerActivated && (check == null || check.check()));
        } else {

            List<String> expected = computeTrimmedLines(target);
            List<String> actual = computeTrimmedLines(current.getText());

            if (triggerQueue !=null) {
                return ((expected.equals(actual) && (triggerQueue.size() == 0)) && (check == null || check.check()));
            } else return ((expected.equals(actual) && triggerActivated ) && (check == null || check.check()));
        }

    }

    private List<String> computeTrimmedLines(String s) {
        ArrayList<String> ls = new ArrayList<>();

        for (String it :StringUtil.splitByLines(s) ) {
            String[] splitted = it.split("[ ]+");
            for(String element: splitted)
            if (!element.equals("")) {
                ls.add(element);
            }
        }
        return ls;
    }


    /**
     * method adds action and document listeners to monitor user activity and check task
     */
    private void addActionAndDocumentListeners() throws Exception {
        final ActionManager actionManager = ActionManager.getInstance();
        if(actionManager == null) throw new Exception("Unable to get instance for ActionManager");

        myAnActionListener = new AnActionListener() {

            private boolean editorFlag;

            @Override
            public void beforeActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {
                if (event.getProject() == null || FileEditorManager.getInstance(event.getProject()).getSelectedTextEditor() != editor )
                    editorFlag = false;
                else
                    editorFlag = true;
            }

            @Override
            public void afterActionPerformed(AnAction action, DataContext dataContext, AnActionEvent event) {

                //if action called not from project or current editor is different from editor
                 if (!editorFlag) return;

                if(triggerQueue.size() == 0) {
                    if (isTaskSolved(document, target)) {
                        actionManager.removeAnActionListener(this);
                        if (doWhenDone != null) {
                            dispose();
                            doWhenDone.run();
                        }
                    }
                }
                if (checkAction(action, triggerQueue.peek())) {
                    if (triggerQueue.size() > 1) {
                        triggerQueue.poll();
                    } else if (triggerQueue.size() == 1) {
                        if (isTaskSolved(document, target)) {
                            actionManager.removeAnActionListener(this);
                            if (doWhenDone != null) {
                                dispose();
                                doWhenDone.run();
                            }
                        } else {
                            triggerQueue.poll();
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
                    ApplicationManager.getApplication().invokeLater(() -> {

                        if(!disposed && !project.isDisposed()) {
                            PsiDocumentManager.getInstance(project).commitAndRunReadAction(new Runnable() {
                                @Override
                                public void run() {
                                    if (triggerQueue.size() == 0) {
                                        if (isTaskSolved(document, target)) {
                                            removeListeners(document, actionManager);
                                            if (doWhenDone != null)
                                                dispose();
                                            assert doWhenDone != null;
                                            doWhenDone.run();
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
            }
        };

        document.addDocumentListener(myDocumentListener);
        actionManager.addAnActionListener(myAnActionListener);
    }


    /**
     *
     * @param action - caught action by AnActionListener (see {@link #addActionAndDocumentListeners()} addActionAndDocumentListeners()} method.)
     * @param actionString - could be an actionId or action class name
     * @return if action equals to actionId or have the same action class name
     */
    private boolean checkAction(AnAction action, String actionString){
        String actionId = ActionManager.getInstance().getId(action);
        final String actionClassName = action.getClass().getName();
        return actionId != null ? equalStr(actionId, actionString) || equalStr(actionClassName, actionString) : equalStr(actionClassName, actionString);
    }

    private void removeListeners(Document document, ActionManager actionManager){
        if (myAnActionListener != null) actionManager.removeAnActionListener(myAnActionListener);
        if (myDocumentListener != null) document.removeDocumentListener(myDocumentListener);
        myAnActionListener = null;
        myDocumentListener = null;
    }

    private boolean equalStr(@Nullable String str1, @Nullable String str2) {
        return !((str1 == null) || (str2 == null)) && (str1.toUpperCase().equals(str2.toUpperCase()));
    }
}

