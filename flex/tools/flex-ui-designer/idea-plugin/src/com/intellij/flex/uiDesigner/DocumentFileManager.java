package com.intellij.flex.uiDesigner;

import com.intellij.AppTopics;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerAdapter;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectLocator;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.xml.XmlFile;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DocumentFileManager extends AbstractFileManager<DocumentFileManager.DocumentInfo> {
  private static final Key<DocumentInfo> INFO = Key.create("FUD_DOCUMENT_FILE_INFO");

  public DocumentFileManager() {
    ApplicationManager.getApplication().getMessageBus().connect().subscribe(AppTopics.FILE_DOCUMENT_SYNC, new FileDocumentManagerAdapter() {
      @Override
      public void beforeDocumentSaving(Document document) {
        final VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file == null) {
          return;
        }
        
        DocumentInfo info = file.getUserData(INFO);
        if (!isRegistered(info) || FlexUIDesignerApplicationManager.getInstance().isDocumentOpening()) {
          return;
        }

        Project project = ProjectLocator.getInstance().guessProjectForFile(file);
        if (project == null) {
          return;
        }
        
        final Module moduleForFile = ModuleUtil.findModuleForFile(file, project);
        if (moduleForFile == null) {
          return;
        }
        
        XmlFile psiFile = (XmlFile)PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile == null) {
          return;
        }

        
        FlexUIDesignerApplicationManager.getInstance().openDocument(project, moduleForFile, psiFile, false);
      }
    });
  }

  public static DocumentFileManager getInstance() {
    return ServiceManager.getService(DocumentFileManager.class);
  }

  public boolean isRegistered(VirtualFile virtualFile) {
    return isRegistered(virtualFile.getUserData(INFO));
  }
  
  @SuppressWarnings({"ConstantConditions"})
  public boolean isActual(VirtualFile virtualFile) {
    final DocumentInfo info = virtualFile.getUserData(INFO);
    //return isRegistered(info) && info.mtime == virtualFile.getModificationCount();
    return false;
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
      
      info.mtime = virtualFile.getModificationCount();
    }

    return info;
  }

  public static class DocumentInfo extends AbstractFileManager.FileInfo {
    private String className;

    private long mtime;

    public String getClassName() {
      return className;
    }
  }
}