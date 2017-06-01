package com.jetbrains.lang.dart.psi;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.resolve.DartResolver;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.or;

public class DartReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(or(psiElement(DartTokenTypesSets.MULTI_LINE_DOC_COMMENT),
                                           psiElement(DartTokenTypesSets.SINGLE_LINE_DOC_COMMENT)),
                                        new DartReferenceProvider());
  }

  private static class DartReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
      final VirtualFile file = DartResolveUtil.getRealVirtualFile(element.getContainingFile());
      if (!DartAnalysisServerService.isLocalAnalyzableFile(file)) return PsiReference.EMPTY_ARRAY;

      final List<PsiReference> result = new SmartList<>();
      DartResolver.processRegionsInRange(DartAnalysisServerService.getInstance(element.getProject()).getNavigation(file),
                                         element.getTextRange(),
                                         region -> result.add(new DartContributedReference(element, region)));
      return result.toArray(PsiReference.EMPTY_ARRAY);
    }
  }
}
