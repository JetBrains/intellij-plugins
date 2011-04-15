package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.AmfOutputStream;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.messages.MessageBusConnection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AppTest extends AppTestBase {
  private MessageBusConnection connection;
  private final Info info = new Info();

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    changeServiceImplementation(SocketInputHandler.class, MySocketInputHandler.class);
    final MySocketInputHandler socketInputHandler = (MySocketInputHandler)ServiceManager.getService(SocketInputHandler.class);
    socketInputHandler.info = info;
  }
  
  private VirtualFile open(String relativeFile) throws IOException, InterruptedException {
    VirtualFile newParent = configureByFiles(null, getVFile(getTestPath() + "/" + relativeFile));

    assert connection == null;
    connection = ApplicationManager.getApplication().getMessageBus().connect();
    connection.subscribe(FlexUIDesignerApplicationManager.MESSAGE_TOPIC, new FlexUIDesignerApplicationListener() {
      @Override
      public void initialDocumentOpened() {
        info.lock.countDown();
      }

      @Override
      public void applicationClosed() {
      }
    });
    
    copySwfAndDescriptor(new File(PathManager.getSystemPath(), "flexUIDesigner"));
    FlexUIDesignerApplicationManager.getInstance().openDocument(myProject, myModule, (XmlFile)myFile, false);
    await();

    if (!info.result.get().equals(PASSED)) {
      fail(info.result.get());
    }
    
    return newParent;
  }
  
  private void await() throws InterruptedException {
    assertTrue(info.lock.await(10, TimeUnit.SECONDS));
    //lock.await();
  }
  
  private void callClientAssert(String methodName) throws IOException, InterruptedException {
    info.lock = new CountDownLatch(1);
    
    Client client = FlexUIDesignerApplicationManager.getInstance().getClient();
    AmfOutputStream out = client.getOutput();
    out.write(1);
    out.writeAmfUtf(methodName, false);
    out.write(3);
    
    client.flush();
    
    await();
    
    info.result.set("");
  }

  @Flex(version="4.5")
  public void testCloseAndOpenProject() throws Exception {
    info.count = 1;
    
    open("injectedAS/Transitions.mxml");
    
    FlexUIDesignerApplicationManager designerAppManager = FlexUIDesignerApplicationManager.getInstance();
    designerAppManager.projectManagerListener.projectClosed(myProject);

    callClientAssert("close");
  }
  
  @Flex(version="4.5")
  public void testUpdateDocumentOnIdeaAutoSave() throws Exception {
    info.count = 2;
    
    VirtualFile newParent = open("states/SetProperty.mxml");
    
    callClientAssert("wait"); // hack, wait library loaded on client
    
    VirtualFile virtualFile = newParent.getChildren()[0];
    final Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        assert document != null;
        document.insertString(254, "A");
      }
    });

    PsiDocumentManager.getInstance(myProject).commitAllDocuments();

    final FlexUIDesignerApplicationManager designerApplicationManager = FlexUIDesignerApplicationManager.getInstance();
    designerApplicationManager.openDocument(myProject, myModule, (XmlFile)myFile, false);
    while (designerApplicationManager.isDocumentOpening()) {
      Thread.sleep(8); // todo event about document open?
    }
    
    callClientAssert(getTestName(false));
  }
  
  @Override
  protected void tearDown() throws Exception {
    FlexUIDesignerApplicationManager.getInstance().dispose();
    
    if (connection != null) {
      connection.disconnect();
    }
    
    super.tearDown();
  }
  
  private static class MySocketInputHandler extends SocketInputHandlerImpl {
    private Info info;

    @Override
    public void read(InputStream inputStream) throws IOException {
      createReader(inputStream);
      while (info.count != 0) {
        try {
          info.result.set(reader.readUTF());
        }
        catch (IOException e) {
          if (!(e instanceof SocketException)) {
            LOG.error(e);
          }
          break;
        }
        finally {
          info.lock.countDown();
          info.count--;
        }
      }
    }
  }
  
  private static class Info {
    private CountDownLatch lock = new CountDownLatch(1);
    private final Ref<String> result = new Ref<String>("");
    private int count;
  }
}
