package training.commands;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.command.undo.BasicUndoableAction;
import com.intellij.openapi.command.undo.DocumentReference;
import com.intellij.openapi.command.undo.DocumentReferenceManager;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.EditorGutter;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiDocumentManager;
import org.jdom.Element;

/**
 * Created by karashevich on 30/01/15.
 */
public class CopyTextCommand extends Command {

    public CopyTextCommand(){
        super(CommandType.COPYTEXT);
    }

    @Override
    public void execute(final ExecutionList executionList) throws InterruptedException {

        Element element = executionList.getElements().poll();
//        updateDescription(element, infoPanel, editor);

        final String finalText = (element.getContent().isEmpty() ? "" : element.getContent().get(0).getValue());
        if(WriteCommandAction.runWriteCommandAction(executionList.getProject(), new Computable<Boolean>() {
            @Override
            public Boolean compute() {
                final DocumentReference documentReference = DocumentReferenceManager.getInstance().create(executionList.getEditor().getDocument());
                UndoManager.getInstance(executionList.getProject()).nonundoableActionPerformed(documentReference, false);
                executionList.getEditor().getDocument().insertString(0, finalText);
                PsiDocumentManager.getInstance(executionList.getProject()).commitDocument(executionList.getEditor().getDocument());

                updateGutter(executionList);

                UndoManager.getInstance(executionList.getProject()).undoableActionPerformed(new BasicUndoableAction() {
                    @Override
                    public void undo() {
                    }

                    @Override
                    public void redo() {
                    }
                });

                return true;
            }
        })) startNextCommand(executionList);
    }

    private void updateGutter(ExecutionList executionList) {
        final EditorGutter editorGutter = executionList.getEditor().getGutter();
        EditorGutterComponentEx editorGutterComponentEx = (EditorGutterComponentEx) editorGutter;
        editorGutterComponentEx.revalidateMarkup();
    }
}
