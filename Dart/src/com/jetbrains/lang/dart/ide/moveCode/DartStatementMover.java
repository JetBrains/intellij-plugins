package com.jetbrains.lang.dart.ide.moveCode;

import com.intellij.codeInsight.editorActions.moveUpDown.LineMover;
import com.intellij.codeInsight.editorActions.moveUpDown.LineRange;
import com.intellij.codeInsight.editorActions.moveUpDown.StatementUpDownMover;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartPsiImplUtil;
import com.jetbrains.lang.dart.util.DartRefactoringUtil;
import com.jetbrains.lang.dart.util.UsefulPsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.jetbrains.lang.dart.util.DartRefactoringUtil.isComma;
import static com.jetbrains.lang.dart.util.DartRefactoringUtil.isRightParen;

/**
 * Move executable statements within a method or function. Statements cannot be moved outside a component.
 * TODO: What about moving statements from a local function to the surrounding method?
 */
public class DartStatementMover extends LineMover {
  private SmartPsiElementPointer statementToSurroundWithCodeBlock;

  @Override
  public void afterMove(@NotNull final Editor editor, @NotNull final PsiFile file, @NotNull final MoveInfo info, final boolean down) {
    super.afterMove(editor, file, info, down);
    statementToSurroundWithCodeBlock = null;
  }

  @Override
  public void beforeMove(@NotNull final Editor editor, @NotNull final MoveInfo info, final boolean down) {
    super.beforeMove(editor, info, down);
    if (statementToSurroundWithCodeBlock != null) {
      surroundWithCodeBlock(info, down);
    }
  }

  private void surroundWithCodeBlock(@NotNull final MoveInfo info, final boolean down) {
    // TODO Implement surroundWithCodeBlock()
  }

  @Override
  public boolean checkAvailable(@NotNull Editor editor, @NotNull PsiFile file, @NotNull MoveInfo info, boolean down) {
    if (!(file instanceof DartFile)) return false;
    if (!super.checkAvailable(editor, file, info, down)) return false;
    LineRange range = expandLineRangeToCoverPsiElements(info.toMove, editor, file);
    if (range == null) return false;
    info.toMove = range;
    final int startOffset = editor.logicalPositionToOffset(new LogicalPosition(range.startLine, 0));
    final int endOffset = editor.logicalPositionToOffset(new LogicalPosition(range.endLine, 0));
    PsiElement[] statements = DartRefactoringUtil.findListExpressionInRange(file, startOffset, endOffset);
    if (statements.length == 1) {
      info.toMove2 = null; // Disallow component mover
      return true; // Require trailing comma
    }
    if (statements.length == 0) {
      statements = DartRefactoringUtil.findStatementsInRange(file, startOffset, endOffset);
    }
    if (statements.length == 0) return false;
    range.firstElement = statements[0];
    range.lastElement = statements[statements.length - 1];
    info.indentTarget = true;
    if (!checkMovingInsideOutside(file, editor, info, down)) {
      info.toMove2 = null;
    }
    return true;
  }

  private static LineRange expandLineRangeToCoverPsiElements(final LineRange range, Editor editor, final PsiFile file) {
    Pair<PsiElement, PsiElement> psiRange = getElementRange(editor, file, range);
    if (psiRange == null) return null;
    if (psiRange.getFirst() instanceof DartStatements || psiRange.getFirst() instanceof DartExpressionList) {
      PsiElement first = psiRange.getFirst();
      PsiElement last = psiRange.getSecond();
      if (last != null) {
        PsiElement statement = first.getFirstChild();
        if (statement != null) {
          psiRange = Pair.create(statement, last);
        }
      }
    }
    if (psiRange.getSecond() instanceof DartStatements) {
      PsiElement first = psiRange.getFirst();
      PsiElement last = psiRange.getSecond();
      if (PsiTreeUtil.isAncestor(last, first, false)) {
        PsiElement statement = last.getLastChild();
        if (statement != null) {
          psiRange = Pair.create(first, statement);
        }
      }
    }
    if (isComma(psiRange.getSecond())) {
      PsiElement first = psiRange.getFirst();
      PsiElement last = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(psiRange.getSecond(), true);
      if (PsiTreeUtil.isAncestor(last, first, false)) {
        PsiElement statement = last.getLastChild();
        if (statement != null) {
          psiRange = Pair.create(first, statement);
        }
      }
    }
    final PsiElement parent = PsiTreeUtil.findCommonParent(psiRange.getFirst(), psiRange.getSecond());
    Pair<PsiElement, PsiElement> elementRange = getElementRange(parent, psiRange.getFirst(), psiRange.getSecond());
    if (elementRange == null) return null;
    int endOffset = elementRange.getSecond().getTextRange().getEndOffset();
    Document document = editor.getDocument();
    if (endOffset > document.getTextLength()) {
      return null;
    }
    int endLine;
    if (endOffset == document.getTextLength()) {
      endLine = document.getLineCount();
    }
    else {
      endLine = editor.offsetToLogicalPosition(endOffset).line + 1;
      endLine = Math.min(endLine, document.getLineCount());
    }
    int startLine = Math.min(range.startLine, editor.offsetToLogicalPosition(elementRange.getFirst().getTextOffset()).line);
    endLine = Math.max(endLine, range.endLine);
    return new LineRange(startLine, endLine);
  }

