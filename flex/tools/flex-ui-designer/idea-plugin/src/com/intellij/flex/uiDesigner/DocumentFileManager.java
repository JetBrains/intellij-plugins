package com.intellij.flex.uiDesigner;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import gnu.trove.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

public class DocumentFileManager {
  private static final Key<Integer> ID = Key.create("FUD_DOCUMENT_FILE_ID");
  
  private TIntArrayList freeIndices = new TIntArrayList();
  private int counter;

  public static DocumentFileManager getInstance() {
    return ServiceManager.getService(DocumentFileManager.class);
  }

  public int getId(VirtualFile virtualFile, XmlFile psiFile, List<XmlFile> unregisteredDocumentFactories) {
    Integer id = virtualFile.getUserData(ID);
    if (id == null) {
      id = freeIndices.isEmpty() ? counter++ : freeIndices.remove(freeIndices.size() - 1);
      virtualFile.putUserData(ID, id);
      unregisteredDocumentFactories.add(psiFile);
    }
    
    return id;
  }
}
