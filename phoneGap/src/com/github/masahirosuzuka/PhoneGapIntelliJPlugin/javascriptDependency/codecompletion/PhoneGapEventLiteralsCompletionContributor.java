package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.javascriptDependency.codecompletion;

import com.github.masahirosuzuka.PhoneGapIntelliJPlugin.PhoneGapUtil;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * PhoneGapCodeCompletion.java
 * <p/>
 * Created by Masahiro Suzuka on 2014/05/25.
 */
public class PhoneGapEventLiteralsCompletionContributor extends CompletionContributor {

  public PhoneGapEventLiteralsCompletionContributor() {

    extend(CompletionType.BASIC, PlatformPatterns.psiElement(JSTokenTypes.STRING_LITERAL), getProvider());
  }

  private static CompletionProvider<CompletionParameters> getProvider() {
    return new CompletionProvider<>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters completionParameters,
                                    @NotNull ProcessingContext processingContext,
                                    @NotNull CompletionResultSet completionResultSet) {
        Project project = completionParameters.getEditor().getProject();

        if (null == project || !PhoneGapUtil.isPhoneGapProject(project)) {
          return;
        }
        completionResultSet.addElement(LookupElementBuilder.create("deviceready"));
        completionResultSet.addElement(LookupElementBuilder.create("pause"));
        completionResultSet.addElement(LookupElementBuilder.create("resume"));
        completionResultSet.addElement(LookupElementBuilder.create("online"));
        completionResultSet.addElement(LookupElementBuilder.create("offline"));
        completionResultSet.addElement(LookupElementBuilder.create("backbutton"));
        completionResultSet.addElement(LookupElementBuilder.create("menubutton"));
        completionResultSet.addElement(LookupElementBuilder.create("searchbutton"));
        completionResultSet.addElement(LookupElementBuilder.create("startcallbutton"));
        completionResultSet.addElement(LookupElementBuilder.create("endcallbutton"));
        completionResultSet.addElement(LookupElementBuilder.create("volumedownbutton"));
        completionResultSet.addElement(LookupElementBuilder.create("volumeupbutton"));
      }
    };
  }
}