  private boolean checkMovingInsideOutside(PsiFile file, final Editor editor, @NotNull final MoveInfo info, final boolean down) {
    final int offset = editor.getCaretModel().getOffset();
    PsiElement elementAtOffset = file.getViewProvider().findElementAt(offset, DartLanguage.INSTANCE);
    if (elementAtOffset == null) return false;

    PsiElement guard = elementAtOffset;
    boolean isExpr = isMovingExpr(info.toMove);
    if (isExpr) {
      guard = PsiTreeUtil.getParentOfType(guard, DartMethodDeclaration.class, DartListLiteralExpression.class,
                                          DartFunctionDeclarationWithBodyOrNative.class, DartClass.class, PsiComment.class);
    }
    else {
      guard = PsiTreeUtil.getParentOfType(guard, DartMethodDeclaration.class,
                                          DartFunctionDeclarationWithBodyOrNative.class, DartClass.class, PsiComment.class);
    }

    PsiElement brace = soloRightBraceBeingMoved(file, editor);
    if (brace != null) {
      int line = editor.getDocument().getLineNumber(offset);
      final LineRange toMove = new LineRange(line, line + 1);
      toMove.firstElement = toMove.lastElement = brace;
      info.toMove = toMove;
    }

    // Cannot move in/outside method/class/function/comment.
    if (!calcInsertOffset(file, editor, info.toMove, info, down)) return false;
    int insertOffset = down
                       ? getLineStartSafeOffset(editor.getDocument(), info.toMove2.endLine)
                       : editor.getDocument().getLineStartOffset(info.toMove2.startLine);
    PsiElement elementAtInsertOffset = file.getViewProvider().findElementAt(insertOffset, DartLanguage.INSTANCE);
    PsiElement newGuard;
    if (isExpr) {
      newGuard =
        PsiTreeUtil.getParentOfType(elementAtInsertOffset, DartMethodDeclaration.class, DartListLiteralExpression.class,
                                    DartFunctionDeclarationWithBodyOrNative.class, DartClass.class, PsiComment.class);
    }
    else {
      newGuard = PsiTreeUtil.getParentOfType(elementAtInsertOffset, DartMethodDeclaration.class,
                                             DartFunctionDeclarationWithBodyOrNative.class, DartClass.class, PsiComment.class);
    }

    if (brace != null && PsiTreeUtil.getParentOfType(brace, IDartBlock.class, false) !=
                         PsiTreeUtil.getParentOfType(elementAtInsertOffset, IDartBlock.class, false)) {
      info.indentSource = true;
    }
    if (newGuard == guard && isInside(insertOffset, newGuard) == isInside(offset, guard)) return true;

    return false;
  }

  private static PsiElement soloRightBraceBeingMoved(final PsiFile file, final Editor editor) {
    // Return the right brace on the line with the cursor, or null if the line is not a single brace.
    LineRange range = getLineRangeFromSelection(editor);
    if (range.endLine - range.startLine != 1) {
      return null;
    }
    Document document = editor.getDocument();
    int offset = editor.getCaretModel().getOffset();
    int line = document.getLineNumber(offset);
    int lineStartOffset = document.getLineStartOffset(line);
    String lineText = document.getText().substring(lineStartOffset, document.getLineEndOffset(line));
    if (!lineText.trim().equals("}")) {
      return null;
    }
    return file.findElementAt(lineStartOffset + lineText.indexOf('}'));
  }

