package com.intellij.lang.javascript.flex;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.index.JSNamespace;
import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.index.JSSymbolVisitor;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;

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
}
