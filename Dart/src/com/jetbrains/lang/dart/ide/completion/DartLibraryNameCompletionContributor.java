package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.ide.settings.DartSettings;
import com.jetbrains.lang.dart.psi.DartId;
import com.jetbrains.lang.dart.psi.DartLibraryId;
import com.jetbrains.lang.dart.psi.DartPathOrLibraryReference;
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * @author: Fedor.Korotkov
 */
public class DartLibraryNameCompletionContributor extends CompletionContributor {
  public DartLibraryNameCompletionContributor() {
    extend(CompletionType.BASIC,
           psiElement().withSuperParent(2, DartPathOrLibraryReference.class).withParent(
             DartStringLiteralExpression.class),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               final Set<String> names = DartLibraryIndex.getAllLibraryNames(parameters.getPosition().getProject());
               names.addAll(ContainerUtil.map(getStdLibraries(parameters.getPosition()), new Function<String, String>() {
                 @Override
                 public String fun(String coreLib) {
                   return "dart:" + coreLib;
                 }
               }));
               names.add("package:");
               for (String libraryName : names) {
                 if (libraryName.endsWith(".dart")) {
                   continue;
                 }
                 result.addElement(new QuotedStringLookupElement(libraryName));
               }
             }
           });
    extend(CompletionType.BASIC,
           psiElement().withSuperParent(1, DartId.class).withSuperParent(2, DartLibraryId.class),
           new CompletionProvider<CompletionParameters>() {
             @Override
             protected void addCompletions(@NotNull CompletionParameters parameters,
                                           ProcessingContext context,
                                           @NotNull CompletionResultSet result) {
               for (String libraryName : DartLibraryIndex.getAllLibraryNames(parameters.getPosition().getProject())) {
                 result.addElement(LookupElementBuilder.create(libraryName));
               }
             }
           });
  }

  private static Collection<? extends String> getStdLibraries(PsiElement context) {
    DartSettings settings = DartSettings.getSettingsForModule(ModuleUtilCore.findModuleForPsiElement(context));
    return settings != null ? settings.getLibraries(context) : Collections.<String>emptyList();
  }

  public static class QuotedStringLookupElement extends LookupElement {
    private final String myName;

    public QuotedStringLookupElement(String name) {
      myName = name;
    }

    @NotNull
    @Override
    public String getLookupString() {
      return myName;
    }

    @Override
    public void renderElement(LookupElementPresentation presentation) {
      super.renderElement(presentation);
      presentation.setIcon(icons.DartIcons.Dart_16);
    }

    @Override
    public void handleInsert(InsertionContext context) {
      Document document = context.getDocument();
      int start = context.getStartOffset();
      int end = context.getTailOffset();
      if (start < 1 || end > document.getTextLength() - 1) return;
      CharSequence sequence = document.getCharsSequence();
      boolean left = sequence.charAt(start - 1) == sequence.charAt(start);
      boolean right = sequence.charAt(end - 1) == sequence.charAt(end);
      if (left || right) {
        document.replaceString(start, end, sequence.subSequence(left ? start + 1 : start, right ? end - 1 : end));
        if (right) {
          context.getEditor().getCaretModel().moveCaretRelatively(1, 0, false, false, true);
        }
      }
    }
  }
}
