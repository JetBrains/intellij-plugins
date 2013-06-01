package com.jetbrains.lang.dart.ide.refactoring.extract;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartControlFlow;
import com.jetbrains.lang.dart.util.DartElementGenerator;
import com.jetbrains.lang.dart.util.DartRefactoringUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * @author: Fedor.Korotkov
 */
public class DartExtractMethodHandler implements RefactoringActionHandler {
  @Override
  public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext) {
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
    final SelectionModel selectionModel = editor.getSelectionModel();
    if (!selectionModel.hasSelection()) selectionModel.selectLineAtCaret();

    final PsiElement[] elements =
      DartRefactoringUtil.findStatementsInRange(file, selectionModel.getSelectionStart(), selectionModel.getSelectionEnd());

    if (elements.length == 0 || (elements.length == 1 && elements[0] instanceof DartExpression)) {
      // todo
      CommonRefactoringUtil.showErrorHint(
        project,
        editor,
        RefactoringBundle.getCannotRefactorMessage(DartBundle.message("dart.refactoring.extract.method.from.expression.error")),
        DartBundle.message("dart.refactoring.extract.method.error"),
        null
      );
      return;
    }

    final DartControlFlow controlFlow = DartControlFlow.analyze(elements);

    selectionModel.isBlockSelectionGuarded();

    if (controlFlow.getReturnValues().size() > 1) {
      CommonRefactoringUtil.showErrorHint(
        project,
        editor,
        RefactoringBundle.getCannotRefactorMessage(DartBundle.message("dart.refactoring.multiple.output.values")),
        DartBundle.message("dart.refactoring.extract.method.error"),
        null
      );
      return;
    }

    final Scope scope = findScope(elements);

    controlFlow.filterParams(new Condition<DartComponentName>() {
      @Override
      public boolean value(DartComponentName name) {
        return !scope.containsDeclaration(name);
      }
    });

    doRefactoringInWriteAction(project, editor, elements, controlFlow, scope);
  }

  private static void doRefactoringInWriteAction(Project project,
                                                 final Editor editor,
                                                 final PsiElement[] elements,
                                                 final DartControlFlow controlFlow,
                                                 final Scope scope) {
    CommandProcessor.getInstance().executeCommand(project, new Runnable() {
      public void run() {
        AccessToken l = WriteAction.start();
        try {
          doRefactoring(editor, elements, controlFlow, scope);
        }
        finally {
          l.finish();
        }
      }
    }, DartBundle.message("dart.extract.method"), null);
  }

  private static void doRefactoring(Editor editor, PsiElement[] elements, DartControlFlow controlFlow, Scope scope) {
    final Project project = elements[0].getProject();
    final PsiFile file = elements[0].getContainingFile();
    final PsiElement anchorToAdd = scope.findAnchor(elements);
    final StringBuilder functionBody = new StringBuilder();

    final Set<String> usedNames = DartRefactoringUtil.collectUsedNames(anchorToAdd);
    String functionName = "extracted";
    while (usedNames.contains(functionName)) {
      functionName += "0";
    }

    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      DartExtractDialog extractDialog = new DartExtractDialog(project, functionName, controlFlow);
      extractDialog.show();
      if (!extractDialog.isOK()) {
        return;
      }
      functionName = extractDialog.getFunctionName();
    }

    functionBody.append(controlFlow.getSignature(functionName));
    functionBody.append("{\n");

    final int startOffset = elements[0].getTextOffset();
    final int endOffset = elements[elements.length - 1].getTextRange().getEndOffset();
    functionBody.append(new TextRange(startOffset, endOffset).substring(elements[0].getContainingFile().getText()));

    if (!controlFlow.getReturnValues().isEmpty()) {
      functionBody.append("\nreturn ");
      final DartComponentName componentName = controlFlow.getReturnValues().iterator().next();
      functionBody.append(componentName.getName());
      functionBody.append(";");
    }
    functionBody.append("\n}");
    final String replaceStatementText = controlFlow.getReplaceStatementText(functionName);
    PsiElement replaceStatement = DartElementGenerator.createStatementFromText(project, replaceStatementText);
    final List<DartComponent> dartComponents = DartElementGenerator.createFunctionsFromText(project, functionBody.toString());
    if (replaceStatement == null || dartComponents.isEmpty()) {
      return;
    }
    PsiElement function = dartComponents.iterator().next();
    function = anchorToAdd.getParent().addBefore(function, anchorToAdd);

    final PsiElement newLineNode =
      PsiParserFacade.SERVICE.getInstance(function.getProject()).createWhiteSpaceFromText("\n");

    replaceStatement = elements[0].getParent().addBefore(replaceStatement, elements[0]);
    replaceStatement.getParent().addBefore(newLineNode, replaceStatement);

    function.getParent().addAfter(newLineNode, function);

    final ASTNode nextChild = replaceStatement.getNode().getTreeNext();
    replaceStatement.getParent().getNode().addLeaf(DartTokenTypes.SEMICOLON, ";", nextChild);

    elements[0].getParent().deleteChildRange(elements[0], elements[elements.length - 1]);

    PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
    CodeStyleManager.getInstance(project).reformatText(
      file,
      function.getTextRange().getStartOffset(),
      function.getTextRange().getEndOffset()
    );

    editor.getCaretModel().moveToOffset(replaceStatement.getTextOffset());
  }

  private static Scope findScope(PsiElement[] elements) {
    final DartClass dartClass = PsiTreeUtil.getParentOfType(elements[0], DartClass.class);
    return new Scope(dartClass == null ? PsiTreeUtil.getTopmostParentOfType(elements[0], DartExecutionScope.class) : dartClass);
  }

  private static class Scope {

    private final PsiElement myElement;

    public Scope(PsiElement element) {
      myElement = element;
    }

    public boolean containsDeclaration(final DartComponentName declarationName) {
      return DartResolver.resolveSimpleReference(getScopeBody(), declarationName.getText()).contains(declarationName);
    }

    private PsiElement getScopeBody() {
      PsiElement result = myElement instanceof DartClass ? DartResolveUtil.getBody((DartClass)myElement) : myElement;
      assert result != null;
      return result;
    }

    public PsiElement findAnchor(PsiElement[] elements) {
      PsiElement scopeBody = getScopeBody();
      PsiElement result = elements[0];
      while (result.getParent() != null && result.getParent() != scopeBody) {
        result = result.getParent();
      }
      return result;
    }
  }
}
