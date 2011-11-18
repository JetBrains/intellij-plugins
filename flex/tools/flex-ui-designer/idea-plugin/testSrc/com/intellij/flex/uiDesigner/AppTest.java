package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.messages.MessageBusConnection;

import java.io.IOException;
import java.net.SocketException;

@Flex(version="4.5")
public class AppTest extends AppTestBase {
  private static final int APP_TEST_CLASS_ID = 3;

  private MessageBusConnection connection;
  private final Info info = new Info();

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    final MySocketInputHandler socketInputHandler = (MySocketInputHandler)SocketInputHandler.getInstance();
    socketInputHandler.info = info;
  }
  
  private VirtualFile open(String relativeFile) throws Exception {
    VirtualFile file = configureByFile(getVFile(getTestPath() + "/" + relativeFile));

    info.semaphore.down();
    assert connection == null;
    connection = ApplicationManager.getApplication().getMessageBus().connect();
    //connection.subscribe(DesignerApplicationManager.MESSAGE_TOPIC, new DesignerApplicationListener() {
    //  @Override
    //  public void initialDocumentOpened() {
    //    info.semaphore.up();
    //  }
    //
    //  @Override
    //  public void applicationClosed() {
    //  }
    //});

    DesignerApplicationManager.getInstance().openDocument(myModule, (XmlFile)file, false);
    await();

    return file;
  }
  
  private void await() throws InterruptedException {
    //assertTrue(info.semaphore.waitForUnsafe());
    info.semaphore.waitForUnsafe();
  }
  
  private void callClientAssert(String methodName) throws IOException, InterruptedException {
    info.semaphore.down();
    TestClient.test(Client.getInstance(), methodName.equals("close") ? null : myModule, methodName, APP_TEST_CLASS_ID);
    await();
    if (info.fail.get()) {
      fail();
    }
    info.fail.set(false);
  }

  public void _testCloseAndOpenProject() throws Exception {
    open("injectedAS/Transitions.mxml");

    DesignerApplicationManager designerAppManager = DesignerApplicationManager.getInstance();
    //designerAppManager.projectManagerListener.projectClosed(myProject);

    callClientAssert("close");
  }

  public void _testUpdateDocumentOnIdeaAutoSave() throws Exception {
    VirtualFile file = open("states/SetProperty.mxml");

    final Document document = FileDocumentManager.getInstance().getDocument(file);
    AccessToken token = WriteAction.start();
    try {
      assert document != null;
      document.insertString(254, "A");
    }
    finally {
      token.finish();
    }

    PsiDocumentManager.getInstance(myProject).commitAllDocuments();

    final DesignerApplicationManager designerApplicationManager = DesignerApplicationManager.getInstance();
    designerApplicationManager.openDocument(myModule, (XmlFile)file, false);
    while (designerApplicationManager.isDocumentOpening()) {
      Thread.sleep(8); // todo event about document opened
    }
    
    callClientAssert(getTestName(false));
  }
  
  @Override
  protected void tearDown() throws Exception {
    try {
      DesignerApplicationManager.getInstance().dispose();

      StringRegistry.getInstance().reset();

      if (connection != null) {
        connection.disconnect();
      }
    }
    finally {
      super.tearDown();
    }
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
          info.semaphore.up();
        }
      }

      return true;
    }
  }
  
  private static class Info {
    private final Semaphore semaphore = new Semaphore();
    private final Ref<Boolean> fail = new Ref<Boolean>(false);
  }
}
