package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.index.JSNamedElementIndexItem;
import com.intellij.lang.javascript.index.JSNamespace;
import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.index.JSSymbolVisitor;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassProvider;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptSymbolVisitor extends JSSymbolVisitor {
  public ActionScriptSymbolVisitor(JSNamespace namespace,
                                   JSSymbolUtil.JavaScriptSymbolProcessorEx symbolVisitor,
                                   PsiFile file) {
    super(namespace, symbolVisitor, file);
    boolean processAllTags = false;
    if (file instanceof XmlFile) {
      XmlBackedJSClassProvider provider = XmlBackedJSClassProvider.forFile((XmlFile)file);
      if (provider != null && provider.processAllTags()) {
        processAllTags = true;
        String fileName = file.getOriginalFile().getVirtualFile().getNameWithoutExtension();
        myThisNamespace = myFileNamespace = myNamespace = namespace.getChildNamespace(fileName);
      }
    }
    if (JavaScriptSupportLoader.isFlexMxmFile(myFile) || processAllTags) {
      myNamedTagsAreMembersOfDocument = false;
    }
  }

  @Override
  protected boolean updateNsFromAttributeList(JSAttributeListOwner node) {
    final JSAttributeList attributeList = node.getAttributeList();
    if (attributeList != null) {
      final String ns = attributeList.getNamespace();
      if (ResolveProcessor.AS3_NAMESPACE.equals(ns)) return false;
      if (ns != null) myNamespace = getNestedNsWithName(ns, myNamespace);
      myAccessType = attributeList.getAccessType();
    }
    return true;
  }

  @Override
  protected void processXmlTag(XmlTag element) {
    String id = element.getAttributeValue("id");
    if (id != null) {
      myAttributeName = JSNamedElementIndexItem.AttributeName.Id;
      mySymbolVisitor.processTag(myFileNamespace, id, element, "id");
    }

    element.acceptChildren(this);
  }
}
