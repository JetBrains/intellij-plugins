/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.config;

import com.intellij.codeInsight.editorActions.moveUpDown.LineMover;
import com.intellij.codeInsight.editorActions.moveUpDown.LineRange;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.intellij.terraform.hcl.HCLElementTypes;
import org.intellij.terraform.hcl.HCLLanguage;
import org.intellij.terraform.hcl.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Set;

/**
 * Based on com.intellij.codeInsight.editorActions.moveUpDown.StatementMover
 */
public class HCLStatementMover extends LineMover {
  private static final Logger LOG = Logger.getInstance(HCLStatementMover.class);

  @Override
  public boolean checkAvailable(@NotNull final Editor editor, @NotNull final PsiFile file, @NotNull final MoveInfo info, final boolean down) {
    final boolean available = super.checkAvailable(editor, file, info, down);
    if (!available) return false;
    LineRange range = info.toMove;

    range = expandLineRangeToCoverPsiElements(range, editor, file);
    if (range == null) return false;
    info.toMove = range;
    final int startOffset = editor.logicalPositionToOffset(new LogicalPosition(range.startLine, 0));
    final int endOffset = editor.logicalPositionToOffset(new LogicalPosition(range.endLine, 0));
    final PsiElement[] statements = findStatementsInRange(file, startOffset, endOffset);
    if (statements.length == 0) return false;
    range.firstElement = statements[0];
    range.lastElement = statements[statements.length - 1];

    return checkMovingInsideOutside(file, editor, info, down) || info.prohibitMove();
  }

  private boolean calcInsertOffset(@NotNull PsiFile file, @NotNull Editor editor, @NotNull LineRange range, @NotNull final MoveInfo info, final boolean down) {
    int destLine = down ? range.endLine + 1 : range.startLine - 1;
    int startLine = down ? range.endLine : range.startLine - 1;

    if (destLine < 0 || startLine < 0) return false;
    final PsiElement movingParent = range.firstElement.getParent();


    while (true) {
      final int offset = editor.logicalPositionToOffset(new LogicalPosition(destLine, 0));
      PsiElement element;
      {
        ASTNode node = file.getNode().findLeafElementAt(offset);
        if (node != null) {
          PsiElement psi = node.getPsi();
          // Last whitespace in file
          if (down && psi instanceof PsiWhiteSpace && psi.getParent() instanceof PsiFile && psi.getNextSibling() == null) {
            //noinspection ConstantConditions
            return calcInsertOffsetFound(info, down, destLine, startLine);
          }
          element = firstNonWhiteElement(psi, true);
        } else {
          int lc = editor.getDocument().getLineCount();
          // Workaround for moving block to latest non-empty line
          if (down && (destLine < lc || (destLine == lc && startLine == lc - 1 && file.getNode().findLeafElementAt(editor.logicalPositionToOffset(new LogicalPosition(startLine, 0))) != null))) {
            //noinspection ConstantConditions
            return calcInsertOffsetFound(info, down, destLine, startLine);
          }
          element = null;
        }
      }

      while (element != null && !(element instanceof PsiFile)) {
        TextRange elementTextRange = element.getTextRange();
        if (elementTextRange.isEmpty() || !elementTextRange.grown(-1).shiftRight(1).contains(offset)) {
          boolean found = false;
          ASTNode node = element.getNode();
          if ((element instanceof HCLElement || element instanceof PsiComment) && statementCanBePlacedAlong(element, movingParent)) {
            if (movingParent instanceof HCLArray && !movingParent.equals(element.getParent())) {
              return info.prohibitMove();
            }
            found = true;
          } else if (element instanceof LeafPsiElement && node != null) {
            IElementType elementType = node.getElementType();
            if (elementType == HCLElementTypes.R_CURLY && element.getParent() instanceof HCLObject) {
              // before code block closing brace
              found = true;
            } else if (elementType == HCLElementTypes.R_BRACKET && element.getParent() instanceof HCLArray && element.getParent().equals(movingParent)) {
              // before array closing bracket
              found = true;
            }
          }
          if (found) {
            return calcInsertOffsetFound(info, down, destLine, startLine);
          }
        }
        element = element.getParent();
      }
      destLine += down ? 1 : -1;
      if (destLine < 0 || destLine >= editor.getDocument().getLineCount()) {
        return false;
      }
    }
  }

