package org.osmorc.manifest.lang.headerparser.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.lang.ManifestTokenType;
import org.osmorc.manifest.lang.psi.ExportReference;
import org.osmorc.manifest.lang.psi.HeaderValuePart;
import org.osmorc.manifest.lang.psi.PackageReference;
import org.osmorc.manifest.lang.psi.PackageReferenceSet;

/**
 * Created with IntelliJ IDEA.
 * User: Vladislav.Soroka
 */
public class ExportPackageClauseParser {

  public PsiReference[] getReferences(@NotNull HeaderValuePart headerValuePart) {
    final ASTNode node = headerValuePart.getNode();
    final ASTNode prevNode = node.getTreePrev();
    if (prevNode != null && prevNode.getElementType() == ManifestTokenType.SEMICOLON) {
      return new PsiReference[]{new ExportReference(headerValuePart, 0)};
    }
    else {
      PackageReferenceSet referenceSet = new PackageReferenceSet(headerValuePart.getUnwrappedText(), headerValuePart, 0);
      return referenceSet.getReferences().toArray(new PackageReference[referenceSet.getReferences().size()]);
    }
  }
}
