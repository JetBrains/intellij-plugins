package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.concurrency.Semaphore;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.SocketException;

@Flex(version="4.5")
public class AppTest extends AppTestBase {
  private static final int APP_TEST_CLASS_ID = 3;

  private final Info info = new Info();

  private final Semaphore semaphore = new Semaphore();

  @Override
  protected void changeServicesImplementation() {
    Tests.changeDesignerServiceImplementation(SocketInputHandler.class, MySocketInputHandler.class);
    Tests.changeDesignerServiceImplementation(Client.class, TestClient.class);
  }
  
  private VirtualFile open(String relativeFile) throws Exception {
    VirtualFile file = configureByFile(getSource(relativeFile));
    openAndWait(file, relativeFile);
    return file;
  }

  @Override
  protected void applicationLaunchedAndInitialized() {
    ((MySocketInputHandler)SocketInputHandler.getInstance()).info = info;
  }

  private void await() throws InterruptedException {
    info.semaphore.waitForUnsafe();
  }
  
  private void callClientAssert(String methodName) throws IOException, InterruptedException {
    info.semaphore.down();
    client.test(methodName.equals("close") ? null : myModule, methodName, APP_TEST_CLASS_ID);
    await();
    if (info.fail.get()) {
      fail();
    }
    info.fail.set(false);
  }

  public void testCloseAndOpenProject() throws Exception {
    open("injectedAS/Transitions.mxml");
  }

  public void testUpdateDocumentOnIdeaAutoSave() throws Exception {
    VirtualFile f1 = getSource("ProjectMxmlComponentAsChild.mxml");
    VirtualFile f2 = getSource("AuxProjectMxmlComponent.mxml");
    configureByFiles(null, new VirtualFile[]{f1, f2}, null);

    DesignerApplicationManager designerManager = DesignerApplicationManager.getInstance();

    semaphore.down();
    designerManager.renderDocument(myModule, Tests.virtualToPsi(myProject, f1)).doWhenProcessed(new Runnable() {
      @Override
      public void run() {
        semaphore.up();
      }
    });
    semaphore.waitForUnsafe();

    final Document document = FileDocumentManager.getInstance().getDocument(f1);
    assert document != null;
    final AccessToken token = WriteAction.start();
    try {
      document.insertString(166, "A");
    }
    finally {
      token.finish();
    }

    info.semaphore.down();
    client.test(myModule, "ProjectMxmlComponentAsChild", Tests.INFORM_DOCUMENT_OPENED);
    socketInputHandler.setCustomMessageHandler(new MyCustomMessageHandler());

    PsiDocumentManager.getInstance(myProject).commitAllDocuments();
    designerManager.renderDocument(myModule, Tests.virtualToPsi(myProject, f1));
    await();
    callClientAssert(getTestName(false));
  }

  private void openAndWait(VirtualFile file, @Nullable String relativePath) throws InterruptedException, IOException {
    info.semaphore.down();

    if (relativePath != null) {
      client.test(myModule, relativePath, Tests.INFORM_DOCUMENT_OPENED);
    }
    socketInputHandler.setCustomMessageHandler(new MyCustomMessageHandler());

    DesignerApplicationManager.getInstance().renderDocument(myModule, Tests.virtualToPsi(myProject, file));
    await();
  }
  
  @Override
  protected void tearDown() throws Exception {
    try {
      super.tearDown();
      if (getName().equals("testCloseAndOpenProject")) {
        callClientAssert("close");
      }
    }
    finally {
      DesignerApplicationManager.getInstance().disposeApplication();
      StringRegistry.getInstance().reset();
    }
  }

  private static class MySocketInputHandler extends TestSocketInputHandler {
    private Info info;

    @Override
    protected boolean processOnRead() {
      return true;
    }

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

  private class MyCustomMessageHandler extends TestSocketInputHandler.CustomMessageHandler {
    @Override
    public void process() throws IOException {
      info.semaphore.up();
    }
  }
}
