package com.intellij.flex.uiDesigner.mxml;

import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.xml.*;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.regex.Pattern;

public final class MxmlUtil {
  private static final Pattern FLEX_SDK_ABSTRACT_CLASSES = Pattern.compile("^(mx|spark)\\.(.*)?Base$");

  // about id http://opensource.adobe.com/wiki/display/flexsdk/id+property+in+MXML+2009
  static boolean isIdLanguageIdAttribute(XmlAttribute attribute) {
    final String ns = attribute.getNamespace();
    return ns.isEmpty() || ns.equals(JavaScriptSupportLoader.MXML_URI3);
  }

  static boolean isComponentLanguageTag(XmlTag tag) {
    return tag.getNamespace().equals(JavaScriptSupportLoader.MXML_URI3) && tag.getLocalName().equals("Component");
  }

  static boolean containsOnlyWhitespace(XmlTagChild child) {
    final PsiElement firstChild = child.getFirstChild();
    return firstChild == child.getLastChild() && (firstChild == null || firstChild instanceof PsiWhiteSpace);
  }

  @Nullable
  public static XmlElement getInjectedHost(XmlTag tag) {
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

  public static boolean isAbstract(ClassBackedElementDescriptor classBackedDescriptor) {
    return FLEX_SDK_ABSTRACT_CLASSES.matcher(classBackedDescriptor.getQualifiedName()).matches();
  }
  
  public static boolean isFlashDisplayContainerClass (JSClass jsClass) {
    return checkClassHasParentOfAnotherOne(jsClass, "flash.display.Sprite", null);
  }

  private static boolean checkClassHasParentOfAnotherOne(final JSClass aClass, final String parent, @Nullable Set<JSClass> visited) {
    if (visited != null && visited.contains(aClass)) {
      return false;
    }

    for (JSClass superClazz : aClass.getSupers()) {
      if (superClazz.getQualifiedName().equals(parent)) {
        return true;
      }

      if (visited == null) {
        visited = new THashSet<JSClass>();
      }
      visited.add(aClass);
      if (checkClassHasParentOfAnotherOne(superClazz, parent, visited)) {
        return true;
      }
    }
    return false;
  }
}
