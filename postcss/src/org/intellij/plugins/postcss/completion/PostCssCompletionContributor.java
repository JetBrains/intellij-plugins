package org.intellij.plugins.postcss.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssNamedElement;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.ObjectUtils;
import org.intellij.plugins.postcss.psi.PostCssCustomMedia;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomMediaIndex;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomSelectorIndex;
import org.intellij.plugins.postcss.references.PostCssCustomMediaReference;
import org.intellij.plugins.postcss.references.PostCssCustomSelectorReference;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class PostCssCompletionContributor extends CompletionContributor {

  public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull final CompletionResultSet result) {
    if (result.isStopped()) return;
    final PsiElement position = parameters.getPosition();
    if (!PostCssPsiUtil.isInsidePostCss(position)) return;

    PsiElementPattern.Capture<PsiElement> isIdent = psiElement(CssElementTypes.CSS_IDENT);
    boolean isCustomSelector = isIdent.withReference(PostCssCustomSelectorReference.class).accepts(position);
    boolean isCustomMedia = isIdent.withReference(PostCssCustomMediaReference.class).accepts(position);
    if (isCustomSelector) {
      addVariantsForCustomSelector(parameters, result);
    }
    else if (isCustomMedia) {
      addVariantsForCustomMedia(parameters, result);
    }
  }

  private static void addVariantsForCustomSelector(@NotNull final CompletionParameters parameters,
                                                   @NotNull final CompletionResultSet result) {
    final PsiElement position = parameters.getPosition();
    Project project = position.getProject();
    final GlobalSearchScope scope = CssUtil.getCompletionAndResolvingScopeForElement(position);
    final Set<VirtualFile> importedFiles = CssUtil.getImportedFiles(parameters.getOriginalFile(), position, false);
    for (String name : StubIndex.getInstance().getAllKeys(PostCssCustomSelectorIndex.KEY, project)) {
      if (name.isEmpty()) continue;
      for (PostCssCustomSelector element : StubIndex
        .getElements(PostCssCustomSelectorIndex.KEY, name, project, scope, PostCssCustomSelector.class)) {
        result.addElement(createCustomElementLookup(element, importedFiles));
      }
    }
  }

  private static void addVariantsForCustomMedia(@NotNull final CompletionParameters parameters,
                                                @NotNull final CompletionResultSet result) {
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

  @NotNull
  private static LookupElement createCustomElementLookup(@NotNull final CssNamedElement element,
                                                         @NotNull final Set<VirtualFile> importedFiles) {
    //TODO replace with appropriate constant when PostCSS module will be part of API
    int priority = 10 + (importedFiles.contains(element.getContainingFile().getVirtualFile()) ? 1 : 0);

    ItemPresentation itemPresentation = ObjectUtils.notNull(element.getPresentation());
    return PrioritizedLookupElement.withPriority(
      LookupElementBuilder.createWithSmartPointer("--" + element.getName(), element)
        .withPresentableText(ObjectUtils.notNull(itemPresentation.getPresentableText()))
        .withIcon(itemPresentation.getIcon(false))
        .withTypeText(itemPresentation.getLocationString(), true), priority);
  }
}