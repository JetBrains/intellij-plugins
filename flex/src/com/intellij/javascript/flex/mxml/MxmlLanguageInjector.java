package com.intellij.javascript.flex.mxml;

import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.JSTargetedInjector;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.MxmlLanguage;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.injections.JSInXmlLanguagesInjector;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.meta.PsiMetaData;
import com.intellij.psi.xml.*;
import com.intellij.xml.XmlElementDescriptorWithCDataContent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class MxmlLanguageInjector implements MultiHostInjector, JSTargetedInjector {
  public static final String PRIVATE_TAG_NAME = "Private";
  private static final String FUNCTION_CALL_PREFIX = "(function (... _){}) (";
  private static final String FUNCTION_CALL_SUFFIX = ");";
  private static class Holder {
    private static final Language regexpLanguage = Language.findLanguageByID("RegExp");
    private static final Language cssLanguage = Language.findLanguageByID("CSS");
  }

  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement host) {
    if (!host.getContainingFile().getLanguage().isKindOf(MxmlLanguage.INSTANCE)) {
      return;
    }

    if (host instanceof XmlAttributeValue) {
      final PsiElement attribute = host.getParent();
      final PsiElement tag = attribute.getParent();

      if (attribute instanceof XmlAttribute && tag instanceof XmlTag) {
        if (isFxPrivateTag((XmlTag)tag) || isInsideFxPrivateTag((XmlTag)tag)) {
          return;
        }

        if (host.getTextLength() == 0) return;

        @NonNls String attrName = ((XmlAttribute)attribute).getName();

        if ("implements".equals(attrName)) {
          TextRange range = new TextRange(1, host.getTextLength() - 1);
          registrar.startInjecting(JavaScriptSupportLoader.ECMA_SCRIPT_L4)
            .addPlace("class Foo implements ", " {}", (PsiLanguageInjectionHost)host, range)
            .doneInjecting();
        }
        else if ("source".equals(attrName) &&
                 FlexPredefinedTagNames.BINDING.equals(((XmlTag)tag).getLocalName()) &&
                 JavaScriptSupportLoader.isLanguageNamespace(((XmlTag)tag).getNamespace()) &&
                 !host.textContains('{')) {
          TextRange range = new TextRange(1, host.getTextLength() - 1);
          registrar.startInjecting(JavaScriptSupportLoader.ECMA_SCRIPT_L4)
            .addPlace(FUNCTION_CALL_PREFIX, FUNCTION_CALL_SUFFIX, (PsiLanguageInjectionHost)host, range)
            .doneInjecting();
        }
        else if (attrName.equals("expression") &&
                 "RegExpValidator".equals(((XmlTag)tag).getLocalName()) &&
                 Holder.regexpLanguage != null
          ) {
          String hostText = host.getText();
          int startPos = hostText.indexOf('/');
          int endPos = hostText.lastIndexOf('/');

          if (startPos != -1) {
            if (endPos > startPos) {
              TextRange range = new TextRange(startPos + 1, endPos);
              registrar.startInjecting(Holder.regexpLanguage)
                .addPlace(null, null, (PsiLanguageInjectionHost)host, range)
                .doneInjecting();
            }
          }
          else {
            injectInMxmlFile(registrar, host, ((XmlAttribute)attribute).getDescriptor(), (XmlTag)tag);
          }
        }
        else {
          injectInMxmlFile(registrar, host, ((XmlAttribute)attribute).getDescriptor(), (XmlTag)tag);
        }
      }
    }
    else if (host instanceof XmlText) {
      final PsiElement _tag = host.getParent();
      if (_tag instanceof XmlTag tag) {

        if (isFxPrivateTag(tag) || isInsideFxPrivateTag(tag) || tag instanceof HtmlTag) {
          return;
        }

        final @NonNls String localName = tag.getLocalName();

        if ((XmlBackedJSClassImpl.SCRIPT_TAG_NAME.equals(localName) ||
             FlexPredefinedTagNames.METADATA.equals(localName)) &&
            tag.getAttributeValue("source") == null) {
          JSInXmlLanguagesInjector.injectToXmlText(registrar, host, JavaScriptSupportLoader.ECMA_SCRIPT_L4, null, null);
        }
        else if (FlexPredefinedTagNames.STYLE.equals(localName) && FlexUtils.isMxmlNs(tag.getNamespace()) && Holder.cssLanguage != null) {
          JSInXmlLanguagesInjector.injectToXmlText(registrar, host, Holder.cssLanguage, null, null);
        }
        else if (tag.getSubTags().length == 0) {
          injectInMxmlFile(registrar, host, tag.getDescriptor(), tag);
        }
      }
    }
    else if (host instanceof XmlComment) {
      final String text = host.getText();
      final String marker = "<!---";
      if (text.startsWith(marker)) {
        final String marker2 = "-->";
        int end = text.endsWith(marker2) ? host.getTextLength() - marker2.length() : host.getTextLength();
        //int nestedCommentStart = text.indexOf(marker, marker.length());
        //if (nestedCommentStart != -1) end = nestedCommentStart;
        if (end < marker.length()) return;
        TextRange range = new TextRange(marker.length(), end);
        registrar.startInjecting(JavaScriptSupportLoader.ECMA_SCRIPT_L4)
          .addPlace("/***", "*/", (PsiLanguageInjectionHost)host, range)
          .doneInjecting();
      }
    }
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlText.class, XmlAttributeValue.class, XmlComment.class);
  }

  public static boolean isFxPrivateTag(final XmlTag tag) {
    return tag != null && PRIVATE_TAG_NAME.equals(tag.getLocalName()) && JavaScriptSupportLoader.MXML_URI3.equals(tag.getNamespace());
  }

  public static boolean isInsideFxPrivateTag(final XmlTag tag) {
    if (tag == null) return false;

    XmlTag parent = tag;
    while ((parent = parent.getParentTag()) != null) {
      if (isFxPrivateTag(parent)) {
        return true;
      }
    }
    return false;
  }

  private static void injectInMxmlFile(final MultiHostRegistrar registrar,
                                       final PsiElement host,
                                       final PsiMetaData descriptor,
                                       XmlTag tag) {
    int offset = host instanceof XmlText ? 0 : 1;

    if (descriptor instanceof AnnotationBackedDescriptor &&
        ((XmlElementDescriptorWithCDataContent)descriptor).requiresCdataBracesInContext(tag)) {
      final int length = host.getTextLength();
      if (length < 2 * offset) return;
      String type = ((AnnotationBackedDescriptor)descriptor).getType();
      if (type == null) type = "*";
      @NonNls String prefix = "(function (event:" + type + ") {";
      @NonNls String suffix = "})(null);";

      if (host instanceof XmlText) {
        JSInXmlLanguagesInjector.injectToXmlText(registrar, host, JavaScriptSupportLoader.ECMA_SCRIPT_L4, prefix, suffix);
      }
      else {
        if (JSCommonTypeNames.FUNCTION_CLASS_NAMES.contains(type) && host.textContains('{')) {
          final String text = StringUtil.stripQuotesAroundValue(host.getText());
          if (text.startsWith("{") && text.endsWith("}")) {
            prefix = FUNCTION_CALL_PREFIX;
            suffix = FUNCTION_CALL_SUFFIX;
            offset++;
          }
        }

        TextRange range = new TextRange(offset, length - offset);

        registrar.startInjecting(JavaScriptSupportLoader.ECMA_SCRIPT_L4)
          .addPlace(prefix, (host instanceof XmlAttributeValue ? "\n" : "") + suffix, (PsiLanguageInjectionHost)host, range)
          .doneInjecting();
      }
    }
    else if (!(host instanceof XmlText) || !hasCDATA((XmlText)host)){
      final String text = StringUtil.stripQuotesAroundValue(host.getText());
      int openedBraces = 0;
      int start = -1;
      boolean addedSomething = false;
      boolean quoted = false;

      for (int i = 0; i < text.length(); ++i) {
        final char ch = text.charAt(i);

        if (quoted) {
          quoted = false;
          continue;
        }
        if (ch == '\\') {
          quoted = true;
        }
        else if (ch == '{') {
          if (openedBraces == 0) start = i + 1;
          openedBraces++;
        }
        else if (ch == '}') {
          openedBraces--;
          if (openedBraces == 0 && start != -1) {
            registrar.startInjecting(JavaScriptSupportLoader.ECMA_SCRIPT_L4)
              .addPlace(FUNCTION_CALL_PREFIX, FUNCTION_CALL_SUFFIX, (PsiLanguageInjectionHost)host,
                        new TextRange(offset + start, i + offset))
              .doneInjecting();
            addedSomething = true;
            start = -1;
          }
        }
      }

      if (!addedSomething) {
        final String trimmedText = text.trim();
        start = trimmedText.indexOf("@");

        if (start == 0 &&
            trimmedText.length() > 1 &&
            Character.isUpperCase(trimmedText.charAt(1))) { // @id can be reference to attribute
          offset += text.indexOf(trimmedText);
          registrar.startInjecting(JavaScriptSupportLoader.ECMA_SCRIPT_L4)
            .addPlace(null, null, (PsiLanguageInjectionHost)host, new TextRange(offset, trimmedText.length() + offset))
            .doneInjecting();
        }
      }
    }
  }

  private static boolean hasCDATA(final XmlText xmlText) {
    for (PsiElement element : xmlText.getChildren()) {
      final ASTNode node = element.getNode();
      if (node != null && node.getElementType() == XmlElementType.XML_CDATA) {
        return true;
      }
    }
    return false;
  }
}