  private boolean calcInsertOffsetFound(@NotNull MoveInfo info, boolean down, int endLine, int startLine) {
    if (startLine > endLine) {
      int tmp = endLine;
      endLine = startLine;
      startLine = tmp;
    }

    info.toMove2 = down ? new LineRange(startLine, endLine) : new LineRange(startLine, endLine + 1);
    return true;
  }

  @SuppressWarnings("RedundantIfStatement")
  private static boolean statementCanBePlacedAlong(@NotNull final PsiElement element, final PsiElement movingParent) {
    final PsiElement parent = element.getParent();
    if (parent instanceof PsiFile) return true;
    if (parent instanceof HCLObject) return true;
    if (parent instanceof HCLProperty) return true;
    if (parent instanceof HCLArray && parent.equals(movingParent)) return true;
    // know nothing about that
    return false;
  }

  private boolean checkMovingInsideOutside(@NotNull final PsiFile file, @NotNull final Editor editor, @NotNull final MoveInfo info, final boolean down) {
    final int offset = editor.getCaretModel().getOffset();

    Language language = findHCLOrLikeLanguage(file);
    if (language == null) return false;

    PsiElement elementAtOffset = file.getViewProvider().findElementAt(offset, language);
    if (elementAtOffset == null) return false;

    PsiElement brace = itIsTheClosingCurlyBraceWeAreMoving(file, editor);
    if (brace != null) {
      PsiElement parent = brace.getParent();
      // Not empty object
      if (parent instanceof HCLObject && (parent.getChildren().length != 0 || down)) {
        int line = editor.getDocument().getLineNumber(offset);
        final LineRange toMove = new LineRange(line, line + 1);
        toMove.firstElement = toMove.lastElement = brace;
        info.toMove = toMove;
      } else return info.prohibitMove();
    }

    // cannot move in/outside method/class/initializer/comment
    if (!calcInsertOffset(file, editor, info.toMove, info, down)) return false;
    if (brace != null) {
      int insertOffset = down ? getLineStartSafeOffset(editor.getDocument(), info.toMove2.endLine) : editor.getDocument().getLineStartOffset(info.toMove2.startLine);
      PsiElement elementAtInsertOffset = file.getViewProvider().findElementAt(insertOffset, language);

      if (PsiTreeUtil.getParentOfType(brace, HCLObject.class, false) != PsiTreeUtil.getParentOfType(elementAtInsertOffset, HCLObject.class, false)) {
        info.indentSource = true;
      }
    }

    return true;
  }

  private static LineRange expandLineRangeToCoverPsiElements(final LineRange range, Editor editor, final PsiFile file) {
    Pair<PsiElement, PsiElement> psiRange = getElementRange(editor, file, range);
    if (psiRange == null) return null;
    final PsiElement parent = PsiTreeUtil.findCommonParent(psiRange.getFirst(), psiRange.getSecond());
    Pair<PsiElement, PsiElement> elementRange = getElementRange(parent, psiRange.getFirst(), psiRange.getSecond());
    if (elementRange == null) return null;
    int endOffset = elementRange.getSecond().getTextRange().getEndOffset();
    Document document = editor.getDocument();
    if (endOffset > document.getTextLength()) {
      LOG.assertTrue(!PsiDocumentManager.getInstance(file.getProject()).isUncommited(document));
      LOG.assertTrue(PsiDocumentManagerImpl.checkConsistency(file, document));
    }
    int endLine;
    if (endOffset == document.getTextLength()) {
      endLine = document.getLineCount();
    } else {
      endLine = editor.offsetToLogicalPosition(endOffset).line + 1;
      endLine = Math.min(endLine, document.getLineCount());
    }
    int startLine = Math.min(range.startLine, editor.offsetToLogicalPosition(elementRange.getFirst().getNode().getStartOffset()).line);
    endLine = Math.max(endLine, range.endLine);
    return new LineRange(startLine, endLine);
  }

