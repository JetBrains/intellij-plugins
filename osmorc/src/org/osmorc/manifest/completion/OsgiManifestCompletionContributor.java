package org.osmorc.manifest.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.ManifestLanguage;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.ManifestTokenType;
import org.osmorc.manifest.ManifestConstants;
import org.osmorc.manifest.lang.psi.Directive;

/**
 * @author Vladislav.Soroka
 */
public class OsgiManifestCompletionContributor extends CompletionContributor {
  public OsgiManifestCompletionContributor() {
    extend(CompletionType.BASIC,
           PlatformPatterns.psiElement(ManifestTokenType.HEADER_VALUE_PART).withLanguage(ManifestLanguage.INSTANCE)
             .withSuperParent(2, PlatformPatterns.psiElement(Directive.class).withName(ManifestConstants.Directives.NO_IMPORT))
             .withSuperParent(4, PlatformPatterns.psiElement(Header.class).withName(ManifestConstants.Headers.EXPORT_PACKAGE)),
           new CompletionProvider<CompletionParameters>() {
             @Override
             public void addCompletions(@NotNull CompletionParameters completionParameters,
                                        ProcessingContext processingContext,
                                        @NotNull CompletionResultSet completionResultSet) {
               PsiElement psiElement = completionParameters.getOriginalPosition();
               if (psiElement!= null && psiElement.getPrevSibling() == null) {
                 completionResultSet.addElement(LookupElementBuilder.create("true"));
                 completionResultSet.addElement(LookupElementBuilder.create("false"));
               }
             }
           }
    );

    extend(CompletionType.BASIC,
           PlatformPatterns.psiElement(ManifestTokenType.HEADER_VALUE_PART).withLanguage(ManifestLanguage.INSTANCE)
             .withSuperParent(2, PlatformPatterns.psiElement(Directive.class).withName(ManifestConstants.Directives.SPLIT_PACKAGE))
             .withSuperParent(4, PlatformPatterns.psiElement(Header.class).withName(ManifestConstants.Headers.EXPORT_PACKAGE)),
           new CompletionProvider<CompletionParameters>() {
             @Override
             public void addCompletions(@NotNull CompletionParameters completionParameters,
                                        ProcessingContext processingContext,
                                        @NotNull CompletionResultSet completionResultSet) {
               PsiElement psiElement = completionParameters.getOriginalPosition();
               if (psiElement != null && psiElement.getPrevSibling() == null) {
                 completionResultSet.addElement(LookupElementBuilder.create("merge-first"));
                 completionResultSet.addElement(LookupElementBuilder.create("merge-last"));
                 completionResultSet.addElement(LookupElementBuilder.create("first"));
                 completionResultSet.addElement(LookupElementBuilder.create("error"));
               }
             }
           }
    );

    extend(CompletionType.BASIC,
           PlatformPatterns.psiElement(ManifestTokenType.HEADER_VALUE_PART).withLanguage(ManifestLanguage.INSTANCE)
             .withSuperParent(4, PlatformPatterns.psiElement(Header.class).withName(ManifestConstants.Headers.EXPORT_PACKAGE)),
           new ExportPackageCompletionProvider()
    );

    extend(CompletionType.BASIC,
           PlatformPatterns.psiElement(ManifestTokenType.HEADER_VALUE_PART).withLanguage(ManifestLanguage.INSTANCE)
             .withSuperParent(3, PlatformPatterns.psiElement(Header.class).withName(ManifestConstants.Headers.EXPORT_PACKAGE)),
           new ExportPackageCompletionProvider()
    );

    extend(CompletionType.BASIC,
           PlatformPatterns.psiElement(ManifestTokenType.HEADER_VALUE_PART).withLanguage(ManifestLanguage.INSTANCE)
             .withSuperParent(4, PlatformPatterns.psiElement(Header.class).withName(ManifestConstants.Headers.IMPORT_PACKAGE)),
           new ImportPackageCompletionProvider()
    );
  }
}
