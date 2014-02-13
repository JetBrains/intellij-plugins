package org.angularjs.codeInsight;

import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.SchemaPrefix;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.HtmlXmlExtension;
import org.angularjs.index.AngularIndexUtil;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSHtmlExtension extends HtmlXmlExtension {
  @Override
  public boolean isAvailable(PsiFile file) {
    return super.isAvailable(file) && AngularIndexUtil.hasAngularJS(file.getProject());
  }

  @Override
  public boolean isRequiredAttributeImplicitlyPresent(XmlTag tag, String attrName) {
    for (XmlAttribute attribute : tag.getAttributes()) {
      if (("ng-" + attrName).equals(AngularJSAttributeDescriptorsProvider.normalizeAttributeName(attribute.getName()))) {
        return true;
      }
    }

    return super.isRequiredAttributeImplicitlyPresent(tag, attrName);
  }

  @Override
  public SchemaPrefix getPrefixDeclaration(XmlTag context, String namespacePrefix) {
    if ("ng".equals(namespacePrefix)) {
      return new SchemaPrefix(null, null, namespacePrefix);
    }
    return super.getPrefixDeclaration(context, namespacePrefix);
  }
}
