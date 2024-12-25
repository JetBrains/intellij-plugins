// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.moveCode;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.codeInsight.editorActions.moveUpDown.LineMover;
import com.intellij.codeInsight.editorActions.moveUpDown.LineRange;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Move top-level functions, entire class declarations, and class member declarations above or below the neighboring component.
 */
public final class DartComponentMover extends LineMover {

  private enum CommentType {
    SINGLE_LINE_COMMENT,
    MULTI_LINE_COMMENT,
    SINGLE_LINE_DOC_COMMENT,
    MULTI_LINE_DOC_COMMENT,
    INVALID,
    NONE
  }

  private static class CodeMover {
    final @NotNull Editor editor;
    final @NotNull PsiFile file;
    final @NotNull MoveInfo info;
    final boolean isMovingDown;
    Pair<PsiElement, PsiElement> sourceComponents;
    Pair<PsiElement, PsiElement> targetComponents;
    LineRange sourceRange;
    LineRange targetRange;

    CodeMover(@NotNull Editor editor, @NotNull PsiFile file, @NotNull MoveInfo info, boolean down) {
      this.editor = editor;
      this.file = file;
      this.info = info;
      this.isMovingDown = down;
    }

    static @Nullable Pair<PsiElement, PsiElement> findCommentRange(@NotNull PsiElement element) {
      if (!isComment(element) && !(element instanceof PsiWhiteSpace)) return null;
      PsiElement first = findFinalComment(element, false);
      PsiElement last = findFinalComment(element, true);
      if (commentTypeOf(first) != commentTypeOf(last)) {
        // Could be starting from whitespace at end of line comment.
        last = element.getPrevSibling();
      }
      return Pair.create(first, last);
    }

    boolean hasSourceComponents() {
      return sourceComponents != null && sourceComponents.first != null;
    }

    void findSourceComponents() {
      Pair<PsiElement, PsiElement> psiRange = getElementRange(editor, file, info.toMove);
      if (psiRange == null) return;
      PsiElement firstMember = getDeclarationParent(firstMovableComponent(psiRange.first));
      if (firstMember == null) return;
      PsiElement lastMember;
      if (isComment(firstMember)) {
        lastMember = findAttachedDeclaration(firstMember);
        if (lastMember == firstMember) lastMember = getDeclarationParent(psiRange.second);
      }
      else {
        lastMember = firstMember;
        firstMember = findAttachedComment(firstMember);
        if (firstMember == lastMember && !isMovingDown) {
          if (lastMember.getParent() instanceof DartClassMembers members) {
            PsiElement next = nextSib(members, false);
            if (next instanceof PsiWhiteSpace && !isCommentSeparator(next)) next = nextSib(next, false);
            if (isComment(next)) firstMember = next;
          }
        }
      }
      if (lastMember == null) return;
      PsiElement sibling = lastMember.getNextSibling();
      if (!isCommentSeparator(sibling)) {
        PsiElement next = firstNonWhiteElement(lastMember.getNextSibling(), true);
        if (isSemicolon(next)) lastMember = next;
        if (isMovingDown) {
          next = lastMember.getNextSibling();
          if (next == null && lastMember.getParent() instanceof DartClassMembers members) {
            next = nextSib(members, true);
          }
          if (next instanceof PsiWhiteSpace && !StringUtil.containsLineBreak(next.getText())) next = next.getNextSibling();
          if (next != null && isLineComment(next)) lastMember = next;
        }
      }
      sourceComponents = Pair.create(firstMember, lastMember);
    }

    boolean hasSourceLineRange() {
      return sourceRange != null;
    }

    void findSourceLineRange() {
      LineRange range;
      if (sourceComponents.first == sourceComponents.second) {
        range = memberRange(sourceComponents.first);
        if (range == null) return;
        range.firstElement = range.lastElement = sourceComponents.first;
      }
      else {
        final PsiElement parent = PsiTreeUtil.findCommonParent(sourceComponents.first, sourceComponents.second);
        if (parent == null) return;
        if (parent instanceof DartClassBody) {
          // This is an edge case that occurs when attempting to move a declaration out of a class body
          // and the declaration to be moved ends with a line comment (down) or has a preceding comment (up).
          // TODO Handle multi-line declarations. (This functions for single lines by defaulting to line mover.)
          return;
        }

        Pair<PsiElement, PsiElement> combinedRange;
        combinedRange = getElementRange(parent, sourceComponents.first, sourceComponents.second);
        if (combinedRange == null) return;
        final LineRange lineRange1 = memberRange(combinedRange.first);
        if (lineRange1 == null) return;
        final LineRange lineRange2 = memberRange(combinedRange.second);
        if (lineRange2 == null) return;
        range = new LineRange(lineRange1.startLine, lineRange2.endLine);
        range.firstElement = combinedRange.first;
        range.lastElement = combinedRange.second;
      }
      sourceRange = range;
    }

