package com.jetbrains.lang.dart.psi;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.injection.DartMultiHostInjector;
import com.jetbrains.lang.dart.resolve.DartResolver;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.PlatformPatterns.psiFile;
import static com.intellij.patterns.StandardPatterns.or;

public class DartReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
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
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
      if (element.getText().contains(DartMultiHostInjector.STRING_TEMPLATE_PLACEHOLDER)) {
        return PsiReference.EMPTY_ARRAY;
      }

      final VirtualFile file = DartResolveUtil.getRealVirtualFile(
        InjectedLanguageManager.getInstance(element.getProject()).getTopLevelFile(element));
      if (!DartAnalysisServerService.isLocalAnalyzableFile(file)) return PsiReference.EMPTY_ARRAY;

      final List<PsiReference> result = new SmartList<>();
      final TextRange range = InjectedLanguageManager.getInstance(element.getProject()).injectedToHost(element, element.getTextRange());
      DartResolver.processRegionsInRange(DartAnalysisServerService.getInstance(element.getProject()).getNavigation(file),
                                         range,
                                         region -> result.add(new DartContributedReference(element, region)));
      return result.toArray(PsiReference.EMPTY_ARRAY);
    }
  }
}
