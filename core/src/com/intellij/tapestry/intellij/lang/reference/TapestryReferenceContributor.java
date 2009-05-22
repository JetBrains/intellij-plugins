package com.intellij.tapestry.intellij.lang.reference;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.*;
import com.intellij.psi.*;
import com.intellij.psi.xml.*;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.util.ComponentUtils;
import com.intellij.tapestry.intellij.core.resource.xml.IntellijXmlTag;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class TapestryReferenceContributor extends PsiReferenceContributor {

  public void registerReferenceProviders(final PsiReferenceRegistrar registrar) {
    registerTagNameReferenceProvider(registrar);
  }

  private void registerTagNameReferenceProvider(PsiReferenceRegistrar registrar) {
    PatternCondition<XmlTag> tapestryTagCondition = new PatternCondition<XmlTag>("tapestryTagCondition") {
      public boolean accepts(@NotNull XmlTag tag, final ProcessingContext context) {
        final VirtualFile vFile = tag.getContainingFile().getVirtualFile();
        return (vFile == null || TapestryConstants.TEMPLATE_FILE_EXTENSION.equals(vFile.getExtension())) 
               && ComponentUtils.isComponentTag(new IntellijXmlTag(tag));
      }
    };
    ElementPattern tagNamePattern = PlatformPatterns.psiElement(XmlTokenType.XML_NAME).withParent(XmlPatterns.xmlTag().with(tapestryTagCondition));
    registrar.registerReferenceProvider(tagNamePattern, new PsiReferenceProvider() {
      @NotNull
      public PsiReference[] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
        return PsiReference.EMPTY_ARRAY;
      }
    });
  }

}