    boolean hasTargetComponents() {
      return targetComponents != null && targetComponents.first != null;
    }

    void findTargetComponents() {
      PsiElement ref = isMovingDown ? sourceRange.lastElement : sourceRange.firstElement;
      PsiElement sibling = nextSib(ref, isMovingDown);
      if (sibling instanceof PsiWhiteSpace && StringUtil.countNewLines(sibling.getText()) == 0) {
        PsiElement next = sibling.getNextSibling();
        if (isLineComment(next)) sibling = next.getNextSibling();
      }
      if (sibling == null && ref.getParent() instanceof DartClassMembers members) {
        sibling = nextSib(members, isMovingDown);
      }
      PsiElement firstElement = firstNonWhiteElement(sibling, isMovingDown);
      if (firstElement == null) firstElement = sibling == null ? ref : sibling;
      PsiElement lastElement;
      if (isComment(firstElement)) {
        lastElement = isCommentSeparator(sibling) ? firstElement : findAttachedDeclaration(firstElement);
      }
      else {
        lastElement = isMovingDown ? firstElement : findAttachedComment(firstElement);
      }
      if (firstElement instanceof PsiWhiteSpace || lastElement instanceof PsiWhiteSpace) {
        info.prohibitMove();
        return;
      }
      //PsiElement lastElement = isComment(firstElement) ? findAttachedDeclaration(firstElement) : findAttachedComment(firstElement);
      targetComponents = isMovingDown ? Pair.create(firstElement, lastElement) : Pair.create(lastElement, firstElement);
    }

    boolean hasTargetLineRange() {
      return targetRange != null;
    }

    void findTargetLineRange() {
      PsiElement source = isMovingDown ? sourceRange.lastElement : sourceRange.firstElement;
      PsiElement target = isMovingDown ? targetComponents.first : targetComponents.second;
      if (crossesHeaderBoundary(source, target)) {
        info.prohibitMove();
        return;
      }
      targetRange = new LineRange(targetComponents.first, targetComponents.second, editor.getDocument());
    }

    boolean areTargetsAtSameLevel() {
      return sourceComponents.second.getParent() == targetComponents.second.getParent();
    }

    private LineRange memberRange(@NotNull PsiElement member) {
      TextRange textRange = member.getTextRange();
      if (editor.getDocument().getTextLength() < textRange.getEndOffset()) return null;
      LogicalPosition startPosition = editor.offsetToLogicalPosition(textRange.getStartOffset());
      LogicalPosition endPosition = editor.offsetToLogicalPosition(textRange.getEndOffset());
      int endLine = endPosition.line + 1;
      return new LineRange(startPosition.line, endLine);
    }

    private static @NotNull PsiElement findAttachedDeclaration(@NotNull PsiElement element) {
      // Skip to the end of the comment (element) then return the next declaration if any, else element.
      PsiElement commentEnd = findFinalComment(element, true);
      if (isCommentSeparator(commentEnd.getNextSibling())) {
        return commentEnd;
      }
      PsiElement next = PsiTreeUtil.skipWhitespacesForward(commentEnd);
      return next == null ? element : (isComment(next) ? element : next);
    }

    private static @NotNull PsiElement findAttachedComment(@NotNull PsiElement element) {
      // Identify the first element that represents a comment prior to the given element.
      // The comment may be a single block comment or a series of line comments.
      // Do not mix types of comments. A line comment preceding a line-doc comment is not included.
      PsiElement sib = isComment(element) ? element : element.getPrevSibling();
      PsiElement commentStart = findFinalComment(sib == null ? element : sib, false);
      return commentStart == sib && (commentStart instanceof PsiWhiteSpace) ? element : commentStart;
    }

    private static PsiElement nextSib(@NotNull PsiElement element, boolean isForward) {
      return isForward ? element.getNextSibling() : element.getPrevSibling();
    }

    private static boolean isCommentSeparator(@Nullable PsiElement element) {
      return element instanceof PsiWhiteSpace && StringUtil.countNewLines(element.getText()) > 1;
    }

