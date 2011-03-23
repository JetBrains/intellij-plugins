package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DocumentFileManager extends AbstractFileManager<DocumentFileManager.DocumentInfo> {
  private static final Key<DocumentInfo> INFO = Key.create("FUD_DOCUMENT_FILE_INFO");
  
  public static DocumentFileManager getInstance() {
    return ServiceManager.getService(DocumentFileManager.class);
  }
  
  public boolean isRegistered(VirtualFile virtualFile) {
    return isRegistered(virtualFile.getUserData(INFO));
  }

  public DocumentInfo getInfo(VirtualFile virtualFile, XmlFile psiFile, @Nullable List<XmlFile> unregisteredDocumentFactories) {
    DocumentInfo info = virtualFile.getUserData(INFO);
    if (!isRegistered(info)) {
      if (info == null) {
        info = new DocumentInfo();
      }

      initInfo(info);

      JSClass jsClass = XmlBackedJSClassImpl.getXmlBackedClass(psiFile);
      assert jsClass != null;
      info.className = jsClass.getName();

      virtualFile.putUserData(INFO, info);
      if (unregisteredDocumentFactories != null) {
        unregisteredDocumentFactories.add(psiFile);
      }
    }

    return info;
  }

  public static class DocumentInfo extends AbstractFileManager.FileInfo {
    private String className;

    public String getClassName() {
      return className;
    }
  }
}