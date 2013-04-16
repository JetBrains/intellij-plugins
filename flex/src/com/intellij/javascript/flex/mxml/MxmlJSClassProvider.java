package com.intellij.javascript.flex.mxml;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassProvider;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NonNls;

/**
 * @author yole
 */
public class MxmlJSClassProvider extends XmlBackedJSClassProvider {
  @NonNls public static final String SCRIPT_TAG_NAME = "Script";

  public static MxmlJSClassProvider getInstance() {
    for (XmlBackedJSClassProvider provider : Extensions.getExtensions(EP_NAME)) {
      if (provider instanceof MxmlJSClassProvider) {
        return (MxmlJSClassProvider)provider;
      }
    }
    assert false;
    return null;
  }

  @Override
  public boolean hasJSClass(XmlFile file) {
    return JavaScriptSupportLoader.isFlexMxmFile(file);
  }

  @Override
  public boolean isScriptTag(XmlTag tag) {
    return SCRIPT_TAG_NAME.equals(tag.getLocalName());
  }
}
