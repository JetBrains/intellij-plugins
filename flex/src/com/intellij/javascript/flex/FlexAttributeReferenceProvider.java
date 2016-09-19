package com.intellij.javascript.flex;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInsight.daemon.XmlErrorMessages;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixProvider;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageNamesValidation;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.ReferenceSupport;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeImpl;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSAttributeNameValuePairImpl;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.impl.JSReferenceSet;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceFix;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class FlexAttributeReferenceProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    JSAttributeNameValuePairImpl nameValuePair = (JSAttributeNameValuePairImpl) element;
    final @NonNls String name = nameValuePair.getName();
    return valueRefs(nameValuePair, name);
  }

  @NonNls private static final String BUNDLE_ATTR_NAME = "bundle";
  private static final FlexPropertiesSupport.PropertyReferenceInfoProvider<JSAttributeNameValuePairImpl> ourPropertyInfoProvider =
    new FlexPropertiesSupport.PropertyReferenceInfoProvider<JSAttributeNameValuePairImpl>() {
      @Nullable
      public TextRange getReferenceRange(JSAttributeNameValuePairImpl element) {
        return getValueRange(element);
      }

      public String getBundleName(JSAttributeNameValuePairImpl element) {
        JSAttributeNameValuePair pair = ((JSAttribute)element.getParent()).getValueByName(BUNDLE_ATTR_NAME);
        return pair != null ? pair.getSimpleValue() : null;
      }

      public boolean isSoft(JSAttributeNameValuePairImpl element) {
        return false;
      }
    };
  private static final Key<JSReferenceSet> METADATA_REFERENCE_KEY = Key.create("com.intellij.lang.javascript.METADATA_REFERENCE_KEY");
  private static final FlexPropertiesSupport.BundleReferenceInfoProvider<JSAttributeNameValuePairImpl> ourBundleInfoProvider =
    new FlexPropertiesSupport.BundleReferenceInfoProvider<JSAttributeNameValuePairImpl>() {
      public TextRange getReferenceRange(JSAttributeNameValuePairImpl element) {
        return getValueRange(element);
      }

      public boolean isSoft(JSAttributeNameValuePairImpl element) {
        return false;
      }
    };

  public static PsiReference[] valueRefs(JSAttributeNameValuePairImpl element, String name) {
      final JSAttributeImpl jsAttribute = (JSAttributeImpl)element.getParent();
      final XmlElementDescriptor descriptor = jsAttribute.getBackedDescriptor();
      final XmlAttributeDescriptor attributeDescriptor =
        descriptor == null ? null : descriptor.getAttributeDescriptor(StringUtil.notNullize(name, JSAttributeNameValuePair.DEFAULT), null);

      if (name == null) {
        return getDefaultPropertyRefs(element, attributeDescriptor);
      }

      final String baseClassFqns = attributeDescriptor == null ? null : attributeDescriptor.getDefaultValue();
      if (baseClassFqns != null) {
        return getClassRefs(element, baseClassFqns);
      }
      else if ("source".equals(name)) {
        return getPathRefsCheckingParent(element);
      }
      else if ("key".equals(name)) {
        return getPropertyRefsCheckingParent(element);
      }
      else if (BUNDLE_ATTR_NAME.equals(name)) {
        return getBundleRefsCheckingParent(element);
      }
      else if (attributeDescriptor != null && attributeDescriptor.isEnumerated()) {
        final String[] enumeratedValues = attributeDescriptor.getEnumeratedValues();
        final TextRange range = getValueRange(element);
        if (enumeratedValues != null && enumeratedValues.length > 0 && range != null) {
          return new PsiReference[]{new EnumeratedAttributeValueReference(element, range, enumeratedValues)};
        }
      }

      return PsiReference.EMPTY_ARRAY;
    }

  private static PsiReference[] getClassRefs(@NotNull final JSAttributeNameValuePairImpl element, final @NotNull String baseClassFqns) {
    final ASTNode valueNode = element.findValueNode();

    if (valueNode != null) {
      final int offsetInParent = valueNode.getPsi().getStartOffsetInParent();
      final String text = valueNode.getText();
      JSReferenceSet referenceSet = element.getUserData(METADATA_REFERENCE_KEY);
      if (referenceSet == null) {
        referenceSet = new JSReferenceSet(element, "", offsetInParent, false, true);
        element.putUserData(METADATA_REFERENCE_KEY, referenceSet);

        referenceSet.setLocalQuickFixProvider(new ClassRefQuickFixProvider(element, referenceSet));
      }

      if (!"Object".equals(baseClassFqns)) {
       referenceSet.setBaseClassFqns(StringUtil.split(baseClassFqns, ","));
      }
      referenceSet.update(text, offsetInParent);
      return referenceSet.getReferences();
    }
    return PsiReference.EMPTY_ARRAY;
  }

  private static PsiReference[] getDefaultPropertyRefs(final JSAttributeNameValuePairImpl element, final XmlAttributeDescriptor attributeDescriptor) {
    final String baseClassFqn = attributeDescriptor == null ? null : attributeDescriptor.getDefaultValue();
    if (baseClassFqn != null) {
      return getClassRefs(element, baseClassFqn);
    }

    final @NonNls String parentName = ((JSAttribute)element.getParent()).getName();
    if ("ResourceBundle".equals(parentName)) {
      return FlexPropertiesSupport.getResourceBundleReference(element, ourBundleInfoProvider);
    }

    if (FlexAnnotationNames.EMBED.equals(parentName)) return getPathRefs(element);
    if ("DefaultProperty".equals(parentName)) {
      final ASTNode valueNode = element.findValueNode();
      if (valueNode != null) {
        JSReferenceSet referenceSet = element.getUserData(METADATA_REFERENCE_KEY);
        if (referenceSet == null) {
          referenceSet = new JSReferenceSet(element, false);
          element.putUserData(METADATA_REFERENCE_KEY, referenceSet);
        }
        referenceSet.update(valueNode.getText(), valueNode.getPsi().getStartOffsetInParent());
        return referenceSet.getReferences();
      }
    }

    return PsiReference.EMPTY_ARRAY;
  }

  private static PsiReference[] getBundleRefsCheckingParent(JSAttributeNameValuePairImpl element) {
    JSAttribute attribute = (JSAttribute)element.getParent();
    final @NonNls String parentName = attribute.getName();

    if (!FlexAnnotationNames.RESOURCE.equals(parentName)) return PsiReference.EMPTY_ARRAY;
    return FlexPropertiesSupport.getResourceBundleReference(element, ourBundleInfoProvider);
  }

  private static PsiReference[] getPropertyRefsCheckingParent(JSAttributeNameValuePairImpl element) {
    JSAttribute attribute = (JSAttribute)element.getParent();
    final @NonNls String parentName = attribute.getName();

    if (!FlexAnnotationNames.RESOURCE.equals(parentName)) return PsiReference.EMPTY_ARRAY;
    return FlexPropertiesSupport.getPropertyReferences(element, ourPropertyInfoProvider);
  }

  private static PsiReference[] getPathRefsCheckingParent(JSAttributeNameValuePairImpl element) {
    final @NonNls String parentName = ((JSAttribute)element.getParent()).getName();

    if (!FlexAnnotationNames.EMBED.equals(parentName)) return PsiReference.EMPTY_ARRAY;
    return getPathRefs(element);
  }

  private static PsiReference[] getPathRefs(JSAttributeNameValuePairImpl element) {
    final ASTNode valueNode = element.findValueNode();

    if (valueNode != null && StringUtil.isQuotedString(valueNode.getText())) {
      return ReferenceSupport.getFileRefs(element, valueNode.getPsi(), valueNode.getPsi().getStartOffsetInParent() + 1,
                                          ReferenceSupport.LookupOptions.EMBEDDED_ASSET);
    }
    return PsiReference.EMPTY_ARRAY;
  }

  @Nullable
  private static TextRange getValueRange(JSAttributeNameValuePairImpl element) {
    ASTNode valueNode = element.findValueNode();
    if (valueNode == null) return null;
    int valueStart = valueNode.getPsi().getStartOffsetInParent();
    int length = valueNode.getTextLength();
    return StringUtil.isQuotedString(valueNode.getText())
           ? new TextRange(valueStart + 1, valueStart + length - 1) : new TextRange(valueStart, valueStart + length);
  }

  private static class ClassRefQuickFixProvider implements LocalQuickFixProvider {
    private final JSAttributeNameValuePairImpl myElement;
    private final JSReferenceSet myReferenceSet;

    public ClassRefQuickFixProvider(JSAttributeNameValuePairImpl element, JSReferenceSet referenceSet) {
      myElement = element;
      myReferenceSet = referenceSet;
    }

    public LocalQuickFix[] getQuickFixes() {
      final String fqn = myElement.getSimpleValue();

      if (fqn != null && LanguageNamesValidation.INSTANCE.forLanguage(JavaScriptSupportLoader.JAVASCRIPT.getLanguage())
        .isIdentifier(StringUtil.getShortName(fqn), null)) {

        final String[] baseClasses = myReferenceSet.getBaseClassFqns();

        String baseClass = null;
        if (baseClasses.length == 1 && !"Object".equals(baseClasses[0])) {
          baseClass = baseClasses[0];
        }
        else if (baseClasses.length > 0) {
          //
          for (String aClass : baseClasses) {
            if (ActionScriptClassResolver.findClassByQNameStatic(aClass, myElement) != null) {
              if (baseClass == null) {
                baseClass = aClass;
              }
              else {
                // more than one class resolved, but CreateClassOrInterfaceFix accepts only one
                baseClass = null;
                break;
              }
            }
          }
        }

        final CreateClassOrInterfaceFix fix = new CreateClassOrInterfaceFix(fqn, baseClass, myElement);
        fix.setCreatedClassFqnConsumer(fqn1 -> {
          if (myElement.isValid()) {
            if (!fqn1.equals(StringUtil.stripQuotesAroundValue(myElement.getValueNode().getText()))) {
              final ASTNode oldValueNode = myElement.getValueNode();
              final String oldText = oldValueNode.getText();
              char quoteChar = oldText.length() > 0 ? oldText.charAt(0) : '"';
              if (quoteChar != '\'' && quoteChar != '"') {
                quoteChar = '"';
              }

              final ASTNode newNode = JSChangeUtil.createExpressionFromText(myElement.getProject(), quoteChar + fqn1 + quoteChar);
              myElement.getNode().replaceChild(oldValueNode, newNode.getFirstChildNode());
            }
          }
        });
        return new LocalQuickFix[]{fix};
      }

      return LocalQuickFix.EMPTY_ARRAY;
    }
  }

  private static class EnumeratedAttributeValueReference implements PsiReference, EmptyResolveMessageProvider {

    private final JSAttributeNameValuePairImpl myElement;
    private final String[] myAllowedValues;
    private final String myValue;
    private final TextRange myRange;
    private final boolean myResolveOk;

    public EnumeratedAttributeValueReference(final JSAttributeNameValuePairImpl element,
                                             final TextRange range,
                                             final String[] allowedValues) {
      myElement = element;
      myAllowedValues = allowedValues;
      myRange = range;
      final ASTNode valueNode = element.getValueNode();
      myValue = valueNode == null ? "" : StringUtil.stripQuotesAroundValue(valueNode.getText());
      myResolveOk = ArrayUtil.contains(myValue, allowedValues);
    }

    public PsiElement getElement() {
      return myElement;
    }

    public TextRange getRangeInElement() {
      return myRange;
    }

    public PsiElement resolve() {
      if (myResolveOk) {
        final XmlAttributeDescriptor attributeDescriptor =
          ((JSAttributeImpl)myElement.getParent()).getBackedDescriptor().getAttributeDescriptor(myElement.getName(), null);
        return attributeDescriptor == null ? null : attributeDescriptor.getDeclaration();
      }
      return null;
    }

    @NotNull
    public String getCanonicalText() {
      return myValue;
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
      throw new IncorrectOperationException();
    }

    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
      throw new IncorrectOperationException();
    }

    public boolean isReferenceTo(PsiElement element) {
      return false;
    }

    @NotNull
    public Object[] getVariants() {
      return myAllowedValues;
    }

    public boolean isSoft() {
      return false;
    }

    @NotNull
    public String getUnresolvedMessagePattern() {
      return XmlErrorMessages.message("wrong.value", "attribute");
    }
  }
}