  private boolean calcInsertOffset(@NotNull PsiFile file,
                                   @NotNull Editor editor,
                                   @NotNull LineRange range,
                                   @NotNull final MoveInfo info,
                                   final boolean down) {
    int destLine = getDestLineForAnon(editor, range, down);

    int startLine = down ? range.endLine : range.startLine - 1;
    if (destLine < 0 || startLine < 0) return false;
    boolean firstTime = true;
    boolean isExpr = isMovingExpr(info.toMove);
    PsiElement elementStart = null;
    if (isExpr /*&& down*/) {
      int offset = editor.logicalPositionToOffset(new LogicalPosition(startLine, 0));
      elementStart = firstNonWhiteMovableElement(offset, file, true);
      if (elementStart instanceof DartExpression) {
        TextRange elementTextRange = elementStart.getTextRange();
        LogicalPosition pos = editor.offsetToLogicalPosition(elementTextRange.getEndOffset());
        int endOffset = editor.logicalPositionToOffset(new LogicalPosition(pos.line + 1, 0));
        PsiElement elementEnd = firstNonWhiteMovableElement(endOffset, file, false);
        PsiElement elementTail = elementEnd;
        if (isComma(elementEnd)) {
          elementTail = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(elementEnd, true);
          if (elementTail instanceof DartExpressionList) {
            elementTail = elementTail.getLastChild();
            if (elementStart == elementTail) {
              info.toMove2 = new LineRange(startLine, pos.line + 1);
              return true;
            }
          }
        }
      }
      else if (isRightParen(elementStart)) {
        PsiElement start = elementStart.getParent().getParent();
        TextRange elementTextRange = start.getTextRange();
        LogicalPosition pos = editor.offsetToLogicalPosition(elementTextRange.getStartOffset());
        int startOffset = editor.logicalPositionToOffset(new LogicalPosition(pos.line, 0));
        PsiElement startElement = firstNonWhiteMovableElement(startOffset, file, true);
        if (startElement == start) {
          info.toMove2 = new LineRange(pos.line, down ? startLine : startLine + 1);
          return true;
        }
      }
    }
    while (true) {
      int offset = editor.logicalPositionToOffset(new LogicalPosition(destLine, 0));
      PsiElement element = firstNonWhiteMovableElement(offset, file, !isExpr || !down);
      if (firstTime) {
        if (element != null && element.getNode().getElementType() == (down ? DartTokenTypes.RBRACE : DartTokenTypes.LBRACE)) {
          PsiElement elementParent = element.getParent();
          if (elementParent != null && (isStatement(elementParent) || elementParent instanceof DartBlock)) {
            return true;
          }
        }
      }

      if (element instanceof DartStatements) element = element.getFirstChild();
      while (element != null && !(element instanceof PsiFile)) {
        TextRange elementTextRange = element.getTextRange();
        if (elementTextRange.isEmpty() || !elementTextRange.grown(-1).shiftRight(1).contains(offset)) {
          PsiElement elementToSurround = null;
          boolean found = false;
          if (isExpr) {
            if (firstTime && element instanceof DartExpression) {
              found = true;
            }
            else if (isComma(element) && UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(element, true) == elementStart) {
              found = true;
            }
            else if (element instanceof DartArgumentList) {
              element = element.getParent();
              if (element.getParent() == elementStart) {
                element = element.getParent().getNextSibling();
                boolean hasComma = false;
                while (element != null) {
                  if (UsefulPsiTreeUtil.isWhitespaceOrComment(element)) {
                    if (element.getText().contains("\n")) {
                      destLine += 1;
                      break;
                    }
                  }
                  else if (isComma(element)) {
                    hasComma = true;
                  }
                  else {
                    break;
                  }
                  element = element.getNextSibling();
                }
                if (!hasComma) {
                  return false; // Disallow move if following expr has no trailing comma
                }
                found = true;
              }
            }
          }
          else if ((isStatement(element) || element instanceof PsiComment)
                   && statementCanBePlacedAlong(element)) {
            found = true;
            if (!(element.getParent() instanceof IDartBlock)) {
              elementToSurround = element;
            }
          }
          else if ((element.getNode().getElementType() == DartTokenTypes.RBRACE &&
                    element.getParent() instanceof IDartBlock &&
                    (!isStatement(element.getParent().getParent()) || statementCanBePlacedAlong(element.getParent().getParent())))
                   || (!down && element instanceof DartStatements)) {
            // Before/after code block closing/opening brace.
            found = true;
          }
          if (found) {
            if (elementToSurround != null) {
              final SmartPointerManager manager = SmartPointerManager.getInstance(elementToSurround.getProject());
              statementToSurroundWithCodeBlock = manager.createSmartPsiElementPointer(elementToSurround);
            }
            info.toMove = range;
            int endLine = destLine;
            if (startLine > endLine) {
              int tmp = endLine;
              endLine = startLine;
              startLine = tmp;
            }

            info.toMove2 = new LineRange(startLine, down ? endLine : endLine + 1);
            return true;
          }
        }
        element = element.getParent();
      }
      firstTime = false;
      destLine += down ? 1 : -1;
      if (destLine <= 0 || destLine >= editor.getDocument().getLineCount()) {
        return false;
      }
    }
  }

