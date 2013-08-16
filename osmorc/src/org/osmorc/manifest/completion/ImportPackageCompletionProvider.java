package org.osmorc.manifest.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.ManifestConstants;
import org.jetbrains.lang.manifest.psi.ManifestTokenType;
import org.osmorc.manifest.lang.psi.Attribute;
import org.osmorc.manifest.lang.psi.Directive;
import org.jetbrains.lang.manifest.psi.ManifestToken;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Vladislav.Soroka
 */
public class ImportPackageCompletionProvider extends CompletionProvider<CompletionParameters> {

  @NonNls
  private static final String[] directiveNames = new String[]{
    ManifestConstants.Directives.RESOLUTION,
    ManifestConstants.Attributes.VERSION};
  private final Set<LookupElement> lookupElements;

  public ImportPackageCompletionProvider() {
    lookupElements = ContainerUtil.map2Set(directiveNames, new Function<String, LookupElement>() {
      public LookupElementBuilder fun(final String argumentValue) {
        return LookupElementBuilder.create(argumentValue).withCaseSensitivity(false);
      }
    });
  }

  public void addCompletions(@NotNull CompletionParameters completionparameters,
                             ProcessingContext processingcontext,
                             @NotNull CompletionResultSet completionresultset) {
    PsiElement psiElement = completionparameters.getOriginalPosition();
    PsiElement parent = null;
    if (psiElement != null) {
      parent = psiElement.getParent();
    }
    if (parent != null && psiElement.getPrevSibling() == null && isAttributeOrDirectivePart(parent)) {
      completionresultset.addAllElements(lookupElements);
    }
  }

  private static boolean isAttributeOrDirectivePart(final PsiElement element) {
    final boolean result;
    PsiElement prev = element.getPrevSibling();
    if (prev instanceof ManifestToken) {
      ManifestToken manifestToken = (ManifestToken)prev;
      result = manifestToken.getTokenType() == ManifestTokenType.SEMICOLON;
    }
    else if (element.getParent() instanceof Attribute || element.getParent() instanceof Directive) {
      result = true;
    }
    else {
      result = false;
    }

    return result;
  }
}