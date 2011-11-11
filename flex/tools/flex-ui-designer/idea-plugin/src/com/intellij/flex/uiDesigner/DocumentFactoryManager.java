package com.intellij.flex.uiDesigner;

import com.intellij.AppTopics;
import com.intellij.flex.uiDesigner.io.Info;
import com.intellij.flex.uiDesigner.io.InfoMap;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerAdapter;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import gnu.trove.TObjectObjectProcedure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DocumentFactoryManager extends AbstractProjectComponent {
  private final InfoMap<VirtualFile, DocumentInfo> files = new InfoMap<VirtualFile, DocumentInfo>();
  
  private MyFileDocumentManagerListener fileDocumentManagerListener;
  private MessageBusConnection designerApplicationManagerConnection;

  private boolean isSubscribed;

  public DocumentFactoryManager(Project project) {
    super(project);
  }

  public void reset() {
    if (files.isEmpty()) {
      return;
    }
    
    files.clear();
    fileDocumentManagerListener.unsubscribe();
    isSubscribed = false;
  }

  public void unregister(final int[] ids) {
    files.remove(new TObjectObjectProcedure<VirtualFile, DocumentInfo>() {
      //@Override
      public boolean execute(VirtualFile key, DocumentInfo value) {
        for (int id : ids) {
          if (value.getId() == id) {
            return false;
          }
        }

        return true;
      }
    });
  }

  public static DocumentFactoryManager getInstance(@NotNull Project project) {
    return project.getComponent(DocumentFactoryManager.class);
  }

  @Override
  public void disposeComponent() {
    if (designerApplicationManagerConnection != null) {
      designerApplicationManagerConnection.disconnect();
      // unsubscribed in applicationClosed
      if (fileDocumentManagerListener.connection != null) {
        fileDocumentManagerListener.unsubscribe();
      }
    }
  }

  private void listenChages() {
    assert !isSubscribed;
    isSubscribed = true;
    if (fileDocumentManagerListener == null) {
      fileDocumentManagerListener = new MyFileDocumentManagerListener();
    }
    
    MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
    fileDocumentManagerListener.subscribe(messageBus);
    
    if (designerApplicationManagerConnection == null) {
      designerApplicationManagerConnection = messageBus.connect();
      //designerApplicationManagerConnection.subscribe(DesignerApplicationManager.MESSAGE_TOPIC, new DesignerApplicationListener() {
      //  @Override
      //  public void initialDocumentOpened() {
      //  }
      //
      //  @Override
      //  public void applicationClosed() {
      //    reset();
      //  }
      //});
    }
  }
    
  private class MyFileDocumentManagerListener extends FileDocumentManagerAdapter {
    private MessageBusConnection connection;

    public void subscribe(MessageBus messageBus) {
      assert connection == null;
      connection = messageBus.connect();
      connection.subscribe(AppTopics.FILE_DOCUMENT_SYNC, this);
    }
    
    public void unsubscribe() {
      connection.disconnect();
      connection = null;
    }

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

      final Module module = ModuleUtil.findModuleForFile(file, myProject);
      if (module == null) {
        return;
      }

      final XmlFile psiFile = (XmlFile)PsiDocumentManager.getInstance(myProject).getPsiFile(document);
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
  
  public int getId(VirtualFile virtualFile, @Nullable XmlFile psiFile, @Nullable List<XmlFile> unregisteredDocumentFactories) {
    DocumentInfo info = files.getNullableInfo(virtualFile);
    if (info != null) {
      return info.getId();
    }

    if (!isSubscribed) {
      listenChages();
    }

    if (unregisteredDocumentFactories != null) {
      unregisteredDocumentFactories.add(psiFile);
    }

    return files.add(new DocumentInfo(virtualFile));
  }

  public @NotNull VirtualFile getFile(int id) {
    return files.getElement(id);
  }

  public @NotNull DocumentInfo getInfo(int id) {
    return files.getInfo(id);
  }

  public  static class DocumentInfo extends Info<VirtualFile> {
    public long documentModificationStamp;
    
    public DocumentInfo(@NotNull VirtualFile element) {
      super(element);
    }
  }
}