// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.XmlAttributeReferenceCompletionProvider;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.xml.XmlAttributeReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.XmlPatterns.xmlAttribute;
import static com.intellij.patterns.XmlPatterns.xmlTag;


public final class JadeCompletionContributor extends CompletionContributor {
  private static final String[] STATEMENTS = {
    "if", "else", "else if", "until", "while", "unless", "each", "for", "case", "when", "include", "extends", "doctype", "default", "yield"
  };

  private static final String[] TAGS_WITHOUT_CLASSES_COMPLETION = {
    "script", "style"
  };

  private static final InsertHandler<LookupElement> NAME_INSERT_HANDLER = new InsertHandler<>() {
    @Override
    public void handleInsert(final @NotNull InsertionContext context, final @NotNull LookupElement item) {
      final Editor editor = context.getEditor();
      final Document document = editor.getDocument();
      final int caretOffset = editor.getCaretModel().getOffset();
      final CharSequence chars = document.getCharsSequence();
      if (!CharArrayUtil.regionMatches(chars, caretOffset, "=")) {
        String toInsert = "=";
        document.insertString(caretOffset, toInsert);
        if ('=' == context.getCompletionChar()) {
          context.setAddCompletionChar(false);
        }
      }

      editor.getCaretModel().moveToOffset(caretOffset + 1);
      editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
      editor.getSelectionModel().removeSelection();
    }
  };

  public JadeCompletionContributor() {
    extend(CompletionType.BASIC, psiElement().withParent(xmlTag()), new JadeTagCompletionProvider());
    extend(CompletionType.BASIC, psiElement().inside(xmlAttribute()), new JadeAttributeCompletionProvider());
  }

  private static class JadeTagCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
      final PsiElement elementOfCompletion = parameters.getPosition();
      final XmlTag element = PsiTreeUtil.getParentOfType(elementOfCompletion, XmlTag.class);
      assert element != null : "Pattern says that parent is a tag";

      for (final String s : STATEMENTS) {
        result.consume(LookupElementBuilder.create(s).bold().withInsertHandler(new InsertHandler<>() {
          @Override
          public void handleInsert(final @NotNull InsertionContext context, final @NotNull LookupElement item) {
            if (context.getCompletionChar() == ' ') {
              return;
            }
            if ("else".equals(s) || "default".equals(s) || "yield".equals(s)) {
              return;
            }
            Project project = context.getProject();
            Editor editor = context.getEditor();
            final int offset = editor.getCaretModel().getOffset();
            editor.getDocument().insertString(offset, " ");
            PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
            editor.getCaretModel().moveCaretRelatively(1, 0, false, false, true);
          }
        }));
      }
    }
  }

  private static class JadeAttributeCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
      if (parameters.getInvocationCount() == 0 && shouldProhibitClassCompletion(parameters)) {
        result.stopHere();
        return;
      }

      final PsiElement elementOfCompletion = parameters.getPosition();
      final PsiReference reference = elementOfCompletion.getContainingFile().findReferenceAt(parameters.getOffset());
      if (reference instanceof XmlAttributeReference) {
        XmlAttributeReferenceCompletionProvider.addAttributeReferenceCompletionVariants((XmlAttributeReference)reference, result,
                                                                                        NAME_INSERT_HANDLER);
        result.stopHere();
      }
    }
  }

  private static boolean shouldProhibitClassCompletion(@NotNull CompletionParameters parameters) {
    final PsiElement elementOfCompletion = parameters.getPosition();
    final XmlTag tagElement = PsiTreeUtil.getParentOfType(elementOfCompletion, XmlTag.class);
    final int offset = parameters.getOffset();

    assert tagElement != null : "Attribute is inside the tag";
    assert offset > 0;

    if (parameters.getEditor().getDocument().getImmutableCharSequence().charAt(offset - 1) != '.') {
      return false;
    }
    return ArrayUtil.contains(tagElement.getLocalName(), TAGS_WITHOUT_CLASSES_COMPLETION);
  }
}
