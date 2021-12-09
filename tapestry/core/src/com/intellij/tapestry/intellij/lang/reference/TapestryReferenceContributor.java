package com.intellij.tapestry.intellij.lang.reference;

import com.intellij.lang.properties.references.PropertyReference;
import com.intellij.openapi.paths.PathReferenceManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.java.IJavaMethod;
import com.intellij.tapestry.core.model.presentation.TapestryComponent;
import com.intellij.tapestry.core.model.presentation.Page;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ResolvedValue;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverChain;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaField;
import com.intellij.tapestry.intellij.core.java.IntellijJavaMethod;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.lang.descriptor.TapestryXmlExtension;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.tapestry.psi.TmlFile;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TapestryReferenceContributor extends PsiReferenceContributor {
  private static class Holder {
    private static final PatternCondition<XmlElement> tapestryFileCondition = new PatternCondition<>("tapestryFileCondition") {
      @Override
      public boolean accepts(@NotNull XmlElement element, final ProcessingContext context) {
        return element.getContainingFile() instanceof TmlFile;
      }
    };
  }
  private static final Key<XmlTag> TAG_KEY = Key.create("TAG_KEY");

  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
    final String[] tapestryTemplateNamespaces = TapestryXmlExtension.tapestryTemplateNamespaces();
    registerTypeAttrValueReferenceProvider(registrar, tapestryTemplateNamespaces);
    registerIdAttrValueReferenceProvider(registrar, tapestryTemplateNamespaces);
    registerAttrValueReferenceProvider(registrar);
    registerLinkHrefReference(registrar);
  }

  private static void registerLinkHrefReference(@NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(
      XmlPatterns.xmlAttributeValue("href").inside(XmlPatterns.xmlTag().withName("link")).with(Holder.tapestryFileCondition),
      new PsiReferenceProvider() {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
          return PathReferenceManager.getInstance().createReferences(element, true, false, true);
        }
      });
  }

  private static void registerTypeAttrValueReferenceProvider(PsiReferenceRegistrar registrar, String[] tapestryTemplateNamespaces) {
    registrar.registerReferenceProvider(
      XmlPatterns.xmlAttributeValue("type").withNamespace(tapestryTemplateNamespaces).with(Holder.tapestryFileCondition),
      new PsiReferenceProvider() {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
          XmlAttributeValue typeAttrValue = (XmlAttributeValue)element;
          return getReferenceToComponentClass(typeAttrValue, ElementManipulators.getValueTextRange(typeAttrValue));
        }
      });

    registrar.registerReferenceProvider(
      XmlPatterns.xmlAttributeValue("alt").with(Holder.tapestryFileCondition),
      new PsiReferenceProvider() {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
          final String value = StringUtil.stripQuotesAroundValue(element.getText());
          final String prefix = "message:";
          if (value.startsWith(prefix)) {
            final String key = value.substring(prefix.length());
            final int valueStart = prefix.length() + 1;
            return new PsiReference[] {
              new PropertyReference(key, element, null, true, new TextRange(valueStart, valueStart + key.length()))
            };
          }
          return PsiReference.EMPTY_ARRAY;
        }
      });
  }

  private static void registerIdAttrValueReferenceProvider(PsiReferenceRegistrar registrar, String[] tapestryTemplateNamespaces) {
    registrar.registerReferenceProvider(
        XmlPatterns.xmlAttributeValue("id").withNamespace(tapestryTemplateNamespaces).with(Holder.tapestryFileCondition),
        new PsiReferenceProvider() {
          @Override
          public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
            XmlAttributeValue idAttrValue = (XmlAttributeValue)element;
            XmlElement identifier = TapestryUtils.getComponentIdentifier(parentTag(idAttrValue));
            final TextRange valueTextRange = ElementManipulators.getValueTextRange(idAttrValue);
            return identifier == idAttrValue.getParent()
                   ? getReferenceToEmbeddedComponent(idAttrValue, valueTextRange)
                   : getReferenceByComponentId(idAttrValue, valueTextRange);
          }
        });
  }

  private static XmlTag parentTag(XmlAttributeValue value) {
    final PsiElement parent = value.getParent();
    if (parent instanceof XmlAttribute) return ((XmlAttribute) parent).getParent();
    return null;
  }

  private static void registerAttrValueReferenceProvider(PsiReferenceRegistrar registrar) {
    final PatternCondition<XmlTag> tapestryTagCondition = new PatternCondition<>("tapestryTagCondition") {
      @Override
      public boolean accepts(@NotNull XmlTag tag, final ProcessingContext context) {
        return tag.getContainingFile() instanceof TmlFile && TapestryUtils.getTypeOfTag(tag) != null;
      }
    };

    final XmlAttributeValuePattern tapestryAttributeValuePattern =
       XmlPatterns.xmlAttributeValue().withSuperParent(2, XmlPatterns.xmlTag().with(tapestryTagCondition).save(TAG_KEY));
    registrar.registerReferenceProvider(tapestryAttributeValuePattern, new PsiReferenceProvider() {
      @Override
      public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
        XmlAttributeValue attrValue = (XmlAttributeValue)element;
        XmlTag tag = context.get(TAG_KEY);

        TapestryComponent component = TapestryUtils.getTypeOfTag(tag);
        if (component == null) return PsiReference.EMPTY_ARRAY;
        final PsiElement parent = attrValue.getParent();
        if (!(parent instanceof XmlAttribute)) return PsiReference.EMPTY_ARRAY;

        final String localName = ((XmlAttribute)parent).getLocalName();
        TapestryParameter parameter = component.getParameters().get(localName);
        if (parameter == null) return PsiReference.EMPTY_ARRAY;

        return localName.equals("page")
               ? getReferenceToPage(component, attrValue)
               : getAttrValueReference(attrValue, component.getProject(), parameter);
      }
    });
  }

  private static PsiReference[] getAttrValueReference(XmlAttributeValue attrValue, TapestryProject project, TapestryParameter parameter) {
    final PresentationLibraryElement element = project.findElementByTemplate(attrValue.getContainingFile());
    if (element == null) return PsiReference.EMPTY_ARRAY;
    IntellijJavaClassType elementClass = (IntellijJavaClassType)element.getElementClass();

    ResolvedValue resolvedValue;
    try {
      resolvedValue = ValueResolverChain.getInstance().resolve(project, elementClass, attrValue.getValue(), parameter.getDefaultPrefix());
    }
    catch (Exception ex) {
      return PsiReference.EMPTY_ARRAY;
    }

    if (resolvedValue == null) return PsiReference.EMPTY_ARRAY;

    if (resolvedValue.getCodeBind() instanceof IJavaMethod) {
      return new PsiReference[]{
          new PsiAttributeValueReference(attrValue, ((IntellijJavaMethod)resolvedValue.getCodeBind()).getPsiMethod())};
    }

    if (resolvedValue.getCodeBind() instanceof IJavaField) {
      return new PsiReference[]{
          new PsiAttributeValueReference(attrValue, ((IntellijJavaField)resolvedValue.getCodeBind()).getPsiField())};
    }
    return PsiReference.EMPTY_ARRAY;
  }

  private static PsiReference @NotNull [] getReferenceToComponentClass(@NotNull XmlAttributeValue attributeValue, @Nullable TextRange range) {
    if (range == null) return PsiReference.EMPTY_ARRAY;
    final XmlTag tag = parentTag(attributeValue);
    if (tag == null) return PsiReference.EMPTY_ARRAY;
    TapestryComponent component = TapestryUtils.getTypeOfTag(tag);
    final IntellijJavaClassType elementClass = component == null ? null : (IntellijJavaClassType)component.getElementClass();

    return new PsiReference[]{new PsiReferenceBase<PsiElement>(attributeValue, range) {
      @Override
      @Nullable
      public PsiElement resolve() {
        return elementClass == null ? null : elementClass.getPsiClass();
      }

      @Override
      public Object @NotNull [] getVariants() {
        TapestryProject project = TapestryModuleSupportLoader.getTapestryProject(tag);
        return project == null ? EMPTY_ARRAY : project.getAvailableComponentNames();
      }
    }};
  }

  private static PsiReference @NotNull [] getReferenceToEmbeddedComponent(@NotNull XmlAttributeValue attr, TextRange range) {
    if (range == null) return PsiReference.EMPTY_ARRAY;
    final XmlTag tag = parentTag(attr);
    final IntellijJavaField field = (IntellijJavaField)TapestryUtils.findIdentifyingField(tag);

    return new PsiReference[]{new PsiReferenceBase<PsiElement>(attr, range) {
      @Override
      @Nullable
      public PsiElement resolve() {
        return field == null ? null : field.getPsiField();
      }

      @Override
      public Object @NotNull [] getVariants() {
        List<String> fieldsIds = TapestryUtils.getEmbeddedComponentIds(tag);
        return ArrayUtilRt.toStringArray(fieldsIds);
      }
    }};
  }

  private static PsiReference @NotNull [] getReferenceByComponentId(@NotNull final XmlAttributeValue attrValue, TextRange range) {
    if (range == null) return PsiReference.EMPTY_ARRAY;

    return new PsiReference[]{new PsiReferenceBase<PsiElement>(attrValue, range) {
      @Override
      @Nullable
      public PsiElement resolve() {
        return attrValue;
      }
    }};
  }

  private static PsiReference @NotNull [] getReferenceToPage(final TapestryComponent component, @NotNull XmlAttributeValue pageAttrValue) {
    final TextRange range = ElementManipulators.getValueTextRange(pageAttrValue);
    if (range == null) return PsiReference.EMPTY_ARRAY;

    final Page page = component.getProject().findPage(pageAttrValue.getValue());

    return new PsiReference[]{new PsiReferenceBase<PsiElement>(pageAttrValue, range) {
      @Override
      @Nullable
      public PsiElement resolve() {
        if (page == null) return null;
        IResource[] templates = page.getTemplate();
        return templates.length == 0 ? null : ((IntellijResource)templates[0]).getPsiFile();
      }

      @Override
      public Object @NotNull [] getVariants() {
        return component.getProject().getAvailablePageNames();
      }
    }};
  }

}
