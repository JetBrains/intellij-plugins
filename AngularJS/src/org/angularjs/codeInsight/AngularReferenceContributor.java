package org.angularjs.codeInsight;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angularjs.codeInsight.attributes.AngularAttributeDescriptor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(XmlAttributeValue.class), new AngularReferenceProvider());
  }

  private static class AngularReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
      final PsiElement parent = element.getParent();
      final XmlAttributeDescriptor descriptor = parent instanceof XmlAttribute ? ((XmlAttribute)parent).getDescriptor() : null;
      return descriptor instanceof AngularAttributeDescriptor ? ((AngularAttributeDescriptor)descriptor).getReferences(element) :
             new PsiReference[0];
    }
  }
}
