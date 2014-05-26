package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.codecompletion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.rngom.binary.ElementPattern;

/**
 * PhoneGapCodeCompletion.java
 *
 * Created by Masahiro Suzuka on 2014/05/25.
 */
public class PhoneGapCodeCompletion extends CompletionContributor {

  public PhoneGapCodeCompletion() {
    // Event completion
    // See http://docs.phonegap.com/en/edge/cordova_events_events.md.html#Events
    extend(CompletionType.BASIC,
        PlatformPatterns.psiElement(),
        new CompletionProvider<CompletionParameters>() {
          @Override
          protected void addCompletions(@NotNull CompletionParameters completionParameters,
                                        ProcessingContext processingContext,
                                        @NotNull CompletionResultSet completionResultSet) {
            completionResultSet.addElement(LookupElementBuilder.create("deviceready"));
            completionResultSet.addElement(LookupElementBuilder.create("pause"));
            completionResultSet.addElement(LookupElementBuilder.create("resume"));
            completionResultSet.addElement(LookupElementBuilder.create("backbutton"));
            completionResultSet.addElement(LookupElementBuilder.create("menubutton"));
            completionResultSet.addElement(LookupElementBuilder.create("searchbutton"));
            completionResultSet.addElement(LookupElementBuilder.create("startcallbutton"));
            completionResultSet.addElement(LookupElementBuilder.create("endcallbutton"));
            completionResultSet.addElement(LookupElementBuilder.create("volumedownbutton"));
            completionResultSet.addElement(LookupElementBuilder.create("volumeupbutton"));
          }
        }
    );

    // Plugin completion
    // See http://docs.phonegap.com/en/edge/cordova_plugins_pluginapis.md.html#Plugin%20APIs
  }
}
