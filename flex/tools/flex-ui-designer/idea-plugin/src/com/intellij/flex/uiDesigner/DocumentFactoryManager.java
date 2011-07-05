package com.intellij.flex.uiDesigner;

import com.intellij.AppTopics;
import com.intellij.flex.uiDesigner.io.Info;
import com.intellij.flex.uiDesigner.io.InfoList;
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
  private final InfoList<VirtualFile, DocumentInfo> files = new InfoList<VirtualFile, DocumentInfo>();
  
  private MyFileDocumentManagerListener fileDocumentManagerListener;
  private MessageBusConnection flexUIDesignerApplicationManagerConnection;

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
    if (flexUIDesignerApplicationManagerConnection != null) {
      flexUIDesignerApplicationManagerConnection.disconnect();
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
    
    if (flexUIDesignerApplicationManagerConnection == null) {
      flexUIDesignerApplicationManagerConnection = messageBus.connect();
      flexUIDesignerApplicationManagerConnection.subscribe(FlexUIDesignerApplicationManager.MESSAGE_TOPIC, new FlexUIDesignerApplicationListener() {
        @Override
        public void initialDocumentOpened() {
        }

        @Override
        public void applicationClosed() {
          reset();
        }
      });
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
      VirtualFile file = FileDocumentManager.getInstance().getFile(document);
      if (file == null) {
        return;
      }
      
      DocumentInfo info = files.getNullableInfo(file);
      if (info == null) {
        return;
      }
      
      FlexUIDesignerApplicationManager flexUIDesignerApplicationManager = FlexUIDesignerApplicationManager.getInstance();
      if (flexUIDesignerApplicationManager.isDocumentOpening()) {
        return;
      }

      Module module = ModuleUtil.findModuleForFile(file, myProject);
      if (module == null) {
        return;
      }

      XmlFile psiFile = (XmlFile)PsiDocumentManager.getInstance(myProject).getPsiFile(document);
      if (psiFile == null) {
        return;
      }

      if (info.psiModificationStamp == psiFile.getModificationStamp()) {
        info.psiModificationStamp = -1;
        return;
      }

      flexUIDesignerApplicationManager.updateDocumentFactory(info.getId(), module, psiFile);
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
    public long psiModificationStamp;
    
    public DocumentInfo(@NotNull VirtualFile element) {
      super(element);
    }
  }
}