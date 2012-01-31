package com.intellij.flex.uiDesigner;

import com.intellij.AppTopics;
import com.intellij.flex.uiDesigner.io.Info;
import com.intellij.flex.uiDesigner.io.InfoMap;
import com.intellij.flex.uiDesigner.mxml.ProjectDocumentReferenceCounter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerAdapter;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.xml.XmlFile;
import gnu.trove.TObjectObjectProcedure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DocumentFactoryManager {
  private final InfoMap<VirtualFile, DocumentInfo> files = new InfoMap<VirtualFile, DocumentInfo>();

  public DocumentFactoryManager() {
    ApplicationManager.getApplication().getMessageBus().connect(DesignerApplicationManager.getApplication())
      .subscribe(AppTopics.FILE_DOCUMENT_SYNC, new MyFileDocumentManagerListener());
  }

  public static DocumentFactoryManager getInstance() {
    return DesignerApplicationManager.getService(DocumentFactoryManager.class);
  }

  public void unregister(final int[] ids) {
    files.remove(ids);
  }

  public void unregister(final Project project) {
    files.remove(new TObjectObjectProcedure<VirtualFile, DocumentInfo>() {
      @Override
      public boolean execute(VirtualFile file, DocumentInfo documentInfo) {
        return ProjectUtil.guessProjectForFile(file) != project;
      }
    });
  }

  private class MyFileDocumentManagerListener extends FileDocumentManagerAdapter {
    @Override
    public void beforeDocumentSaving(Document document) {
      final VirtualFile file = FileDocumentManager.getInstance().getFile(document);
      if (file == null) {
        return;
      }

      final DocumentInfo info = files.getNullableInfo(file);
      if (info == null) {
        return;
      }

      final DesignerApplicationManager designerApplicationManager = DesignerApplicationManager.getInstance();
      if (designerApplicationManager.isDocumentOpening()) {
        return;
      }

      if (info.documentModificationStamp == document.getModificationStamp()) {
        info.documentModificationStamp = -1;
        return;
      }

      final Project project = ProjectUtil.guessProjectForFile(file);
      if (project == null) {
        return;
      }

      final Module module = ModuleUtil.findModuleForFile(file, project);
      if (module == null) {
        return;
      }

      final XmlFile psiFile = (XmlFile)PsiDocumentManager.getInstance(project).getPsiFile(document);
      if (psiFile == null) {
        return;
      }

      designerApplicationManager.updateDocumentFactory(info.getId(), module, psiFile);
    }
  }

  public boolean isRegistered(VirtualFile virtualFile) {
    return files.contains(virtualFile);
  }

  public int getId(VirtualFile virtualFile) {
    return getId(virtualFile, null, null);
  }
  
  public int getId(VirtualFile virtualFile, @Nullable XmlFile psiFile, @Nullable ProjectDocumentReferenceCounter referenceCounter) {
    return get(virtualFile, psiFile, referenceCounter).getId();
  }

  @Nullable
  public DocumentInfo getNullableInfo(VirtualFile virtualFile) {
    return files.getNullableInfo(virtualFile);
  }

  public DocumentInfo get(VirtualFile virtualFile, @Nullable XmlFile psiFile, @Nullable ProjectDocumentReferenceCounter referenceCounter) {
    DocumentInfo info = files.getNullableInfo(virtualFile);
    if (info != null) {
      if (referenceCounter != null) {
        referenceCounter.registered(info.getId());
      }
      return info;
    }

    info = new DocumentInfo(virtualFile);
    files.add(info);

    if (referenceCounter != null) {
      referenceCounter.unregistered(info.getId(), psiFile);
    }

    return info;
  }

  public @NotNull VirtualFile getFile(int id) {
    return files.getElement(id);
  }

  public @NotNull DocumentInfo getInfo(int id) {
    return files.getInfo(id);
  }

  public static class DocumentInfo extends Info<VirtualFile> {
    public long documentModificationStamp;

    private List<RangeMarker> rangeMarkers;

    public RangeMarker getRangeMarker(int id) {
      return rangeMarkers.get(id);
    }
    
    public DocumentInfo(@NotNull VirtualFile element) {
      super(element);
    }

    public List<RangeMarker> getRangeMarkers() {
      return rangeMarkers;
    }

    public void setRangeMarkers(List<RangeMarker> rangeMarkers) {
      this.rangeMarkers = rangeMarkers;
    }
  }
}