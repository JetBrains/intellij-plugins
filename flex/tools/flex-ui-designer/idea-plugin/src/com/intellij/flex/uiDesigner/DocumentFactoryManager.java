package com.intellij.flex.uiDesigner;

import com.intellij.AppTopics;
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
import gnu.trove.TIntArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DocumentFactoryManager extends AbstractProjectComponent {
  private final List<VirtualFile> files = new ArrayList<VirtualFile>();
  private final TIntArrayList freeIndices = new TIntArrayList();
  
  private MyFileDocumentManagerListener fileDocumentManagerListener;
  private MessageBusConnection flexUIDesignerApplicationManagerConnection;

  public DocumentFactoryManager(Project project) {
    super(project);
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
          files.clear();
          freeIndices.resetQuick();
          fileDocumentManagerListener.unsubscribe();
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
      
      int id = files.indexOf(file);
      if (id == -1) {
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

      flexUIDesignerApplicationManager.updateDocumentFactory(id, module, psiFile);
    }
  }

  public boolean isRegistered(VirtualFile virtualFile) {
    return files.indexOf(virtualFile) != -1;
  }
  
  public int getId(VirtualFile virtualFile) {
    return getId(virtualFile, null, null);
  }
  
  public int getId(VirtualFile virtualFile, @Nullable XmlFile psiFile, @Nullable List<XmlFile> unregisteredDocumentFactories) {
    int id = files.indexOf(virtualFile);
    if (id == -1) {
      if (freeIndices.isEmpty()) {
        if (files.isEmpty()) {
          listenChages();
        }
        
        id = files.size();
        files.add(virtualFile);
      }
      else {
        id = freeIndices.remove(freeIndices.size() - 1);
        files.set(id, virtualFile);
      }
      
      if (unregisteredDocumentFactories != null) {
        unregisteredDocumentFactories.add(psiFile);
      }
    }
    
    return id;
  }
}