package com.intellij.flex.uiDesigner.mxml;

import com.google.common.base.CharMatcher;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagChild;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.StringBuilderSpinAllocator;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.Nullable;

// isCollapseWhiteSpace — only for tag: https://bugs.adobe.com/jira/browse/SDK-3983
class XmlTagValueProvider implements XmlElementValueProvider {
  private XmlTag tag;

  public void setTag(XmlTag tag) {
    this.tag = tag;
  }

  @Override
  public String getTrimmed() {
    return tag.getValue().getTrimmedText();
  }

  @Override
  public CharSequence getSubstituted() {
    CharSequence v = getDisplay(tag.getValue().getChildren());
    if (v == EMPTY) {
      return EMPTY;
    }

    XmlElementDescriptor descriptor = tag.getDescriptor();
    // may be ClassBackedElementDescriptor for fx:String: <TextArea><text><fx:String>sfsdsd</fx:String></text></TextArea>
    if (descriptor instanceof AnnotationBackedDescriptor && ((AnnotationBackedDescriptor)descriptor).isCollapseWhiteSpace()) {
      return CharMatcher.WHITESPACE.trimAndCollapseFrom(v, ' ');
    }
    else {
      return v;
    }
  }

  @Override
  public XmlElement getInjectedHost() {
    return getInjectedHost(tag);
  }

  public static
  @Nullable
  XmlElement getInjectedHost(XmlTag tag) {
    // support <tag>{v}...</tag> or <tag>__PsiWhiteSpace__{v}...</tag>
    // <tag><span>ssss</span> {v}...</tag> is not supported
    for (XmlTagChild child : tag.getValue().getChildren()) {
      if (child instanceof XmlText) {
        return child;
      }
      else if (!(child instanceof PsiWhiteSpace)) {
        return null;
      }
    }

    return null;
  }

  static CharSequence getDisplay(XmlTag tag) {
    return getDisplay(tag.getValue().getChildren());
  }

  private static CharSequence getDisplay(XmlTagChild[] children) {
    if (children.length == 1) {
      if (children[0] instanceof XmlText) {
        return ((XmlText)children[0]).getValue();
      }
      else {
        return EMPTY;
      }
    }
    else {
      final StringBuilder consolidatedText = StringBuilderSpinAllocator.alloc();
      try {
        for (final XmlTagChild element : children) {
          consolidatedText.append(element instanceof XmlText ? ((XmlText)element).getValue() : element.getText());
        }
        return consolidatedText.length() == 0 ? EMPTY : consolidatedText;
      }
      finally {
        StringBuilderSpinAllocator.dispose(consolidatedText);
      }
    }
  }
}
