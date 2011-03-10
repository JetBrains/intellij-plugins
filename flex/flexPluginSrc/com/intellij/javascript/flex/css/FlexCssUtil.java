package com.intellij.javascript.flex.css;

import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSIncludeDirective;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author Eugene.Kudelevsky
 */
public class FlexCssUtil {
  private static final String STYLE_NAME_SUFFIX = "style-name";

  private FlexCssUtil() {
  }

  public static boolean isStyleNameProperty(@NotNull String propertyName) {
    return toClassicForm(propertyName).endsWith(STYLE_NAME_SUFFIX);
  }

  public static String toClassicForm(String propertyName) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < propertyName.length(); i++) {
      char c = propertyName.charAt(i);
      char c1 = Character.toLowerCase(c);
      if (c1 != c && result.length() > 0) {
        result.append('-');
      }
      result.append(c1);
    }
    return result.toString();
  }

  public static boolean inQuotes(String text) {
    return text != null && text.length() >= 2 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"';
  }

  public static void collectAllIncludes(PsiElement element, Set<String> result) {
    if (element instanceof JSClass) {
      PsiElement e = element.getPrevSibling();
      while (e != null) {
        if (e instanceof JSIncludeDirective) {
          String text = ((JSIncludeDirective)e).getIncludeText();
          if (text != null) {
            result.add(text);
          }
        }
        e = e.getPrevSibling();
      }
    }
    for (PsiElement child : element.getChildren()) {
      if (child instanceof JSIncludeDirective) {
        String text = ((JSIncludeDirective)child).getIncludeText();
        if (text != null) {
          result.add(text);
        }
      }
      else if (child instanceof JSAttributeList || child instanceof JSAttributeListOwner) {
        collectAllIncludes(child, result);
      }
    }
  }
}
