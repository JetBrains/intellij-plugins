package com.intellij.flex.uiDesigner;

import com.intellij.AppTopics;
import com.intellij.flex.uiDesigner.io.Info;
import com.intellij.flex.uiDesigner.io.InfoMap;
import com.intellij.flex.uiDesigner.mxml.ProjectComponentReferenceCounter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
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

  private static class MyFileDocumentManagerListener extends FileDocumentManagerAdapter {
    @Override
    public void beforeAllDocumentsSaving() {
      final Document[] unsavedDocuments = FileDocumentManager.getInstance().getUnsavedDocuments();
      if (unsavedDocuments.length > 0) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          @Override
          public void run() {
            ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
              @Override
              public void run() {
                //DesignerApplicationManager.getInstance().renderDocumentsAndCheckLocalStyleModification(unsavedDocuments);
              }
            });
          }
        });
      }
    }
  }

  public boolean isRegistered(VirtualFile virtualFile) {
    return files.contains(virtualFile);
  }

  public int getId(VirtualFile virtualFile) {
    return getId(virtualFile, null, null);
  }
  
  public int getId(VirtualFile virtualFile, @Nullable XmlFile psiFile, @Nullable ProjectComponentReferenceCounter referenceCounter) {
    return get(virtualFile, psiFile, referenceCounter).getId();
  }

  @Nullable
  public DocumentInfo getNullableInfo(VirtualFile virtualFile) {
    return files.getNullableInfo(virtualFile);
  }

  @Nullable
  public DocumentInfo getNullableInfo(PsiFile psiFile) {
    return files.getNullableInfo(psiFile.getVirtualFile());
  }

  public DocumentInfo get(VirtualFile virtualFile, @Nullable XmlFile psiFile, @Nullable ProjectComponentReferenceCounter referenceCounter) {
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

  @NotNull
  public VirtualFile getFile(int id) {
    return files.getElement(id);
  }

  @NotNull
  public DocumentInfo getInfo(int id) {
    return files.getInfo(id);
  }

  @NotNull
  public DocumentInfo getInfo(PsiElement element) {
    return files.getInfo(element.getContainingFile().getVirtualFile());
  }

  public static final class DocumentInfo extends Info<VirtualFile> {
    public long documentModificationStamp;

    private List<RangeMarker> rangeMarkers;

    public RangeMarker getRangeMarker(int id) {
      return rangeMarkers.get(id);
    }
    
    public DocumentInfo(@NotNull VirtualFile element) {
      super(element);
    }

    public int rangeMarkerIndexOf(PsiElement element) {
      for (int i = 0; i < rangeMarkers.size(); i++) {
        RangeMarker rangeMarker = rangeMarkers.get(i);
        if (rangeMarker.getStartOffset() == element.getTextOffset()) {
          return i;
        }
      }

      return -1;
    }

    public List<RangeMarker> getRangeMarkers() {
      return rangeMarkers;
    }

    public void setRangeMarkers(List<RangeMarker> rangeMarkers) {
      this.rangeMarkers = rangeMarkers;
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj || obj instanceof DocumentInfo && ((DocumentInfo)obj).getId() == getId();
    }
  }
}