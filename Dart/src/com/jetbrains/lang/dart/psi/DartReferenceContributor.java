// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.psi;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Processor;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.injection.DartMultiHostInjector;
import com.jetbrains.lang.dart.resolve.DartResolver;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.PlatformPatterns.psiFile;
import static com.intellij.patterns.StandardPatterns.or;

public final class DartReferenceContributor extends PsiReferenceContributor {
  private static final Logger LOG = Logger.getInstance(DartReferenceContributor.class);

  @Override
  public void registerReferenceProviders(final @NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(
      or(psiElement(DartTokenTypesSets.MULTI_LINE_DOC_COMMENT),
         psiElement(DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT),
         psiElement(DartStringLiteralExpression.class),
         psiElement(XmlAttributeValue.class).inFile(psiFile().withLanguage(HTMLLanguage.INSTANCE)),
         psiElement(XmlTokenType.XML_DATA_CHARACTERS).inFile(psiFile().withLanguage(HTMLLanguage.INSTANCE))
      ),
      new DartReferenceProvider());
  }

  private static class DartReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(final @NotNull PsiElement element, final @NotNull ProcessingContext context) {
      if (element.getText().contains(DartMultiHostInjector.STRING_TEMPLATE_PLACEHOLDER)) {
        return PsiReference.EMPTY_ARRAY;
      }

      final Project project = element.getProject();
      final VirtualFile file = DartResolveUtil.getRealVirtualFile(InjectedLanguageManager.getInstance(project).getTopLevelFile(element));
      if (!DartAnalysisServerService.isLocalAnalyzableFile(file)) return PsiReference.EMPTY_ARRAY;

      final List<PsiReference> result = new SmartList<>();
      final TextRange elementRangeInHost = InjectedLanguageManager.getInstance(project).injectedToHost(element, element.getTextRange());

      final Processor<DartServerData.DartNavigationRegion> processor = navigationRegion -> {
        int navRegStartOffset = navigationRegion.getOffset();
        int navRegEndOffset = navRegStartOffset + navigationRegion.getLength();
        if (navRegStartOffset < elementRangeInHost.getStartOffset() || navRegEndOffset > elementRangeInHost.getEndOffset()) {
          LOG.error(element.getClass().getSimpleName() + " [" + element.getText() + "], file:" + element.getContainingFile().getName() +
                    ", elementRange=" + element.getTextRange() + ", elementRangeInHost=" + elementRangeInHost +
                    ", navigationRange=(" + navRegStartOffset + "," + navRegEndOffset + ")");
          return true;
        }

        result.add(new DartContributedReference(element, elementRangeInHost.getStartOffset(), navigationRegion));
        return true;
      };

      DartAnalysisServerService das = DartAnalysisServerService.getInstance(project);
      DartResolver.processRegionsInRange(das.getNavigation(file), elementRangeInHost, processor);

      return result.toArray(PsiReference.EMPTY_ARRAY);
    }
  }
}