    private static @NotNull PsiElement findFinalComment(@NotNull PsiElement element, boolean isForward) {
      // The element argument may be either a comment or a whitespace node. Find the end of the comment
      // moving forward if isForward, or backward, until something other than the same kind of comment is found.
      PsiElement target = element, sib = element;
      CommentType groupType = null;
      while (sib != null) {
        if (sib instanceof PsiWhiteSpace) {
          if (isCommentSeparator(sib)) {
            break; // A "block" of line comments may not contain an empty line.
          }
          else {
            sib = nextSib(sib, isForward);
            continue;
          }
        }
        CommentType type;
        switch (type = commentTypeOf(sib)) {
          case INVALID -> {
            PsiElement parent = sib.getParent();
            if (parent instanceof DartFile) return sib;
            return findFinalComment(parent, isForward);
          }
          case MULTI_LINE_DOC_COMMENT -> {
            return sib;
          }
          case MULTI_LINE_COMMENT, SINGLE_LINE_DOC_COMMENT, SINGLE_LINE_COMMENT -> {
            if (groupType == null) {
              groupType = type;
            }
            if (groupType == type) {
              target = sib;
              sib = nextSib(sib, isForward);
            }
            else {
              sib = null;
            }
          }
          case NONE -> sib = null;
        }
      }
      return target;
    }

    private static boolean isLineComment(@NotNull PsiElement element) {
      return switch (commentTypeOf(element)) {
        case SINGLE_LINE_DOC_COMMENT, SINGLE_LINE_COMMENT -> true;
        default -> false;
      };
    }

    private static @NotNull CommentType commentTypeOf(@NotNull PsiElement element) {
      IElementType type = element.getNode().getElementType();
      if (type == DartTokenTypesSets.SINGLE_LINE_COMMENT) {
        return CommentType.SINGLE_LINE_COMMENT;
      }
      else if (type == DartTokenTypesSets.MULTI_LINE_COMMENT) {
        return CommentType.MULTI_LINE_COMMENT;
      }
      else if (type == DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT) {
        return CommentType.SINGLE_LINE_DOC_COMMENT;
      }
      else if (type == DartTokenTypesSets.MULTI_LINE_DOC_COMMENT) {
        return CommentType.MULTI_LINE_DOC_COMMENT;
      }
      else if (DartTokenTypesSets.DOC_COMMENT_CONTENTS.contains(type)) {
        // Somehow we got a child of a multi-line comment. Shouldn't happen, but might.
        return CommentType.INVALID;
      }
      else {
        return CommentType.NONE;
      }
    }

    private static PsiElement getDeclarationParent(PsiElement element) {
      if (isComment(element)) return element;
      PsiElement parent = getHeaderParent(element);
      if (parent != null) return parent;
      parent = PsiTreeUtil.getParentOfType(element, DartVarDeclarationList.class, false);
      if (parent != null && (parent.getParent() instanceof DartFile || parent.getParent() instanceof DartClassMembers)) {
        return parent;
      }
      if (element instanceof LeafPsiElement &&
          (element.getParent() instanceof DartFile || element.getParent() instanceof DartClassMembers)) {
        return element;
      }
      if (element instanceof DartClassMembers) {
        PsiElement last = element.getLastChild();
        last = PsiTreeUtil.skipSiblingsBackward(last, LeafPsiElement.class, PsiWhiteSpace.class);
        if (last != null) return last;
      }
      return PsiTreeUtil.getParentOfType(element, DartComponent.class, false);
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

    private static boolean isComment(final @NotNull PsiElement element) {
      final IElementType type = element.getNode().getElementType();
      return DartTokenTypesSets.COMMENTS.contains(type) || DartTokenTypesSets.DOC_COMMENT_CONTENTS.contains(type);
    }

    private static boolean isSemicolon(PsiElement element) {
      return element instanceof LeafPsiElement;
    }

    private static PsiElement firstMovableComponent(PsiElement element) {
      if (element instanceof DartClassMembers) {
        return element.getFirstChild();
      }
      return element;
    }
  }

  @Override
  public boolean checkAvailable(@NotNull Editor editor, @NotNull PsiFile file, @NotNull MoveInfo info, boolean down) {
    if (!(file instanceof DartFile)) return false;
    if (!super.checkAvailable(editor, file, info, down)) return false;
    CodeMover codeMover = new CodeMover(editor, file, info, down);
    codeMover.findSourceComponents();
    if (!codeMover.hasSourceComponents()) {
      return false;
    }
    codeMover.findSourceLineRange();
    if (!codeMover.hasSourceLineRange()) {
      return false;
    }
    codeMover.findTargetComponents();
    if (!codeMover.hasTargetComponents()) {
      return info.toMove2 == null; // Null if move is prohibited.
    }
    codeMover.findTargetLineRange();
    if (!codeMover.hasTargetLineRange()) {
      return info.toMove2 == null; // Null if move is prohibited.
    }
    if (codeMover.areTargetsAtSameLevel()) {
      info.indentTarget = false;
    }
    info.toMove = codeMover.sourceRange;
    info.toMove2 = codeMover.targetRange;
    return true;
  }

  @VisibleForTesting
  static Pair<PsiElement, PsiElement> findCommentRange(@NotNull PsiElement element) {
    return CodeMover.findCommentRange(element);
  }
}
