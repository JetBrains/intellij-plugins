package org.osmorc.manifest.lang.headerparser.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PackageReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiPackageReference;
import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.lang.headerparser.HeaderParser;
import org.osmorc.manifest.lang.psi.Clause;
import org.osmorc.manifest.lang.psi.HeaderValuePart;

/**
 * @author Vladislav.Soroka
 */
public class BasePackageParser extends AbstractHeaderParser {
  public static final HeaderParser INSTANCE = new BasePackageParser();

  protected static PsiReference[] getPackageReferences(final PsiElement psiElement, final String unwrappedText) {
    String unwrappedPackage = unwrappedText;
    if (unwrappedPackage.isEmpty()) {
      return PsiReference.EMPTY_ARRAY;
    }

    int offset = psiElement.getText().indexOf(unwrappedPackage);
    offset = offset == -1 ? 0 : offset;
    if (unwrappedPackage.charAt(0) == '!') {
      unwrappedPackage = unwrappedPackage.substring(1);
      offset++;
    }

    int size = unwrappedPackage.length() - 1;
    if (unwrappedPackage.charAt(size) == '?') {
      unwrappedPackage = unwrappedPackage.substring(0, size);
    }
    PackageReferenceSet referenceSet = new PackageReferenceSet(unwrappedPackage, psiElement, offset);
    return referenceSet.getReferences().toArray(new PsiPackageReference[referenceSet.getReferences().size()]);
  }

  @Override
  public PsiReference[] getReferences(@NotNull HeaderValuePart headerValuePart) {
    if (headerValuePart.getParent() instanceof Clause) {
      return getPackageReferences(headerValuePart, headerValuePart.getUnwrappedText());
    }
    return PsiReference.EMPTY_ARRAY;
  }

  @Override
  public boolean isSimpleHeader() {
    return false;
  }
}
