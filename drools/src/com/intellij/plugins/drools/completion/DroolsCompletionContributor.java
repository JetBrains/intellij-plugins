// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.plugins.drools.lang.lexer.DroolsTokenTypeSets;
import com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes;
import com.intellij.plugins.drools.lang.psi.*;
import com.intellij.plugins.drools.lang.psi.util.DroolsResolveUtil;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.plugins.drools.JbossDroolsIcons;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class DroolsCompletionContributor extends CompletionContributor {
  public DroolsCompletionContributor() {
    extendRhsStatement();
    extendPatternBindExpressions();
    extendKeywords();
    extendAttributes();
    extendDialectAttribute();
    extendRule();
  }

  private void extendRule() {
    final PsiElementPattern.Capture<PsiElement> with = psiElement().with(new PatternCondition<>("DroolsRule") {
      @Override
      public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
        final DroolsRuleStatement droolsRule = PsiTreeUtil.getParentOfType(psiElement, DroolsRuleStatement.class);
        return droolsRule != null;
      }
    });
    extend(CompletionType.BASIC,
           with,
           new CompletionProvider<>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           @NotNull ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               final PsiElement position = parameters.getPosition();

               final DroolsRuleStatement droolsRule = PsiTreeUtil.getParentOfType(position, DroolsRuleStatement.class);
               if (droolsRule != null) {
                 if (droolsRule.getLhs() == null) {
                   final List<DroolsRhs> rhs = droolsRule.getRhsList();

                   if (rhs.size() == 0 ||
                       (rhs.size() == 1 &&
                        rhs.iterator().next().getTextRange().getStartOffset() > position.getTextRange().getStartOffset())) {
                     result.addElement(LookupElementBuilder.create("when").bold());
                   }
                 }

                 result.addElement(LookupElementBuilder.create("then  end").withPresentableText("then").bold().withTailText(" end")
                                     .withInsertHandler(new InsertHandler<>() {
                                       @Override
                                       public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
                                         final Editor editor = context.getEditor();
                                         editor.getCaretModel().moveToOffset(context.getTailOffset() - 4);
                                       }
                                     }));
               }
             }
           });
  }

  private void extendKeywords() {
    final PsiElementPattern.Capture<PsiElement> with = psiElement().with(new PatternCondition<>("DroolsAttributes") {
      @Override
      public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
        final PsiElement parent = psiElement.getParent();
        if (parent instanceof DroolsFile || (parent instanceof PsiErrorElement && parent.getParent() instanceof DroolsFile)) return true;

        return parent instanceof DroolsRuleStatement ||
               (parent instanceof PsiErrorElement && parent.getParent() instanceof DroolsRuleStatement);
      }
    });
    extend(CompletionType.BASIC,
           with,
           new CompletionProvider<>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           @NotNull ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               for (String keyword : Arrays.asList("package ", "import ", "rule ", "function ", "declare ", "global ")) {
                 result.addElement(
                   LookupElementBuilder.create(keyword).bold().withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE));
               }
             }
           });
  }

  private void extendDialectAttribute() {
    final PsiElementPattern.Capture<PsiElement> with = psiElement().with(new PatternCondition<>("DroolsAttributes") {
      @Override
      public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
        if (psiElement.getParent() instanceof DroolsStringLiteral) {
          final DroolsAttribute attribute = PsiTreeUtil.getParentOfType(psiElement, DroolsAttribute.class);
          return attribute != null && "dialect".equals(attribute.getAttributeName());
        }

        return false;
      }
    });
    extend(CompletionType.BASIC, with,
           new CompletionProvider<>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           @NotNull ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               result.addElement(LookupElementBuilder.create("mvel").bold());
               result.addElement(LookupElementBuilder.create("java").bold());
             }
           });
  }

  private void extendAttributes() {
    final PsiElementPattern.Capture<PsiElement> with = psiElement().with(new PatternCondition<>("DroolsAttributes") {
      @Override
      public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
        final PsiElement parent = psiElement.getParent();
        if (parent instanceof DroolsFile || (parent instanceof PsiErrorElement && parent.getParent() instanceof DroolsFile)) {
          return true;
        }

        return parent instanceof DroolsRuleStatement ||
               (parent instanceof PsiErrorElement && parent.getParent() instanceof DroolsRuleStatement);
      }
    });
    extend(CompletionType.BASIC,
           with,
           new CompletionProvider<>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           @NotNull ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               final List<String> stringAttrs =
                 Arrays
                   .asList("agenda-group", "activation-group", "ruleflow-group", "date-effective", "date-expires", "dialect");
               for (IElementType elementType : DroolsTokenTypeSets.KEYWORD_ATTRS.getTypes()) {
                 String keyword = elementType.toString();
                 if (stringAttrs.contains(keyword)) {
                   result.addElement(
                     LookupElementBuilder.create(keyword + " \"\"").bold().withPresentableText(keyword)
                       .withTailText(" \"\"", true)
                       .withInsertHandler(new InsertHandler<>() {
                         @Override
                         public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
                           final Editor editor = context.getEditor();
                           editor.getCaretModel().moveToOffset(context.getTailOffset() - 1);
                         }
                       }));
                 }
                 else {
                   result.addElement(LookupElementBuilder.create(keyword).bold());
                 }
               }
             }
           });
  }

  private void extendPatternBindExpressions() {
    final PsiElementPattern.Capture<PsiElement> with = psiElement().with(new PatternCondition<>("DroolsPatternBind") {
      @Override
      public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
        final PsiElement prevSibling = DroolsResolveUtil.getPrevSiblingSkipWhiteSpaces(psiElement, true);

        if (prevSibling != null) {
          return prevSibling.getNode().getElementType() == DroolsTokenTypes.COLON;
        }
        final PsiElement parent = psiElement.getParent();
        if (parent instanceof PsiErrorElement) {
          return DroolsResolveUtil.getPrevSiblingSkipWhiteSpaces(parent, true) instanceof DroolsLhs;
        }
        return false;
      }
    });
    extend(CompletionType.BASIC,
           with,
           new CompletionProvider<>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           @NotNull ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               final PsiFile file = parameters.getPosition().getContainingFile();
               if (file instanceof DroolsFile) {

                 for (PsiClass psiClass : DroolsResolveUtil.getExplicitlyImportedClasses((DroolsFile)file)) {
                   result.addElement(
                     LookupElementBuilder.create(psiClass).withIcon(psiClass.getIcon(0)).appendTailText("(expression)", true)
                       .withInsertHandler(
                         ParenthesesInsertHandler.WITH_PARAMETERS));
                 }
               }
             }
           });
  }

  @Override
  public void beforeCompletion(@NotNull CompletionInitializationContext context) {
    if (context.getCompletionType() == CompletionType.SMART) return;
    super.beforeCompletion(context);
    final PsiFile file = context.getFile();
    if (file instanceof DroolsFile) {
      context.setDummyIdentifier(CompletionInitializationContext.DUMMY_IDENTIFIER_TRIMMED);
    }
  }

  private void extendRhsStatement() {
    final PsiElementPattern.Capture<PsiElement> rhsKeywordPattern =
      psiElement().with(new PatternCondition<>("rhsKeywords") {
        @Override
        public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext context) {
          final PsiFile file = psiElement.getContainingFile();
          if (file instanceof DroolsFile) {
            final PsiStatement type = PsiTreeUtil.getParentOfType(psiElement, PsiStatement.class);
            if (type != null && psiElement.getTextRange().getStartOffset() == type.getTextRange().getStartOffset()) return true;
          }
          return false;
        }
      });

    extend(CompletionType.BASIC,
           rhsKeywordPattern,
           new CompletionProvider<>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           @NotNull ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               for (String keyword : Arrays.asList("insert", "insertLogical", "retract", "update")) {
                 result.addElement(
                   LookupElementBuilder.create(keyword).bold().withIcon(JbossDroolsIcons.Drools_16)
                     .appendTailText("(expression)", true)
                     .withInsertHandler(
                       ParenthesesInsertHandler.WITH_PARAMETERS));
               }

               result.addElement(LookupElementBuilder.create("modify").bold()
                                   .withIcon(JbossDroolsIcons.Drools_16)
                                   .appendTailText("(expression){}", true)
                                   .withInsertHandler(new MyModifyStatementInsertHandler()));
             }
           });
  }

  private static class MyModifyStatementInsertHandler extends ParenthesesInsertHandler {
    @Override
    protected boolean placeCaretInsideParentheses(InsertionContext context, LookupElement item) {
      return true;
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
      super.handleInsert(context, item);
      final Editor editor = context.getEditor();
      final Document document = editor.getDocument();
      document.insertString(context.getTailOffset(), "{}");
    }
  }
}