  private static int getDestLineForAnon(Editor editor, LineRange range, boolean down) {
    int destLine = down ? range.endLine + 1 : range.startLine - 1;
    if (!isStatement(range.firstElement)) {
      return destLine;
    }
    PsiElement sibling =
      StatementUpDownMover.firstNonWhiteElement(down ? range.lastElement.getNextSibling() : range.firstElement.getPrevSibling(), down);
    final DartFunctionExpression fn = findChildOfType(sibling, DartFunctionExpression.class, DartPsiCompositeElement.class);
    if (fn != null && sibling != null && PsiTreeUtil.getParentOfType(fn, DartPsiCompositeElement.class) == sibling) {
      destLine =
        editor.getDocument().getLineNumber(down ? sibling.getTextRange().getEndOffset() + 1 : sibling.getTextRange().getStartOffset());
    }
    return destLine;
  }

  @Nullable
  private static <T extends PsiElement> T findChildOfType(@Nullable final PsiElement element,
                                                          @NotNull final Class<T> aClass,
                                                          @Nullable final Class<? extends PsiElement> stopAt) {
    final PsiElementProcessor.FindElement<PsiElement> processor;
    processor = new PsiElementProcessor.FindElement<PsiElement>() {
      @Override
      public boolean execute(@NotNull PsiElement each) {
        if (element == each) return true; // strict
        if (aClass.isInstance(each)) {
          return setFound(each);
        }
        return stopAt == null || !stopAt.isInstance(each);
      }
    };

    PsiTreeUtil.processElements(element, processor);
    //noinspection unchecked
    return (T)processor.getFoundElement();
  }

  private static boolean isInside(final int offset, final PsiElement guard) {
    if (guard == null) return false;
    TextRange inside = guard.getTextRange();
    if (guard instanceof DartMethodDeclaration) {
      DartFunctionBody body = ((DartMethodDeclaration)guard).getFunctionBody();
      if (body != null) {
        inside = body.getTextRange();
      }
    }
    else if (guard instanceof DartFunctionDeclarationWithBodyOrNative) {
      DartFunctionBody body = ((DartFunctionDeclarationWithBodyOrNative)guard).getFunctionBody();
      if (body != null) {
        inside = body.getTextRange();
      }
    }
    else if (guard instanceof DartClassDefinition) {
      DartClassBody body = ((DartClassDefinition)guard).getClassBody();
      PsiElement lBrace = PsiTreeUtil.getChildOfType(body, LeafPsiElement.class);
      if (lBrace != null && lBrace.getText().equals("{")) {
        PsiElement rBrace = PsiTreeUtil.lastChild(body);
        rBrace = PsiTreeUtil.skipSiblingsBackward(rBrace, PsiWhiteSpace.class, PsiComment.class);
        if (rBrace != null && rBrace.getText().equals("}")) {
          inside = new TextRange(lBrace.getTextOffset(), rBrace.getTextOffset());
        }
      }
    }
    return inside != null && inside.contains(offset);
  }

