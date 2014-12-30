package org.jetbrains.training;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karashevich on 18/12/14.
 */
public class ActionsRecorder implements Disposable {


    private Project project;
    private Document document;
    private String target;

    private boolean disposed = false;

    public ActionsRecorder(Project project, Document document, String target) {
        this.project = project;
        this.document = document;
        this.target = target;
    }

    @Override
    public void dispose() {
        disposed = true;
    }

    public void startRecording(){
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void beforeDocumentChange(DocumentEvent event) {

            }

            @Override
            public void documentChanged(DocumentEvent event) {
                if (isTaskSolved(document, target)) {
                    Messages.showMessageDialog(project, "Congratulations, this task has been solved!", "Information", Messages.getInformationIcon());
                }
            }
        };

        document.addDocumentListener(documentListener, this);
    }

    public boolean isTaskSolved(Document current, String target){
        if (disposed) return false;

        List<String> expected = computeTrimmedLines(target);
        List<String> actual = computeTrimmedLines(current.getText().toString());

        return (expected.equals(actual));
    }

    private List<String> computeTrimmedLines(String s) {
        ArrayList<String> ls = new ArrayList<String>();

        for (String it :StringUtil.splitByLines(s) ) {
            it.trim();
            ls.add(it);
        }

        return ls;

    }



}
