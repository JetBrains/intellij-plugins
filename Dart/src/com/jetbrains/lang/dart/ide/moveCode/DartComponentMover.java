package com.jetbrains.lang.dart.ide.moveCode;

import com.intellij.codeInsight.editorActions.moveUpDown.LineMover;
import com.intellij.codeInsight.editorActions.moveUpDown.LineRange;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * Move top-level functions, entire class declarations, and class member declarations above or below the neighboring component.
 * Movement may not cross semantic boundaries. Use a proper refactoring tool for that to ensure error detection.
 */
public class DartComponentMover extends LineMover {

  @Override
  public boolean checkAvailable(@NotNull Editor editor, @NotNull PsiFile file, @NotNull MoveInfo info, boolean down) {
    if (!(file instanceof DartFile)) return false;
    if (!super.checkAvailable(editor, file, info, down)) return false;
    info.indentTarget = false;
    Pair<PsiElement, PsiElement> psiRange = getElementRange(editor, file, info.toMove);
    if (psiRange == null) return false;
    PsiElement firstMember = getDeclarationParent(psiRange.getFirst());
    PsiElement endElement = psiRange.getSecond();
    PsiElement lastMember = getDeclarationParent(endElement);
    if (firstMember == null || lastMember == null) return false;

    LineRange range;
    if (firstMember == lastMember) {
      if (firstMember instanceof LeafPsiElement && firstMember.getText().equals("/**")) {
        firstMember = firstMember.getParent();
      }
      range = memberRange(firstMember, editor, info.toMove);
      if (range == null) return false;
      range.firstElement = range.lastElement = firstMember;
    }
    else {
      final PsiElement parent = PsiTreeUtil.findCommonParent(firstMember, lastMember);
      if (parent == null) return false;

      final Pair<PsiElement, PsiElement> combinedRange;
      if (parent instanceof DartDocComment) {
        range = memberRange(parent, editor, info.toMove);
        if (range == null) return false;
        range.firstElement = parent;
        range.lastElement = parent;
      }
      else {
        combinedRange = getElementRange(parent, firstMember, lastMember);
        if (combinedRange == null) return false;
        final LineRange lineRange1 = memberRange(combinedRange.getFirst(), editor, info.toMove);
        if (lineRange1 == null) return false;
        final LineRange lineRange2 = memberRange(combinedRange.getSecond(), editor, info.toMove);
        if (lineRange2 == null) return false;
        range = new LineRange(lineRange1.startLine, lineRange2.endLine);
        range.firstElement = combinedRange.getFirst();
        range.lastElement = combinedRange.getSecond();
      }
    }
    Document document = editor.getDocument();
    PsiElement ref;
    PsiElement sibling = down ? (ref = range.lastElement.getNextSibling()) : (ref = range.firstElement).getPrevSibling();
    sibling = firstNonWhiteElement(skipSemicolon(sibling, down), down);
    ref = firstNonWhiteElement(skipSemicolon(ref, !down), !down);
    info.toMove = range;
    if (sibling != null) {
      if (crossesHeaderBoundary(ref, sibling)) {
        info.prohibitMove();
        return true;
      }
      info.toMove2 = new LineRange(sibling, sibling, document);
    }
    return true;
  }

  private static PsiElement getDeclarationParent(PsiElement element) {
    if (isComment(element)) return element;
    PsiElement parent = getHeaderParent(element);
    if (parent != null) return parent;
    parent = PsiTreeUtil.getParentOfType(element, DartVarDeclarationList.class, false);
    if (parent != null && (parent.getParent() instanceof DartFile || parent.getParent() instanceof DartClassMembers)) {
      return parent;
    }
    if (element instanceof LeafPsiElement && element.getParent() instanceof DartFile) {
      return element;
    }
    if (element instanceof DartClassMembers) {
      PsiElement last = element.getLastChild();
      last = PsiTreeUtil.skipSiblingsBackward(last, LeafPsiElement.class, PsiWhiteSpace.class);
      if (last != null) return last;
    }
    return PsiTreeUtil.getParentOfType(element, DartComponent.class, false);
  }

  private static LineRange memberRange(@NotNull PsiElement member, Editor editor, LineRange lineRange) {
    TextRange textRange = member.getTextRange();
    if (editor.getDocument().getTextLength() < textRange.getEndOffset()) return null;
    LogicalPosition startPosition = editor.offsetToLogicalPosition(textRange.getStartOffset());
    LogicalPosition endPosition = editor.offsetToLogicalPosition(textRange.getEndOffset());
    int endLine = endPosition.line + 1;
    if (!isInsideDeclaration(member, startPosition.line, endLine, lineRange, editor)) return null;
    return new LineRange(startPosition.line, endLine);
  }

  private static boolean isInsideDeclaration(@NotNull PsiElement member, int startLine, int endLine, LineRange lineRange, Editor editor) {
    // Easy case: selection is on first or last line of component.
    if (startLine == lineRange.startLine || startLine == lineRange.endLine || endLine == lineRange.startLine ||
        endLine == lineRange.endLine) {
      return true;
    }
    if (startLine <= lineRange.startLine && endLine >= lineRange.endLine) {
      return true;
    }
    return false;
  }

  private static boolean crossesHeaderBoundary(PsiElement base, PsiElement sibling) {
    // We may want to be more flexible and allow header statements to move up past top-level declarations.
    if (isComment(base) || isComment(sibling)) return false;
    PsiElement baseType = getHeaderParent(base);
    PsiElement sibType = getHeaderParent(sibling);
    if (baseType == null && sibType == null) return false;
    if (baseType == null || sibType == null) return true;
    // Having two library statements is not legal but could happen while editing.
    if (baseType instanceof DartLibraryStatement && sibType instanceof DartLibraryStatement) return false;
    // Having mixed library and import is also possible but not allowed in this context.
    if (baseType instanceof DartLibraryStatement || sibType instanceof DartLibraryStatement) return true;
    return false; // both uri-based -- allow moving part & import
  }

  private static PsiElement getHeaderParent(PsiElement element) {
    return PsiTreeUtil.getNonStrictParentOfType(element, DartUriBasedDirective.class, DartLibraryStatement.class);
  }

  private static boolean isComment(@NotNull final PsiElement element) {
    final IElementType type = element.getNode().getElementType();
    return DartTokenTypesSets.COMMENTS.contains(type) || DartTokenTypesSets.DOC_COMMENT_CONTENTS.contains(type);
  }

  private static PsiElement skipSemicolon(PsiElement element, boolean lookRight) {
    if (element instanceof LeafPsiElement) {
      element = lookRight ? element.getNextSibling() : element.getPrevSibling();
    }
    return element;
  }
}
