package org.osmorc.manifest.lang.headerparser.impl;

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

  private BasePackageParser() { }

  @Override
  public PsiReference[] getReferences(@NotNull HeaderValuePart headerValuePart) {
    if (headerValuePart.getParent() instanceof Clause) {
      PackageReferenceSet referenceSet = new PackageReferenceSet(headerValuePart.getUnwrappedText(), headerValuePart, 0);
      return referenceSet.getReferences().toArray(new PsiPackageReference[referenceSet.getReferences().size()]);
    }
    return PsiReference.EMPTY_ARRAY;
  }

  @Override
  public boolean isSimpleHeader() {
    return false;
  }
}
