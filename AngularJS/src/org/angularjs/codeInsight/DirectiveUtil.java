package org.angularjs.codeInsight;

import com.intellij.openapi.util.text.StringUtil;

/**
 * @author Dennis.Ushakov
 */
public class DirectiveUtil {
  public static String getAttributeName(final String text) {
    final String[] split = StringUtil.unquoteString(text).split("(?=[A-Z])");
    for (int i = 0; i < split.length; i++) {
      split[i] = StringUtil.decapitalize(split[i]);
    }
    return StringUtil.join(split, "-");
  }

  public static String normalizeAttributeName(String name) {
    if (name == null) return null;
    if (name.startsWith("data-")) {
      name = name.substring(5);
    } else if (name.startsWith("x-")) {
      name = name.substring(2);
    }
    name = name.replace(':', '-');
    name = name.replace('_', '-');
    return name;
  }

  public static String attributeToDirective(String name) {
    final String[] words = name.split("-");
    for (int i = 1; i < words.length; i++) {
      words[i] = StringUtil.capitalize(words[i]);
    }
    return StringUtil.join(words);
  }
}
