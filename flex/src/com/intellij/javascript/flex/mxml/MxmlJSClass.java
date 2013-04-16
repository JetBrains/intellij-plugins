package com.intellij.javascript.flex.mxml;

import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSReferenceList;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * @author yole
 */
public class MxmlJSClass extends XmlBackedJSClassImpl {
  @NonNls public static final String XML_TAG_NAME = "XML";
  @NonNls public static final String XMLLIST_TAG_NAME = "XMLList";
  @NonNls public static final String PRIVATE_TAG_NAME = MxmlLanguageInjector.PRIVATE_TAG_NAME;
  private static final String OPERATION_TAG_NAME = "operation";
  private static final String HTTP_SERVICE_TAG_NAME = "HTTPService";
  private static final String WEB_SERVICE_TAG_NAME = "WebService";
  private static final String[] REQUEST_TAG_POSSIBLE_NAMESPACES =
    {JavaScriptSupportLoader.MXML_URI, JavaScriptSupportLoader.MXML_URI4, JavaScriptSupportLoader.MXML_URI6};
  private static final String REQUEST_TAG_NAME = "request";
  private static final String[] TAGS_THAT_ALLOW_ANY_XML_CONTENT = {PRIVATE_TAG_NAME, XML_TAG_NAME, XMLLIST_TAG_NAME,
    FlexPredefinedTagNames.MODEL};
  @NonNls private static final String FXG_SUPER_CLASS = "spark.core.SpriteVisualElement";

  private final boolean isFxgBackedClass;

  public MxmlJSClass(XmlTag tag) {
    super(tag);
    final PsiFile psiFile = tag.getContainingFile();
    isFxgBackedClass = psiFile != null && JavaScriptSupportLoader.isFxgFile(psiFile);
  }

  public static XmlTag[] findLanguageSubTags(final XmlTag tag, final String languageTagName) {
    return tag.findSubTags(languageTagName, getLanguageNamespace(tag));
  }

  @Override
  protected String getSuperClassName() {
    if (isFxgBackedClass) {
      return FXG_SUPER_CLASS;
    }
    return super.getSuperClassName();
  }

  @Nullable
  @Override
  public JSReferenceList getImplementsList() {
    if (isFxgBackedClass) {
      return null;
    }
    return super.getImplementsList();
  }

  public static boolean isFxLibraryTag(final XmlTag tag) {
    return tag != null && FlexPredefinedTagNames.LIBRARY.equals(tag.getLocalName()) && JavaScriptSupportLoader.MXML_URI3.equals(tag.getNamespace());
  }

  @Override
  protected boolean canResolveTo(XmlElement element) {
    final XmlTag tag = PsiTreeUtil.getParentOfType(element, XmlTag.class, false);
    if (tag == null) {
      return true;
    }
    // Component tag itself can be referenced by id
    if (!isComponentTag(tag) && getContainingComponent(element) != this) {
      return false;
    }
    return canBeReferencedById(tag);
  }

  public static boolean canBeReferencedById(XmlTag tag) {
    return !isInsideTagThatAllowsAnyXmlContent(tag) && !isOperationTag(tag);
  }

  public static boolean isTagOrInsideTagThatAllowsAnyXmlContent(final XmlTag tag) {
    return isTagThatAllowsAnyXmlContent(tag) || isInsideTagThatAllowsAnyXmlContent(tag);
  }

  public static boolean isInsideTagThatAllowsAnyXmlContent(final XmlTag tag) {
    return isInsideTag(tag, new Condition<XmlTag>() {
      public boolean value(final XmlTag parentTag) {
        return isTagThatAllowsAnyXmlContent(parentTag);
      }
    });
  }

  public static boolean isTagThatAllowsAnyXmlContent(final XmlTag tag) {
    return tag != null &&
           (JavaScriptSupportLoader.isLanguageNamespace(tag.getNamespace()) &&
            ArrayUtil.contains(tag.getLocalName(), TAGS_THAT_ALLOW_ANY_XML_CONTENT) || isWebOrHttpServiceRequestTag(tag));
  }

  private static boolean isOperationTag(final XmlTag tag) {
    return OPERATION_TAG_NAME.equals(tag.getLocalName()) && ArrayUtil.contains(tag.getNamespace(), REQUEST_TAG_POSSIBLE_NAMESPACES);
  }

  private static boolean isWebOrHttpServiceRequestTag(final XmlTag tag) {
    if (ArrayUtil.contains(tag.getNamespace(), REQUEST_TAG_POSSIBLE_NAMESPACES) && REQUEST_TAG_NAME.equals(tag.getLocalName())) {
      final XmlTag parentTag = tag.getParentTag();
      if (parentTag != null && ArrayUtil.contains(parentTag.getNamespace(), REQUEST_TAG_POSSIBLE_NAMESPACES)) {
        if (HTTP_SERVICE_TAG_NAME.equals(parentTag.getLocalName())) {
          return true;
        }
        else if (OPERATION_TAG_NAME.equals(parentTag.getLocalName())) {
          final XmlTag parentParentTag = parentTag.getParentTag();
          if (parentParentTag != null &&
              ArrayUtil.contains(parentParentTag.getNamespace(), REQUEST_TAG_POSSIBLE_NAMESPACES) &&
              WEB_SERVICE_TAG_NAME.equals(parentParentTag.getLocalName())) {
            return true;
          }
        }
      }
    }
    return false;
  }



}
