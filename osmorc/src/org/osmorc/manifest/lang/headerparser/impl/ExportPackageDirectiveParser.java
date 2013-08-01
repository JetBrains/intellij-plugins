package org.osmorc.manifest.lang.headerparser.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.BasicAttributeValueReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osmorc.manifest.lang.ManifestTokenType;
import org.osmorc.manifest.lang.psi.ExportReference;
import org.osmorc.manifest.lang.psi.HeaderValuePart;

import static org.osmorc.manifest.lang.headerparser.impl.AbstractHeaderParserImpl.EMPTY_PSI_REFERENCE_ARRAY;

/**
 * Created with IntelliJ IDEA.
 * User: Vladislav.Soroka
 */
public class ExportPackageDirectiveParser {

  public PsiReference[] getReferences(@NotNull HeaderValuePart headerValuePart) {
    final ASTNode node = headerValuePart.getNode();
    final ASTNode prevNode = node.getTreePrev();
    if (prevNode != null && prevNode.getElementType() == ManifestTokenType.EQUALS) {
      String attrName = prevNode.getTreePrev().getTreePrev().getText();
      if (ExportReference.NO_IMPORT.equals(attrName)) {
        return new PsiReference[]{new NoImportDirectiveValueReference(headerValuePart)};
      }
      else if (ExportReference.SPLIT_PACKAGE.equals(attrName)) {
        return new PsiReference[]{new SplitPackageDirectiveValueReference(headerValuePart)};
      }
    }
    else {
      PsiReference reference = new ExportReference(headerValuePart, 0);
      return new PsiReference[]{reference};
    }
    return EMPTY_PSI_REFERENCE_ARRAY;
  }

  private static class NoImportDirectiveValueReference extends BasicAttributeValueReference {
    public NoImportDirectiveValueReference(HeaderValuePart headerValuePart) {
      super(headerValuePart, 0);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
      return myElement;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      return new Object[]{"true", "false"};
    }

    @Override
    public boolean isSoft() {
      return true;
    }
  }

  private static class SplitPackageDirectiveValueReference extends BasicAttributeValueReference {
    public SplitPackageDirectiveValueReference(HeaderValuePart headerValuePart) {
      super(headerValuePart, 0);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
      return myElement;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      return new Object[]{"merge-first", "merge-last", "first", "error"};
    }

    @Override
    public boolean isSoft() {
      return true;
    }
  }
}
