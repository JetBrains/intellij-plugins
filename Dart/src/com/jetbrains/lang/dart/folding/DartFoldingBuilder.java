package com.jetbrains.lang.dart.folding;

import com.intellij.codeInsight.folding.CodeFoldingSettings;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UnfairTextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DartFoldingBuilder extends CustomFoldingBuilder implements DumbAware {

  private static final String SMILEY = "<~>";

  protected void buildLanguageFoldRegions(@NotNull final List<FoldingDescriptor> descriptors,
                                          @NotNull final PsiElement root,
                                          @NotNull final Document document,
                                          final boolean quick) {
    if (!(root instanceof DartFile)) return;

    final TextRange fileHeaderRange = foldFileHeader(descriptors, (DartFile)root, document); // 1. File header
    foldImportExportStatements(descriptors, (DartFile)root);                                 // 2. Import and export statements
    Collection<PsiElement> psiElements = PsiTreeUtil.collectElementsOfType(root, new Class[]{DartTypeArguments.class, PsiComment.class});
    foldComments(descriptors, psiElements, fileHeaderRange);                                 // 3. Comments and comment sequences
    foldClassBodies(descriptors, (DartFile)root);                                            // 4. Class body
    foldFunctionBodies(descriptors, root);                                                   // 5. Function body
    foldTypeArguments(descriptors, psiElements);                                             // 6. Type arguments
  }

  protected String getLanguagePlaceholderText(@NotNull final ASTNode node, @NotNull final TextRange range) {
    final IElementType elementType = node.getElementType();
    final PsiElement psiElement = node.getPsi();

    if (psiElement instanceof DartFile) return "/.../";                              // 1.   File header
    if (psiElement instanceof DartImportOrExportStatement) return "...";             // 2.   Import and export statements
    if (elementType == DartTokenTypesSets.MULTI_LINE_DOC_COMMENT) return "/**...*/"; // 3.1. Multiline doc comments
    if (elementType == DartTokenTypesSets.MULTI_LINE_COMMENT) return "/*...*/";      // 3.2. Multiline comments
    if (elementType == DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT) return "///...";  // 3.3. Consequent single line doc comments
    if (elementType == DartTokenTypesSets.SINGLE_LINE_COMMENT) return "//...";       // 3.4. Consequent single line comments
    if (psiElement instanceof DartClassBody) return "{...}";                         // 4.   Class body
    if (psiElement instanceof DartFunctionBody) return "{...}";                      // 5.   Function body
    if (psiElement instanceof DartTypeArguments) return SMILEY;                      // 6.   Type arguments

    return "...";
  }

  protected boolean isRegionCollapsedByDefault(@NotNull final ASTNode node) {
    final IElementType elementType = node.getElementType();
    final PsiElement psiElement = node.getPsi();
    final CodeFoldingSettings settings = CodeFoldingSettings.getInstance();

    if (psiElement instanceof DartFile) return settings.COLLAPSE_FILE_HEADER;                        // 1. File header
    if (psiElement instanceof DartImportOrExportStatement) return settings.COLLAPSE_IMPORTS;         // 2. Import and export statements

    if (elementType == DartTokenTypesSets.MULTI_LINE_DOC_COMMENT ||                                  // 3.1. Multiline doc comments
        elementType ==
        DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT) {                                                // 3.3. Consequent single line doc comments
      return settings.COLLAPSE_DOC_COMMENTS;                                                         // 3.2 and 3.4 never collapsed by default
    }
    //                                                                                                  4. Class body never collapsed by default
    if (psiElement instanceof DartFunctionBody) return settings.COLLAPSE_METHODS;                    // 5. Function body

    DartCodeFoldingSettings dartSettings = DartCodeFoldingSettings.getInstance();
    if (psiElement instanceof DartTypeArguments) return dartSettings.isCollapseGenericParameters();  // 6. Type arguments

    return false;
  }

  @Nullable
  private static TextRange foldFileHeader(@NotNull final List<FoldingDescriptor> descriptors,
                                          @NotNull final DartFile dartFile,
                                          @NotNull final Document document) {
    PsiElement firstComment = dartFile.getFirstChild();
    if (firstComment instanceof PsiWhiteSpace) firstComment = firstComment.getNextSibling();

    if (!(firstComment instanceof PsiComment)) return null;

    boolean containsCustomRegionMarker = false;
    PsiElement nextAfterComments = firstComment;
    while (nextAfterComments instanceof PsiComment || nextAfterComments instanceof PsiWhiteSpace) {
      containsCustomRegionMarker |= isCustomRegionElement(nextAfterComments);
      nextAfterComments = nextAfterComments.getNextSibling();
    }

    if (nextAfterComments == null) return null;

    if (nextAfterComments instanceof DartLibraryStatement ||
        nextAfterComments instanceof DartPartStatement ||
        nextAfterComments instanceof DartPartOfStatement ||
        nextAfterComments instanceof DartImportOrExportStatement) {
      if (nextAfterComments.getPrevSibling() instanceof PsiWhiteSpace) nextAfterComments = nextAfterComments.getPrevSibling();
      if (nextAfterComments.equals(firstComment)) return null;
      final TextRange fileHeaderCommentsRange = new UnfairTextRange(firstComment.getTextOffset(), nextAfterComments.getTextOffset());
      if (fileHeaderCommentsRange.getLength() > 1 &&
          document.getLineNumber(fileHeaderCommentsRange.getEndOffset()) >
          document.getLineNumber(fileHeaderCommentsRange.getStartOffset())) {
        if (!containsCustomRegionMarker) {
          descriptors.add(new FoldingDescriptor(dartFile, fileHeaderCommentsRange));
        }
        return fileHeaderCommentsRange;
      }
    }
    return null;
  }

  private static void foldImportExportStatements(@NotNull final List<FoldingDescriptor> descriptors, final @NotNull DartFile dartFile) {
    final DartImportOrExportStatement firstImport = PsiTreeUtil.findChildOfType(dartFile, DartImportOrExportStatement.class);
    if (firstImport == null) return;

    PsiElement lastImport = firstImport;
    PsiElement nextElement = firstImport;
    while (nextElement instanceof DartImportOrExportStatement ||
           nextElement instanceof PsiComment ||
           nextElement instanceof PsiWhiteSpace) {
      if (nextElement instanceof DartImportOrExportStatement) {
        lastImport = nextElement;
      }
      nextElement = nextElement.getNextSibling();
    }

    if (lastImport != firstImport) {
      final int startOffset = firstImport.getTextOffset() + firstImport.getFirstChild().getTextLength() + 1; // after "import " or "export "
      final int endOffset = lastImport.getTextRange().getEndOffset();
      descriptors.add(new FoldingDescriptor(firstImport, TextRange.create(startOffset, endOffset)));
    }
  }

  private static void foldComments(@NotNull final List<FoldingDescriptor> descriptors,
                                   @NotNull final Collection<PsiElement> psiElements,
                                   @Nullable final TextRange fileHeaderRange) {
    PsiElement psiElement;
    for (Iterator<PsiElement> iter = psiElements.iterator(); iter.hasNext(); ) {
      psiElement = iter.next();
      if (!(psiElement instanceof PsiComment)) {
        continue;
      }
      if (fileHeaderRange != null && fileHeaderRange.intersects(psiElement.getTextRange())) {
        continue;
      }

      final IElementType elementType = psiElement.getNode().getElementType();
      if ((elementType == DartTokenTypesSets.MULTI_LINE_DOC_COMMENT || elementType == DartTokenTypesSets.MULTI_LINE_COMMENT)
          && !isCustomRegionElement(psiElement)) {
        descriptors.add(new FoldingDescriptor(psiElement, psiElement.getTextRange()));
      }
      else if (elementType == DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT || elementType == DartTokenTypesSets.SINGLE_LINE_COMMENT) {
        final PsiElement firstCommentInSequence = psiElement;
        PsiElement lastCommentInSequence = firstCommentInSequence;
        PsiElement nextElement = firstCommentInSequence;
        boolean containsCustomRegionMarker = isCustomRegionElement(nextElement);
        while (iter.hasNext() && (nextElement = nextElement.getNextSibling()) != null &&
               (nextElement instanceof PsiWhiteSpace || nextElement.getNode().getElementType() == elementType)) {
          if (nextElement.getNode().getElementType() == elementType) {
            // advance iterator to skip processed comments sequence
            iter.next();
            lastCommentInSequence = nextElement;
            containsCustomRegionMarker |= isCustomRegionElement(nextElement);
          }
        }

        if (lastCommentInSequence != firstCommentInSequence && !containsCustomRegionMarker) {
          final TextRange range =
            TextRange.create(firstCommentInSequence.getTextOffset(), lastCommentInSequence.getTextRange().getEndOffset());
          descriptors.add(new FoldingDescriptor(firstCommentInSequence, range));
        }
      }
    }
  }

  private static void foldClassBodies(@NotNull final List<FoldingDescriptor> descriptors, @NotNull final DartFile dartFile) {
    for (DartClassDefinition dartClass : PsiTreeUtil.getChildrenOfTypeAsList(dartFile, DartClassDefinition.class)) {
      final DartClassBody body = dartClass.getClassBody();
      if (body != null && body.getTextLength() > 2) {
        descriptors.add(new FoldingDescriptor(body, body.getTextRange()));
      }
    }
  }

  private static void foldFunctionBodies(@NotNull final List<FoldingDescriptor> descriptors, @NotNull final PsiElement root) {
    for (PsiElement dartComponent : PsiTreeUtil.findChildrenOfAnyType(root, DartComponent.class, DartOperatorDeclaration.class)) {
      final DartComponentType componentType = DartComponentType.typeOf(dartComponent);
      if (componentType == null) continue;

      switch (componentType) {
        case CLASS:
          final DartClassMembers classMembers = DartResolveUtil.getBody((DartClass)dartComponent);
          if (classMembers != null) {
            for (DartComponent childComponent : PsiTreeUtil.findChildrenOfType(classMembers, DartComponent.class)) {
              foldFunctionBody(descriptors, childComponent);
            }
          }
          break;
        case CONSTRUCTOR:
        case FUNCTION:
        case METHOD:
        case OPERATOR:
          foldFunctionBody(descriptors, dartComponent);
          break;
        default:
          break;
      }
    }
  }

  private static void foldFunctionBody(@NotNull final List<FoldingDescriptor> descriptors,
                                       @NotNull final PsiElement dartComponentOrOperatorDeclaration) {
    final DartFunctionBody functionBody = PsiTreeUtil.getChildOfType(dartComponentOrOperatorDeclaration, DartFunctionBody.class);
    final DartBlock block = functionBody == null ? null : functionBody.getBlock();
    if (block != null && block.getTextLength() > 2) {
      descriptors.add(new FoldingDescriptor(functionBody, block.getTextRange()));
    }
  }

  private static void foldTypeArguments(@NotNull final List<FoldingDescriptor> descriptors,
                                        @NotNull final Collection<PsiElement> psiElements) {
    for (PsiElement psiElement : psiElements) {
      if (psiElement instanceof DartTypeArguments) {
        DartTypeArguments dartTypeArguments = (DartTypeArguments)psiElement;
        if (PsiTreeUtil.getParentOfType(dartTypeArguments, DartNewExpression.class, DartTypeArguments.class) instanceof DartNewExpression) {
          descriptors.add(new FoldingDescriptor(dartTypeArguments, TextRange
            .create(dartTypeArguments.getTextOffset(), dartTypeArguments.getTextRange().getEndOffset())));
        }
      }
    }
  }
}