  private static PsiElement itIsTheClosingCurlyBraceWeAreMoving(final PsiFile file, final Editor editor) {
    LineRange range = getLineRangeFromSelection(editor);
    if (range.endLine - range.startLine != 1) return null;
    int offset = editor.getCaretModel().getOffset();
    Document document = editor.getDocument();
    int line = document.getLineNumber(offset);
    int lineStartOffset = document.getLineStartOffset(line);
    String lineText = document.getText().substring(lineStartOffset, document.getLineEndOffset(line));
    if (!lineText.trim().equals("}")) return null;

    return file.findElementAt(lineStartOffset + lineText.indexOf('}'));
  }


  @Nullable
  private static Language findHCLOrLikeLanguage(@NotNull final PsiFile file) {
    final Set<Language> languages = file.getViewProvider().getLanguages();
    for (final Language language : languages) {
      if (language == HCLLanguage.INSTANCE) return language;
    }
    for (final Language language : languages) {
      if (language.isKindOf(HCLLanguage.INSTANCE)) return language;
    }
    return null;
  }

  private static PsiElement @NotNull [] findStatementsInRange(@NotNull PsiFile file, int startOffset, int endOffset) {
    Language language = findHCLOrLikeLanguage(file);
    if (language == null) return PsiElement.EMPTY_ARRAY;
    FileViewProvider viewProvider = file.getViewProvider();
    PsiElement element1 = viewProvider.findElementAt(startOffset, language);
    PsiElement element2 = viewProvider.findElementAt(endOffset - 1, language);
    if (element1 instanceof PsiWhiteSpace) {
      startOffset = element1.getTextRange().getEndOffset();
      element1 = file.findElementAt(startOffset);
    }
    if (element2 instanceof PsiWhiteSpace) {
      endOffset = element2.getTextRange().getStartOffset();
      element2 = file.findElementAt(endOffset - 1);
    }
    if (element1 == null || element2 == null) return PsiElement.EMPTY_ARRAY;

    PsiElement parent = PsiTreeUtil.findCommonParent(element1, element2);
    if (parent == null) return PsiElement.EMPTY_ARRAY;
    while (true) {
      if (parent instanceof HCLProperty) break;
      if (parent instanceof HCLBlock) break;
      if (parent instanceof HCLArray) break;
      if (parent == null || parent instanceof PsiFile) return PsiElement.EMPTY_ARRAY;
      parent = parent.getParent();
    }

    //region Take outer possible child elements
    if (!parent.equals(element1)) {
      while (!parent.equals(element1.getParent())) {
        element1 = element1.getParent();
      }
    }
    if (startOffset != element1.getTextRange().getStartOffset()) return PsiElement.EMPTY_ARRAY;

    if (!parent.equals(element2)) {
      while (!parent.equals(element2.getParent())) {
        element2 = element2.getParent();
      }
    }
    if (endOffset != element2.getTextRange().getEndOffset()) return PsiElement.EMPTY_ARRAY;
    //endregion

    // Get all children in between
    ArrayList<PsiElement> array = new ArrayList<>();
    PsiElement next = element1;
    while (next != null) {
      if (!(next instanceof PsiWhiteSpace)) {
        array.add(next);
      }
      if (next.equals(element2)) break;
      next = next.getNextSibling();
    }
    // Ensure only proper elements would be returned
    for (PsiElement element : array) {
      if (!(element instanceof HCLElement || element instanceof PsiWhiteSpace || element instanceof PsiComment || element instanceof LeafPsiElement)) {
        return PsiElement.EMPTY_ARRAY;
      }
    }
    return PsiUtilCore.toPsiElementArray(array);
  }
}

