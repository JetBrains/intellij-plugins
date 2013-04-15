package com.dmarcotte.handlebars.util;

import com.dmarcotte.handlebars.HbLanguage;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.BooleanValueHolder;

public class HbUtils {
  public static boolean hasHbScriptTag(XmlFile file) {
    final BooleanValueHolder result = new BooleanValueHolder(false);
    file.accept(new XmlRecursiveElementVisitor() {
      @Override
      public void visitXmlTag(XmlTag tag) {
        super.visitXmlTag(tag);
        if ("script".equalsIgnoreCase(tag.getLocalName()) &&
            ArrayUtilRt.find(HbLanguage.INSTANCE.getMimeTypes(), tag.getAttributeValue("type")) >= 0) {
          result.setValue(true);
        }
      }
    });
    return result.getValue();
  }
}
