package org.intellij.plugins.markdown.lang.references;

import com.intellij.openapi.paths.PathReferenceManager;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.StringPattern;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownLinkDestinationImpl;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.PlatformPatterns.psiFile;
import static com.intellij.patterns.StandardPatterns.string;

public class MarkdownReferenceProvider extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    final StringPattern anchorPattern = string().startsWith("#");

    final PsiElementPattern.Capture<MarkdownLinkDestinationImpl> linkDestinationCapture =
      psiElement(MarkdownLinkDestinationImpl.class).inFile(psiFile(MarkdownFile.class));

    registrar.registerReferenceProvider(linkDestinationCapture.withoutText(anchorPattern), new LinkDestinationReferenceProvider());
    registrar.registerReferenceProvider(linkDestinationCapture.withText(anchorPattern), new AnchorReferenceProvider());
  }

  private static class AnchorReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
      if (!(element instanceof MarkdownLinkDestinationImpl)) return PsiReference.EMPTY_ARRAY;

      return ContainerUtil.ar(new MarkdownHeaderReference((MarkdownLinkDestinationImpl)element));
    }
  }

  private static class LinkDestinationReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
      return PathReferenceManager.getInstance().createReferences(element, false, false, true);
    }
  }
}
