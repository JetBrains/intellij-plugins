package org.osmorc.manifest.lang.header;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PackageReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiPackageReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.lang.manifest.header.HeaderParser;
import org.osmorc.manifest.lang.psi.Clause;
import org.jetbrains.lang.manifest.psi.HeaderValuePart;
import org.osmorc.manifest.resolve.reference.providers.ManifestPackageReferenceSet;

/**
 * @author Vladislav.Soroka
 */
public class BasePackageParser extends OsgiHeaderParser {
  public static final HeaderParser INSTANCE = new BasePackageParser();

  protected static PsiReference[] getPackageReferences(final PsiElement psiElement) {
    String packageName = psiElement.getText();
    if (StringUtil.isEmptyOrSpaces(packageName) ) {
      return PsiReference.EMPTY_ARRAY;
    }

    int offset = 0;
    if (packageName.charAt(0) == '!') {
      packageName = packageName.substring(1);
      offset++;
    }

    int size = packageName.length() - 1;
    if (packageName.charAt(size) == '?') {
      packageName = packageName.substring(0, size);
    }
    PackageReferenceSet referenceSet = new ManifestPackageReferenceSet(packageName, psiElement, offset);
    return referenceSet.getReferences().toArray(new PsiPackageReference[referenceSet.getReferences().size()]);
  }

  @NotNull
  @Override
  public PsiReference[] getReferences(@NotNull HeaderValuePart headerValuePart) {
    if (headerValuePart.getParent() instanceof Clause) {
      return getPackageReferences(headerValuePart);
    }
    return PsiReference.EMPTY_ARRAY;
  }
}
