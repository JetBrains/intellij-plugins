package org.intellij.plugins.postcss.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssNamedElement;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.css.impl.util.completion.provider.PseudoSelectorsCompletionProvider;
import com.intellij.psi.css.util.CssCompletionUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import org.intellij.plugins.postcss.psi.PostCssCustomMedia;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomMediaIndex;
import org.intellij.plugins.postcss.references.PostCssCustomMediaReference;
import org.intellij.plugins.postcss.references.PostCssCustomSelectorReference;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

import static com.intellij.patterns.PlatformPatterns.psiElement;

class PostCssCompletionContributor extends CompletionContributor {
  PostCssCompletionContributor() {
    extend(CompletionType.BASIC, customSelector(), new PseudoSelectorsCompletionProvider());
  }

  private static @NotNull ElementPattern<? extends PsiElement> customSelector() {
    return psiElement(CssElementTypes.CSS_IDENT).withReference(PostCssCustomSelectorReference.class);
  }

  @Override
  public void fillCompletionVariants(final @NotNull CompletionParameters parameters, final @NotNull CompletionResultSet result) {
    if (result.isStopped()) return;
    final PsiElement position = parameters.getPosition();
    if (!PostCssPsiUtil.isInsidePostCss(position)) return;

    PsiElementPattern.Capture<PsiElement> isIdent = psiElement(CssElementTypes.CSS_IDENT);
    boolean isCustomMedia = isIdent.withReference(PostCssCustomMediaReference.class).accepts(position);
    if (isCustomMedia) {
      addVariantsForCustomMedia(parameters, result);
    }
  }

  private static void addVariantsForCustomMedia(final @NotNull CompletionParameters parameters,
                                                final @NotNull CompletionResultSet result) {
    final PsiElement position = parameters.getPosition();
    Project project = position.getProject();
    final GlobalSearchScope scope = CssUtil.getCompletionAndResolvingScopeForElement(position);
    final Set<VirtualFile> importedFiles = CssUtil.getImportedFiles(parameters.getOriginalFile(), position, false);
    for (String name : StubIndex.getInstance().getAllKeys(PostCssCustomMediaIndex.KEY, project)) {
      if (name.isEmpty()) continue;
      for (PostCssCustomMedia element : StubIndex
        .getElements(PostCssCustomMediaIndex.KEY, name, project, scope, PostCssCustomMedia.class)) {
        result.addElement(createCustomElementLookup(element, importedFiles));
      }
    }
  }

  private static @NotNull LookupElement createCustomElementLookup(final @NotNull CssNamedElement element,
                                                                  final @NotNull Set<VirtualFile> importedFiles) {
    int priority = CssCompletionUtil.CSS_PSEUDO_SELECTOR_PRIORITY +
                   (importedFiles.contains(element.getContainingFile().getVirtualFile()) ? 1 : 0);

    ItemPresentation itemPresentation = Objects.requireNonNull(element.getPresentation());
    return PrioritizedLookupElement.withPriority(
      LookupElementBuilder.createWithSmartPointer("--" + element.getName(), element)
        .withPresentableText(Objects.requireNonNull(itemPresentation.getPresentableText()))
        .withIcon(itemPresentation.getIcon(false))
        .withTypeText(itemPresentation.getLocationString(), true), priority);
  }
}