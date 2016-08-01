package org.intellij.plugins.postcss.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.ObjectUtils;
import org.intellij.plugins.postcss.psi.PostCssCustomSelector;
import org.intellij.plugins.postcss.psi.PostCssPsiUtil;
import org.intellij.plugins.postcss.psi.stubs.PostCssCustomSelectorIndex;
import org.intellij.plugins.postcss.references.PostCssCustomSelectorReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class PostCssCompletionContributor extends CompletionContributor {

  public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull final CompletionResultSet result) {
    if (result.isStopped()) return;
    PsiElement position = parameters.getPosition();
    if (PostCssPsiUtil.isInsidePostCss(position) &&
        psiElement(CssElementTypes.CSS_IDENT).withReference(PostCssCustomSelectorReference.class).accepts(position)) {
      Project project = position.getProject();
      GlobalSearchScope scope = CssUtil.getCompletionAndResolvingScopeForElement(position);
      for (String name : StubIndex.getInstance().getAllKeys(PostCssCustomSelectorIndex.KEY, project)) {
        if (name.isEmpty()) continue;
        Collection<PostCssCustomSelector> customSelectors = StubIndex.getElements(PostCssCustomSelectorIndex.KEY, name, project, scope,
                                                                                  PostCssCustomSelector.class);
        for (PostCssCustomSelector customSelector : customSelectors) {
          result.addElement(createCustomSelectorLookup(customSelector, position, parameters.getOriginalFile()));
        }
      }
    }
  }

  @NotNull
  private static LookupElement createCustomSelectorLookup(@NotNull final PostCssCustomSelector customSelector,
                                                          @NotNull final PsiElement position,
                                                          @Nullable final PsiFile contextFile) {
    //TODO use com.intellij.psi.css.impl.util.completion.CssCompletionUtil#CSS_PSEUDO_SELECTOR_PRIORITY instead when PostCSS module will be part of API
    boolean increasePriority =
      CssUtil.getImportedFiles(contextFile, position, false).contains(customSelector.getContainingFile().getVirtualFile());
    int priority = 10 + (increasePriority ? 1 : 0);

    ItemPresentation itemPresentation = ObjectUtils.notNull(customSelector.getPresentation());
    return PrioritizedLookupElement.withPriority(
      LookupElementBuilder.createWithSmartPointer("--" + customSelector.getName(), customSelector)
        .withPresentableText(ObjectUtils.notNull(itemPresentation.getPresentableText()))
        .withIcon(itemPresentation.getIcon(false))
        .withTypeText(itemPresentation.getLocationString(), true), priority);
  }
}