  private static boolean statementCanBePlacedAlong(final PsiElement element) {
    if (element instanceof IDartBlock) {
      return false;
    }
    PsiElement parent = element.getParent();
    if (parent instanceof DartStatements) {
      parent = parent.getParent();
      if (parent instanceof IDartBlock) {
        if (!isStatement(parent.getParent())) {
          return true;
        }
        parent = parent.getParent();
      }
      else {
        return false;
      }
    }
    if (parent instanceof DartIfStatement) {
      PsiElement thenBranch = DartPsiImplUtil.getThenBranch((DartIfStatement)parent);
      PsiElement elseBranch = DartPsiImplUtil.getElseBranch((DartIfStatement)parent);
      if (isSameStatement(element, thenBranch) || isSameStatement(element, elseBranch)) {
        return true;
      }
    }
    if (parent instanceof DartWhileStatement) {
      PsiElement body = DartPsiImplUtil.getWhileBody((DartWhileStatement)parent);
      if (isSameStatement(element, body)) {
        return true;
      }
    }
    if (parent instanceof DartDoWhileStatement) {
      PsiElement body = DartPsiImplUtil.getDoBody((DartDoWhileStatement)parent);
      if (isSameStatement(element, body)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isSameStatement(PsiElement element, PsiElement statementOrBlock) {
    if (element == statementOrBlock) return true;
    if (PsiTreeUtil.findCommonParent(statementOrBlock, element) == statementOrBlock) return true;
    return expressionStatementTeminator(statementOrBlock) == element;
  }

  private static boolean isStatement(PsiElement element) {
    boolean[] result = new boolean[1];
    result[0] = false;

    element.accept(new DartVisitor() {
      public void visitAssertStatement(@NotNull DartAssertStatement o) {
        result[0] = true;
      }

      public void visitBreakStatement(@NotNull DartBreakStatement o) {
        result[0] = true;
      }

      public void visitContinueStatement(@NotNull DartContinueStatement o) {
        result[0] = true;
      }

      public void visitDoWhileStatement(@NotNull DartDoWhileStatement o) {
        result[0] = true;
      }

      public void visitForStatement(@NotNull DartForStatement o) {
        result[0] = true;
      }

      public void visitIfStatement(@NotNull DartIfStatement o) {
        result[0] = true;
      }

      public void visitRethrowStatement(@NotNull DartRethrowStatement o) {
        result[0] = true;
      }

      public void visitReturnStatement(@NotNull DartReturnStatement o) {
        result[0] = true;
      }

      public void visitSwitchStatement(@NotNull DartSwitchStatement o) {
        result[0] = true;
      }

      public void visitTryStatement(@NotNull DartTryStatement o) {
        result[0] = true;
      }

      public void visitWhileStatement(@NotNull DartWhileStatement o) {
        result[0] = true;
      }

      public void visitYieldEachStatement(@NotNull DartYieldEachStatement o) {
        result[0] = true;
      }

      public void visitYieldStatement(@NotNull DartYieldStatement o) {
        result[0] = true;
      }

      public void visitVarDeclarationList(@NotNull DartVarDeclarationList o) {
        result[0] = expressionStatementTeminator(o) != null;
      }

      public void visitExpression(@NotNull DartExpression o) {
        result[0] = expressionStatementTeminator(o) != null;
      }
    });
    return result[0];
  }

  @Nullable
  private static PsiElement expressionStatementTeminator(PsiElement element) {
    if (element instanceof DartExpression || element instanceof DartVarDeclarationList) {
      PsiElement token = PsiTreeUtil.skipSiblingsForward(element, PsiComment.class, PsiWhiteSpace.class);
      if (token != null && token.getNode().getElementType() == DartTokenTypes.SEMICOLON) {
        return token;
      }
    }
    return null;
  }

  @Nullable
  private static PsiElement firstNonWhiteMovableElement(int offset, PsiFile file, boolean lookRight) {
    PsiElement element = firstNonWhiteElement(offset, file, lookRight);
    if (element == null) return null;
    if (element instanceof DartExpressionList && lookRight) {
      element = element.getFirstChild();
    }
    return element;
  }

  private static boolean isMovingExpr(@NotNull LineRange range) {
    return range.firstElement instanceof DartExpression && isComma(range.lastElement);
  }
}
