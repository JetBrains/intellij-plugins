package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartCodeGenerateUtil;
import com.jetbrains.lang.dart.util.UsefulPsiTreeUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class DartKeywordCompletionContributor extends CompletionContributor {
  private static final Set<String> allowedKeywords = new THashSet<String>() {
    {
      for (IElementType elementType : DartTokenTypesSets.RESERVED_WORDS.getTypes()) {
        add(elementType.toString());
      }
      for (IElementType elementType : DartTokenTypesSets.BUILT_IN_IDENTIFIERS.getTypes()) {
        add(elementType.toString());
      }
    }
  };

  public DartKeywordCompletionContributor() {
    final PsiElementPattern.Capture<PsiElement> idInExpression =
      psiElement().withSuperParent(1, DartId.class).withSuperParent(2, DartReference.class);
    final PsiElementPattern.Capture<PsiElement> inComplexExpression = psiElement().withSuperParent(3, DartReference.class);
    final PsiElementPattern.Capture<PsiElement> inStringLiteral = psiElement().inside(DartStringLiteralExpression.class);

    final PsiElementPattern.Capture<PsiElement> elementCapture = psiElement()
      .andNot(idInExpression.and(inComplexExpression))
      .andNot(inStringLiteral);

    extend(CompletionType.BASIC,
           elementCapture,
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               final Collection<String> suggestedKeywords = suggestKeywords(parameters.getPosition());
               suggestedKeywords.retainAll(allowedKeywords);
               for (String keyword : suggestedKeywords) {
                 result.addElement(LookupElementBuilder.create(keyword));
               }
             }
           });
    extend(CompletionType.BASIC,
           psiElement().inFile(StandardPatterns.instanceOf(DartFile.class)).withParent(DartClassDefinition.class),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               result.addElement(LookupElementBuilder.create(DartTokenTypes.IMPLEMENTS.toString()));
             }
           });
    extend(CompletionType.BASIC,
           psiElement().inFile(StandardPatterns.instanceOf(DartFile.class)).andOr(
             psiElement().withParent(DartClassDefinition.class),
             psiElement().withParent(DartInterfaceDefinition.class)
           ),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               result.addElement(LookupElementBuilder.create(DartTokenTypes.EXTENDS.toString()));
             }
           });
  }

  private static Collection<String> suggestKeywords(PsiElement position) {
    final TextRange posRange = position.getTextRange();
    final PsiElement posFile = position.getContainingFile();

    final List<PsiElement> pathToBlockStatement = UsefulPsiTreeUtil.getPathToParentOfType(position, DartBlock.class);
    final DartPsiCompositeElement classInterface = PsiTreeUtil.getParentOfType(position, DartClassMembers.class, DartInterfaceMembers.class);

    final String text;
    final int offset;
    if (pathToBlockStatement != null) {
      final Pair<String, Integer> pair = DartCodeGenerateUtil.wrapStatement(posRange.substring(posFile.getText()));
      text = pair.getFirst();
      offset = pair.getSecond();
    }
    else if (classInterface != null) {
      final Pair<String, Integer> pair = DartCodeGenerateUtil.wrapFunction(posRange.substring(posFile.getText()));
      text = pair.getFirst();
      offset = pair.getSecond();
    }
    else {
      DartEmbeddedContent embeddedContent = PsiTreeUtil.getParentOfType(position, DartEmbeddedContent.class);
      int startOffset = embeddedContent != null ? embeddedContent.getTextOffset() : 0;
      text = posRange.getStartOffset() == 0 ? "" : posFile.getText().substring(startOffset, posRange.getStartOffset());
      offset = 0;
    }

    final List<String> result = new ArrayList<String>();
    if (pathToBlockStatement != null && pathToBlockStatement.size() > 2) {
      final PsiElement blockChild = pathToBlockStatement.get(pathToBlockStatement.size() - 3);
      result.addAll(suggestBySibling(UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(blockChild, true)));
    }

    final PsiFile file =
      PsiFileFactory.getInstance(posFile.getProject()).createFileFromText("a.dart", DartLanguage.INSTANCE, text, true, false);
    GeneratedParserUtilBase.CompletionState state = new GeneratedParserUtilBase.CompletionState(text.length() - offset);
    file.putUserData(GeneratedParserUtilBase.COMPLETION_STATE_KEY, state);
    TreeUtil.ensureParsed(file.getNode());
    result.addAll(state.items);
    return result;
  }

  @NotNull
  private static Collection<? extends String> suggestBySibling(@Nullable PsiElement sibling) {
    if (DartIfStatement.class.isInstance(sibling)) {
      return Arrays.asList(DartTokenTypes.ELSE.toString());
    }
    else if (DartTryStatement.class.isInstance(sibling) || DartCatchPart.class.isInstance(sibling)) {
      return Arrays.asList(DartTokenTypes.CATCH.toString());
    }

    return Collections.emptyList();
  }
}
