package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.flex.uiDesigner.DocumentFactoryManager.DocumentInfo;

@Flex(version="4.5")
public class AppTest extends AppTestBase {
  private static final int APP_TEST_CLASS_ID = 3;

  private final Semaphore semaphore = new Semaphore();
  private final AtomicBoolean fail = new AtomicBoolean();
  private Callable<Void> assertOnDocumentRendered;

  @Override
  protected void changeServicesImplementation() {
    Tests.changeDesignerServiceImplementation(SocketInputHandler.class, MySocketInputHandler.class);
    Tests.changeDesignerServiceImplementation(Client.class, TestClient.class);
  }

  @Override
  protected void applicationLaunchedAndInitialized() {
    ((MySocketInputHandler)SocketInputHandler.getInstance()).fail = fail;
    ((MySocketInputHandler)SocketInputHandler.getInstance()).semaphore = semaphore;

    MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect(myModule);
    connection.subscribe(SocketInputHandler.MESSAGE_TOPIC, new SocketInputHandler.DocumentRenderedListener() {
      @Override
      public void documentRenderedOnAutoSave(DocumentInfo info) {
        //assertTrue(info.getElement().equals(file));
        if (assertOnDocumentRendered != null) {
          try {
            assertOnDocumentRendered.call();
          }
          catch (Exception e) {
            fail.set(true);
            throw new AssertionError(e);
          }
        }

        semaphore.up();
      }

      @Override
      public void errorOccured() {
        fail.set(true);
        semaphore.up();
      }
    });
  }

  private void await() throws InterruptedException {
    semaphore.waitForUnsafe();
    if (fail.get()) {
      fail();
    }
  }
  
  private void callClientAssert(String methodName) throws IOException, InterruptedException {
    semaphore.down();
    client.test(methodName.equals("close") ? null : myModule, methodName, APP_TEST_CLASS_ID);
    await();
    fail.set(false);
  }

  // todo actually, test only close project, but not test open after close
  public void _testCloseAndOpenProject() throws Exception {
    openAndWait(configureByFiles("injectedAS/Transitions.mxml")[0], "injectedAS/Transitions.mxml");
  }

  private void renderAndWait(VirtualFile file) throws InterruptedException {
    AsyncResult<DocumentInfo> result =
      DesignerApplicationManager.getInstance().renderDocument(myModule, Tests.virtualToPsi(myProject, file));

    result.doWhenProcessed(new Runnable() {
      @Override
      public void run() {
        semaphore.up();
      }
    });

    await();
  }

  public void testUpdateDocumentOnIdeaAutoSave() throws Exception {
    final VirtualFile[] files = configureByFiles("ProjectMxmlComponentAsChild.mxml", "AuxProjectMxmlComponent.mxml");

    DesignerApplicationManager designerManager = DesignerApplicationManager.getInstance();

    semaphore.down();
    renderAndWait(files[0]);

    assertOnDocumentRendered = new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        client.test(myModule, getTestName(false), APP_TEST_CLASS_ID);
        return null;
      }
    };
    insertString(files[0], 166, "A");

    assertOnDocumentRendered = null;
    designerManager.renderDocument(myModule, Tests.virtualToPsi(myProject, files[1]));
    await();

    assertOnDocumentRendered = new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        client.test(myModule, "UpdateDocumentOnIdeaAutoSave2", APP_TEST_CLASS_ID);
        return null;
      }
    };
    insertString(files[1], 191, "A");
  }

  private void insertString(VirtualFile file, int offset, @NotNull CharSequence s) throws InterruptedException {
    final Document document = FileDocumentManager.getInstance().getDocument(file);
    assertNotNull(document);
    AccessToken token = WriteAction.start();
    try {
      document.insertString(offset, s);
    }
    finally {
      token.finish();
    }

    semaphore.down();
    PsiDocumentManager.getInstance(myProject).commitAllDocuments();
    FileDocumentManager.getInstance().saveAllDocuments();
    await();
  }

  private void openAndWait(VirtualFile file, @Nullable String relativePath) throws InterruptedException, IOException {
    semaphore.down();

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
    private AtomicBoolean fail;
    private Semaphore semaphore;

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
          result = false;
          fail.set(true);
          try {
            LOG.error(e);
          }
          catch (AssertionError ignored) {
          }
        }
      }
      finally {
        if (!result) {
          semaphore.up();
        }
      }

      return true;
    }
  }
}
