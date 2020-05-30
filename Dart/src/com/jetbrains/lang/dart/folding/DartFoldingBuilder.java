// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.folding;

import com.intellij.codeInsight.folding.CodeFoldingSettings;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UnfairTextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class DartFoldingBuilder extends CustomFoldingBuilder implements DumbAware {

  private static final String SMILEY = "<~>";
  private static final String DOT_DOT_DOT = "...";
  private static final String BRACE_DOTS = "{...}";
  private static final String PAREN_DOTS = "(...)";
  private static final String FILE_HEADER = "/.../";
  private static final String MULTI_LINE_DOC_COMMENT = "/**...*/";
  private static final String MULTI_LINE_COMMENT = "/*...*/";
  private static final String SINGLE_LINE_DOC_COMMENT = "///...";
  private static final String SINGLE_LINE_COMMENT = "//...";

  private static final String TEST_METHOD_NAME = "test";
  private static final String GROUP_METHOD_NAME = "group";

  @Override
  protected boolean isCustomFoldingRoot(@NotNull final ASTNode node) {
    final IElementType type = node.getElementType();
    return type == DartTokenTypesSets.DART_FILE || type == DartTokenTypes.CLASS_BODY || type == DartTokenTypes.FUNCTION_BODY;
  }

  @Override
  protected void buildLanguageFoldRegions(@NotNull final List<FoldingDescriptor> descriptors,
                                          @NotNull final PsiElement root,
                                          @NotNull final Document document,
                                          final boolean quick) {
    if (!(root instanceof DartFile)) return;

    final DartFile dartFile = (DartFile)root;
    final TextRange fileHeaderRange = foldFileHeader(descriptors, dartFile, document); // 1. File header
    foldConsequentStatements(descriptors, dartFile, DartImportOrExportStatement.class);// 2. Import and export statements
    foldConsequentStatements(descriptors, dartFile, DartPartStatement.class);          // 3. Part statements
    final Collection<PsiElement> psiElements = PsiTreeUtil.findChildrenOfAnyType(
      root,
      DartComponent.class,
      DartTypeArguments.class,
      PsiComment.class,
      DartStringLiteralExpression.class,
      DartSetOrMapLiteralExpression.class,
      DartNewExpression.class,
      DartCallExpression.class,
      DartAssertStatement.class,
      DartIfStatement.class);
    foldComments(descriptors, psiElements, fileHeaderRange);                           // 4. Comments and comment sequences
    foldClassBodies(descriptors, dartFile);                                            // 5. Class bodies
    foldFunctionBodies(descriptors, psiElements);                                      // 6. Function bodies
    foldTypeArguments(descriptors, psiElements);                                       // 7. Type arguments
    foldMultilineStrings(descriptors, psiElements);                                    // 8. Multi-line strings
    foldSetOrMapLiterals(descriptors, psiElements);                                    // 9. Set or Map literals
    foldSomeDartCallExpressions(descriptors, psiElements);                             // 10. Constructor, "test", "group" invocations
    foldAssertExpressions(descriptors, psiElements);                                   // 11. Assert statements
    foldIfStatements(descriptors, psiElements);                                        // 12. If statements
  }

  @Override
  @NotNull
  protected String getLanguagePlaceholderText(@NotNull final ASTNode node, @NotNull final TextRange range) {
    final IElementType elementType = node.getElementType();
    final PsiElement psiElement = node.getPsi();

    if (psiElement instanceof DartFile) return FILE_HEADER;                                      // 1.   File header
    if (psiElement instanceof DartImportOrExportStatement) return DOT_DOT_DOT;                   // 2.   Import and export statements
    if (psiElement instanceof DartPartStatement) return DOT_DOT_DOT;                             // 3.   Part statements
    if (elementType == DartTokenTypesSets.MULTI_LINE_DOC_COMMENT) return MULTI_LINE_DOC_COMMENT; // 4.1. Multiline doc comments
    if (elementType == DartTokenTypesSets.MULTI_LINE_COMMENT) return MULTI_LINE_COMMENT;         // 4.2. Multiline comments
    if (elementType == DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT) {
      return SINGLE_LINE_DOC_COMMENT;                                                            // 4.3. Consequent single line doc comments
    }
    if (elementType == DartTokenTypesSets.SINGLE_LINE_COMMENT) return SINGLE_LINE_COMMENT;       // 4.4. Consequent single line comments
    if (psiElement instanceof DartClassBody || psiElement instanceof DartEnumDefinition) {
      return BRACE_DOTS;                                                                         // 5.   Class body
    }
    if (psiElement instanceof DartFunctionBody) return BRACE_DOTS;                               // 6.   Function body
    if (psiElement instanceof DartTypeArguments) return SMILEY;                                  // 7.   Type arguments
    if (psiElement instanceof DartStringLiteralExpression) {
      return multilineStringPlaceholder(node);                                                   // 8.   Multi-line strings
    }
    if (psiElement instanceof DartSetOrMapLiteralExpression) return BRACE_DOTS;                  // 9.   Set or Map literals
    if (psiElement instanceof DartArguments) return PAREN_DOTS;                                  // 10.1 Constructor invocations
    if (psiElement instanceof DartExpression) return DOT_DOT_DOT;                                // 10.2 Second arg in test methods

    if (psiElement instanceof DartBlock) return BRACE_DOTS;                                      // 12.  If statements

    return DOT_DOT_DOT;
  }

  @Override
  protected boolean isRegionCollapsedByDefault(@NotNull final ASTNode node) {
    final IElementType elementType = node.getElementType();
    final PsiElement psiElement = node.getPsi();
    final CodeFoldingSettings settings = CodeFoldingSettings.getInstance();
    final DartCodeFoldingSettings dartSettings = DartCodeFoldingSettings.getInstance();

    if (psiElement instanceof DartFile) return settings.COLLAPSE_FILE_HEADER;                        // 1. File header
    if (psiElement instanceof DartImportOrExportStatement) return settings.COLLAPSE_IMPORTS;         // 2. Import and export statements
    if (psiElement instanceof DartPartStatement) return dartSettings.isCollapseParts();              // 3. Part statements

    if (elementType == DartTokenTypesSets.MULTI_LINE_DOC_COMMENT ||                                  // 4.1. Multiline doc comments
        elementType ==
        DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT) {                                                // 4.3. Consequent single line doc comments
      return settings.COLLAPSE_DOC_COMMENTS;                                                         // 4.2 and 4.4 never collapsed by default
    }
    //                                                                                                  5. Class body never collapsed by default
    if (psiElement instanceof DartFunctionBody) return settings.COLLAPSE_METHODS;                    // 6. Function body
    if (psiElement instanceof DartTypeArguments) return dartSettings.isCollapseGenericParameters();  // 7. Type arguments

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
      final TextRange fileHeaderCommentsRange =
        new UnfairTextRange(firstComment.getTextRange().getStartOffset(), nextAfterComments.getTextRange().getStartOffset());
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

  private static <T extends PsiElement> void foldConsequentStatements(@NotNull final List<FoldingDescriptor> descriptors,
                                                                      @NotNull final DartFile dartFile,
                                                                      @NotNull final Class<T> aClass) {
    final T firstStatement = PsiTreeUtil.getChildOfType(dartFile, aClass);
    if (firstStatement == null) return;

    PsiElement lastStatement = firstStatement;
    PsiElement nextElement = firstStatement;
    while (aClass.isInstance(nextElement) ||
           nextElement instanceof PsiComment ||
           nextElement instanceof PsiWhiteSpace) {
      if (aClass.isInstance(nextElement)) {
        lastStatement = nextElement;
      }
      nextElement = nextElement.getNextSibling();
    }

    if (lastStatement != firstStatement) {
      // after "import " or "export " or "part "
      final int startOffset = firstStatement.getTextRange().getStartOffset() + firstStatement.getFirstChild().getTextLength() + 1;
      final int endOffset = lastStatement.getTextRange().getEndOffset();
      final FoldingDescriptor descriptor = new FoldingDescriptor(firstStatement, TextRange.create(startOffset, endOffset));
      if (aClass == DartImportOrExportStatement.class) {
        // imports are often added/removed automatically, so we enable autoupdate of folded region for foldings even if it's collapsed
        descriptor.setCanBeRemovedWhenCollapsed(true);
      }
      descriptors.add(descriptor);
    }
  }

  private static void foldComments(@NotNull final List<FoldingDescriptor> descriptors,
                                   @NotNull final Collection<PsiElement> psiElements,
                                   @Nullable final TextRange fileHeaderRange) {
    for (Iterator<PsiElement> iter = psiElements.iterator(); iter.hasNext(); ) {
      PsiElement psiElement = iter.next();
      if (!(psiElement instanceof PsiComment)) {
        continue;
      }
      if (fileHeaderRange != null && fileHeaderRange.intersects(psiElement.getTextRange())) {
        continue;
      }

      final IElementType elementType = psiElement.getNode().getElementType();
      if ((elementType == DartTokenTypesSets.MULTI_LINE_DOC_COMMENT || elementType == DartTokenTypesSets.MULTI_LINE_COMMENT) &&
          !isCustomRegionElement(psiElement)) {
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
            TextRange.create(firstCommentInSequence.getTextRange().getStartOffset(), lastCommentInSequence.getTextRange().getEndOffset());
          descriptors.add(new FoldingDescriptor(firstCommentInSequence, range));
        }
      }
    }
  }

  private static void foldClassBodies(@NotNull final List<FoldingDescriptor> descriptors, @NotNull final DartFile dartFile) {
    for (PsiElement element : PsiTreeUtil.getChildrenOfAnyType(dartFile, DartClass.class, DartExtensionDeclaration.class)) {
      if (element instanceof DartClassDefinition) {
        final DartClassBody body = ((DartClassDefinition)element).getClassBody();
        if (body != null && body.getTextLength() > 2) {
          descriptors.add(new FoldingDescriptor(body, body.getTextRange()));
        }
      }
      else if (element instanceof DartEnumDefinition) {
        final ASTNode lBrace = element.getNode().findChildByType(DartTokenTypes.LBRACE);
        final ASTNode rBrace = element.getNode().findChildByType(DartTokenTypes.RBRACE, lBrace);
        if (lBrace != null && rBrace != null && rBrace.getStartOffset() - lBrace.getStartOffset() > 2) {
          descriptors.add(new FoldingDescriptor(element, TextRange.create(lBrace.getStartOffset(), rBrace.getStartOffset() + 1)));
        }
      }
      else if (element instanceof DartExtensionDeclaration) {
        final DartClassBody body = ((DartExtensionDeclaration)element).getClassBody();
        if (body.getTextLength() > 2) {
          descriptors.add(new FoldingDescriptor(body, body.getTextRange()));
        }
      }
    }
  }

  private static void foldFunctionBodies(@NotNull final List<FoldingDescriptor> descriptors,
                                         @NotNull final Collection<PsiElement> psiElements) {
    for (PsiElement dartComponent : psiElements) {
      final DartComponentType componentType = DartComponentType.typeOf(dartComponent);
      if (componentType == null) continue;

      switch (componentType) {
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
    final IDartBlock block = functionBody == null ? null : functionBody.getBlock();
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
            .create(dartTypeArguments.getTextRange().getStartOffset(), dartTypeArguments.getTextRange().getEndOffset())));
        }
      }
    }
  }

  private static void foldMultilineStrings(@NotNull final List<FoldingDescriptor> descriptors,
                                           @NotNull final Collection<PsiElement> psiElements) {
    for (PsiElement element : psiElements) {
      if (element instanceof DartStringLiteralExpression) {
        DartStringLiteralExpression dartString = (DartStringLiteralExpression)element;
        PsiElement child = dartString.getFirstChild();
        if (child == null) continue;
        IElementType type = child.getNode().getElementType();
        if (type == DartTokenTypes.RAW_TRIPLE_QUOTED_STRING || (type == DartTokenTypes.OPEN_QUOTE && child.getTextLength() == 3)) {
          descriptors.add(new FoldingDescriptor(dartString, dartString.getTextRange()));
        }
      }
    }
  }

  private static void foldSomeDartCallExpressions(@NotNull final List<FoldingDescriptor> descriptors,
                                                  @NotNull final Collection<PsiElement> psiElements) {
    for (PsiElement psiElement : psiElements) {
      if (psiElement instanceof DartNewExpression) {
        DartNewExpression dartNewExpression = (DartNewExpression)psiElement;
        foldNonEmptyDartArguments(descriptors, dartNewExpression.getArguments());
      }
      else if (psiElement instanceof DartCallExpression) {
        DartCallExpression dartCallExpression = (DartCallExpression)psiElement;
        // test for capitalization test
        final String methodName = dartCallExpression.getExpression().getText();
        if (StringUtil.isCapitalized(methodName)) {
          foldNonEmptyDartArguments(descriptors, dartCallExpression.getArguments());
        }
        else if (TEST_METHOD_NAME.equals(methodName) || GROUP_METHOD_NAME.equals(methodName)) {
          foldTestDartArguments(descriptors, dartCallExpression.getArguments());
        }
      }
    }
  }

  private static void foldAssertExpressions(@NotNull final List<FoldingDescriptor> descriptors,
                                            @NotNull final Collection<PsiElement> psiElements) {
    for (PsiElement psiElement : psiElements) {
      if (psiElement instanceof DartAssertStatement) {
        final ASTNode openParenNode = psiElement.getNode().findChildByType(DartTokenTypes.LPAREN);
        if (openParenNode == null) return;

        final PsiElement closeParenElt = psiElement.getLastChild();
        if (closeParenElt == null || closeParenElt.getNode().getElementType() != DartTokenTypes.RPAREN) {
          return;
        }

        final int startOffset = openParenNode.getStartOffset() + openParenNode.getTextLength();
        final int endOffset = closeParenElt.getTextRange().getStartOffset();
        final String text = psiElement.getText().substring(startOffset - psiElement.getTextRange().getStartOffset(),
                                                           endOffset - psiElement.getTextRange().getStartOffset());
        if (text.contains("\n")) {
          descriptors.add(new FoldingDescriptor(psiElement, TextRange.create(startOffset, endOffset)));
        }
      }
    }
  }

  private static void foldIfStatements(@NotNull final List<FoldingDescriptor> descriptors,
                                       @NotNull final Collection<PsiElement> psiElements) {
    for (PsiElement psiElement : psiElements) {
      if (psiElement instanceof DartIfStatement) {
        final DartIfStatement dartIfStatement = (DartIfStatement)psiElement;
        final List<DartBlock> dartBlockList = dartIfStatement.getBlockList();
        for (DartBlock dartBlock : dartBlockList) {
          descriptors.add(new FoldingDescriptor(dartBlock, dartBlock.getTextRange()));
        }
      }
    }
  }

  private static void foldNonEmptyDartArguments(@NotNull final List<FoldingDescriptor> descriptors,
                                                @Nullable final DartArguments dartArguments) {
    if (dartArguments == null || dartArguments.getArgumentList() == null) return;

    DartArgumentList dartArgumentList = dartArguments.getArgumentList();
    if (dartArgumentList.getExpressionList().isEmpty() && dartArgumentList.getNamedArgumentList().isEmpty()) return;

    descriptors.add(new FoldingDescriptor(dartArguments, dartArguments.getTextRange()));
  }

  private static void foldTestDartArguments(@NotNull final List<FoldingDescriptor> descriptors,
                                            @Nullable final DartArguments dartArguments) {
    if (dartArguments == null || dartArguments.getArgumentList() == null) return;

    DartArgumentList dartArgumentList = dartArguments.getArgumentList();
    if (dartArgumentList.getExpressionList().size() != 2) return;

    final DartExpression secondExpression = dartArgumentList.getExpressionList().get(1);
    if (secondExpression == null) return;

    final String text = secondExpression.getText();
    if (text != null && text.contains("\n")) {
      descriptors.add(new FoldingDescriptor(secondExpression, secondExpression.getTextRange()));
    }
  }

  @NotNull
  private static String multilineStringPlaceholder(@NotNull final ASTNode node) {
    ASTNode child = node.getFirstChildNode();
    if (child == null) return DOT_DOT_DOT;
    if (child.getElementType() == DartTokenTypes.RAW_TRIPLE_QUOTED_STRING) {
      String text = child.getText();
      String quotes = text.substring(1, 4);
      return "r" + quotes + DOT_DOT_DOT + quotes;
    }
    if (child.getElementType() == DartTokenTypes.OPEN_QUOTE) {
      String text = child.getText();
      String quotes = text.substring(0, 3);
      return quotes + DOT_DOT_DOT + quotes;
    }
    return DOT_DOT_DOT;
  }

  private static void foldSetOrMapLiterals(@NotNull final List<FoldingDescriptor> descriptors,
                                           @NotNull final Collection<PsiElement> psiElements) {
    for (PsiElement psiElement : psiElements) {
      if (psiElement instanceof DartSetOrMapLiteralExpression) {
        final ASTNode node = psiElement.getNode();
        final ASTNode lBrace = node.findChildByType(DartTokenTypes.LBRACE);
        final ASTNode rBrace = lBrace == null ? null : node.findChildByType(DartTokenTypes.RBRACE, lBrace);
        if (lBrace != null && rBrace != null) {
          final String text =
            node.getText().substring(lBrace.getStartOffset() - node.getStartOffset(), rBrace.getStartOffset() - node.getStartOffset());
          if (text.contains("\n")) {
            descriptors.add(new FoldingDescriptor(psiElement, TextRange.create(lBrace.getStartOffset(),
                                                                               rBrace.getStartOffset() + rBrace.getTextLength())));
          }
        }
      }
    }
  }
}
