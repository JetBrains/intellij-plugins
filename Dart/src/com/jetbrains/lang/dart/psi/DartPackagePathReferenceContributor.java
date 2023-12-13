package com.jetbrains.lang.dart.psi;

import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public final class DartPackagePathReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    DartPackagePathReferenceProvider provider = new DartPackagePathReferenceProvider();
    String[] htmlAttrs = new String[]{"href", "src"};
    ElementFilter htmlFilter = DartPackagePathReferenceProvider.getFilter();
    XmlUtil
      .registerXmlAttributeValueReferenceProvider(registrar, htmlAttrs, htmlFilter, false, provider, PsiReferenceRegistrar.HIGHER_PRIORITY);
  }
}
