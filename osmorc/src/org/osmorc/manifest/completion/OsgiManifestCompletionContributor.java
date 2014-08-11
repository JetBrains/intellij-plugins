package org.osmorc.manifest.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiElement;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.ManifestTokenType;
import org.osgi.framework.Constants;
import org.osmorc.manifest.lang.psi.Directive;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * @author Vladislav.Soroka
 */
public class OsgiManifestCompletionContributor extends CompletionContributor {
  public OsgiManifestCompletionContributor() {
    extend(
      CompletionType.BASIC,
      header(Constants.EXPORT_PACKAGE),
      new HeaderParametersProvider(Constants.VERSION_ATTRIBUTE, Constants.USES_DIRECTIVE+':'));

    extend(
      CompletionType.BASIC,
      header(Constants.IMPORT_PACKAGE),
      new HeaderParametersProvider(Constants.VERSION_ATTRIBUTE, Constants.RESOLUTION_DIRECTIVE+':'));

    extend(
      CompletionType.BASIC,
      directive(Constants.RESOLUTION_DIRECTIVE),
      new SimpleProvider("mandatory", "optional"));
  }

  private static ElementPattern<PsiElement> header(String name) {
    return psiElement(ManifestTokenType.HEADER_VALUE_PART)
      .afterLeaf(";")
      .withSuperParent(3, psiElement(Header.class).withName(name));
  }

  private static ElementPattern<PsiElement> directive(String name) {
    return psiElement(ManifestTokenType.HEADER_VALUE_PART)
      .withSuperParent(2, psiElement(Directive.class).withName(name));
  }
}
