package com.google.jstestdriver.idea;

import com.google.common.collect.Maps;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;
import java.util.regex.Pattern;

public class JsTestDriverTestUtils {

  private JsTestDriverTestUtils() {}

  public static File getTestDataDir() {
    return new File("./test/testData/");
  }

  public static Map<String, String> parseProperties(String propertiesStr) {
    Map<String, String> props = Maps.newLinkedHashMap();
    String[] keyValueStrings = propertiesStr.split(Pattern.quote(","));
    for (String keyValueStr : keyValueStrings) {
      String[] components = keyValueStr.split(Pattern.quote(":"), 2);
      if (components.length == 2) {
        props.put(components[0].trim(), components[1]);
      }
    }
    return props;
  }

  @NotNull
  public static PsiElement findExactPsiElement(JSFile jsFile, TextRange textRange) {
    if (textRange.getLength() < 2) {
      throw new RuntimeException("Too small text range to find exact PsiElement");
    }
    PsiElement psiElement = jsFile.findElementAt(textRange.getStartOffset() + 1);
    while (psiElement != null && !psiElement.getTextRange().contains(textRange)) {
      psiElement = psiElement.getParent();
    }
    if (psiElement != null && psiElement.getTextRange().equals(textRange)) {
      PsiElement parent = psiElement.getParent();
      if (parent != null && parent.getTextRange().equals(textRange)) {
        throw new RuntimeException("Multiply matching PsiElements");
      }
      return psiElement;
    }
    throw new RuntimeException("Can't find exact PsiElement");
  }

}
