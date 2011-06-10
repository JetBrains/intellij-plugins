package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.libraries.LibraryManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.messages.MessageBusConnection;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AppTest extends AppTestBase {
  private static final int APP_TEST_CLASS_ID = 3;

  private MessageBusConnection connection;
  private final Info info = new Info();

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    //TestDesignerApplicationManager.changeServiceImplementation();
    TestDesignerApplicationManager.changeServiceImplementation(Client.class, TestClient.class);
    TestDesignerApplicationManager.changeServiceImplementation(SocketInputHandler.class, MySocketInputHandler.class);
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

    FlexUIDesignerApplicationManager.getInstance().openDocument(myModule, (XmlFile)myFile, false);
    await();
    return newParent;
  }
  
  private void await() throws InterruptedException {
    assertTrue(info.lock.await(1000, TimeUnit.SECONDS));
    //lock.await();
  }
  
  private void callClientAssert(String methodName) throws IOException, InterruptedException {
    info.lock = new CountDownLatch(1);
    TestClient.test(Client.getInstance(), methodName, APP_TEST_CLASS_ID);
    await();
    if (info.fail.get()) {
      fail();
    }
    info.fail.set(false);
  }

  @Flex(version="4.5")
  public void testCloseAndOpenProject() throws Exception {
    open("injectedAS/Transitions.mxml");

    TestClient.test(Client.getInstance(), "useRealProjectManagerBehavior", 0);

    FlexUIDesignerApplicationManager designerAppManager = FlexUIDesignerApplicationManager.getInstance();
    designerAppManager.projectManagerListener.projectClosed(myProject);

    callClientAssert("close");
  }
  
  @Flex(version="4.5")
  public void testUpdateDocumentOnIdeaAutoSave() throws Exception {
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
    designerApplicationManager.openDocument(myModule, (XmlFile)myFile, false);
    while (designerApplicationManager.isDocumentOpening()) {
      Thread.sleep(8); // todo event about document open?
    }
    
    callClientAssert(getTestName(false));
  }
  
  @Override
  protected void tearDown() throws Exception {
    FlexUIDesignerApplicationManager.getInstance().dispose();

    StringRegistry.getInstance().reset();

    BinaryFileManager.getInstance().reset();
    LibraryManager.getInstance().reset();
    
    if (connection != null) {
      connection.disconnect();
    }
    
    super.tearDown();
  }

  private static class MySocketInputHandler extends TestSocketInputHandler {
    private Info info;

    @Override
    protected boolean processCommand(int command) throws IOException {
      boolean result = true;
      try {
        result = super.processCommand(command);
      }
      catch (Throwable e) {
        if (!(e instanceof SocketException)) {
          LOG.error(e);
          result = false;
          info.fail.set(true);
        }
      }
      finally {
        if (!result) {
          info.lock.countDown();
        }
      }

      return true;
    }
  }
  
  private static class Info {
    private CountDownLatch lock = new CountDownLatch(1);
    private final Ref<Boolean> fail = new Ref<Boolean>(false);
  }
}
