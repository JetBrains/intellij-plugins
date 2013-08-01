package org.osmorc.manifest.lang.headerparser.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.lang.ManifestTokenType;
import org.osmorc.manifest.lang.psi.ExportReference;
import org.osmorc.manifest.lang.psi.HeaderValuePart;

import static org.osmorc.manifest.lang.headerparser.impl.AbstractHeaderParserImpl.EMPTY_PSI_REFERENCE_ARRAY;

/**
 * Created with IntelliJ IDEA.
 * User: Vladislav.Soroka
 */
public class ExportPackageAttributeParser {

  public PsiReference[] getReferences(@NotNull HeaderValuePart headerValuePart) {
    final ASTNode node = headerValuePart.getNode();
    final ASTNode prevNode = node.getTreePrev();
    if (prevNode != null && prevNode.getElementType() == ManifestTokenType.EQUALS) {
      return EMPTY_PSI_REFERENCE_ARRAY;
    }
    else {
      PsiReference reference = new ExportReference(headerValuePart, 0);
      return new PsiReference[]{reference};
    }
  }
}
