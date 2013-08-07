package org.osmorc.manifest.lang.headerparser.impl;

import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PackageReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiPackageReference;
import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.lang.psi.Clause;
import org.osmorc.manifest.lang.psi.HeaderValuePart;

/**
 * Created with IntelliJ IDEA.
 * User: Vladislav.Soroka
 */
public class BasePackageParser extends AbstractHeaderParserImpl {

  public PsiReference[] getReferences(@NotNull HeaderValuePart headerValuePart) {
    if (headerValuePart.getParent() instanceof Clause) {
      PackageReferenceSet referenceSet = new PackageReferenceSet(headerValuePart.getUnwrappedText(), headerValuePart, 0);
      return referenceSet.getReferences().toArray(new PsiPackageReference[referenceSet.getReferences().size()]);
    }
    return EMPTY_PSI_REFERENCE_ARRAY;
  }

  public boolean isSimpleHeader() {
    return false;
  }
}