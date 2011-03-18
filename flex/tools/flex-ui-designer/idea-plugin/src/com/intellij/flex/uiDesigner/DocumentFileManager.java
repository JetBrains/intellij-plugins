package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import gnu.trove.TIntArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DocumentFileManager {
  private static final Key<DocumentInfo> INFO = Key.create("FUD_DOCUMENT_FILE_INFO");

  private TIntArrayList freeIndices = new TIntArrayList();
  private int counter;

  private int sessionId;

  public static DocumentFileManager getInstance() {
    return ServiceManager.getService(DocumentFileManager.class);
  }

  public boolean isRegistered(VirtualFile virtualFile) {
    DocumentInfo info = virtualFile.getUserData(INFO);
    return info != null && info.sessionId == sessionId;
  }

  public DocumentInfo getId(VirtualFile virtualFile, XmlFile psiFile, @Nullable List<XmlFile> unregisteredDocumentFactories) {
    DocumentInfo info = virtualFile.getUserData(INFO);
    if (info == null || info.sessionId != sessionId) {
      if (info == null) {
        info = new DocumentInfo();
      }

      info.id = freeIndices.isEmpty() ? counter++ : freeIndices.remove(freeIndices.size() - 1);
      info.sessionId = sessionId;

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

  public void reset(int sessionId) {
    counter = 0;
    freeIndices.resetQuick();
    this.sessionId = sessionId;
  }

  public static class DocumentInfo {
    private int id;
    private int sessionId;
    private String className;

    public int getId() {
      return id;
    }

    public String getClassName() {
      return className;
    }
  }
}