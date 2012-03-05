package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.util.concurrency.Semaphore;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.flex.uiDesigner.DocumentFactoryManager.DocumentInfo;

@Flex(version="4.5")
public class AppTest extends AppTestBase {
  private static final int APP_TEST_CLASS_ID = 3;

  private final Semaphore semaphore = new Semaphore();
  private final AtomicBoolean fail = new AtomicBoolean();

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

  private void callClientAssert(VirtualFile file, String methodName) throws IOException, InterruptedException {
    semaphore.down();
    ActionCallback callback = client.test(null, DocumentFactoryManager.getInstance().getId(file), getTestName(false), APP_TEST_CLASS_ID);
    callback.doWhenProcessed(new Runnable() {
      @Override
      public void run() {
        semaphore.up();
      }
    });
    await();
  }

  public void _testCloseAndOpenProject() throws Exception {
    File temp = createTempDirectory();
    final Project alienProject = createProject(new File(temp, "t.ipr"), DebugUtil.currentStackTrace());
    assertTrue(ProjectManagerEx.getInstanceEx().openProject(alienProject));

    openAndWait(configureByFiles("injectedAS/Transitions.mxml")[0], "injectedAS/Transitions.mxml");
  }

  private void renderAndWait(VirtualFile file) throws InterruptedException {
    semaphore.down();
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
    renderAndWait(files[0]);

    insertString(files[0], 166, "A");
    callClientAssert(files[0], getTestName(false));

    renderAndWait(files[1]);

    insertString(files[1], 191, "A");
    callClientAssert(files[0], "UpdateDocumentOnIdeaAutoSave2");
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
    protected void processCommand(int command) throws IOException {
      boolean result = true;
      try {
        super.processCommand(command);
      }
      catch (Throwable e) {
        //noinspection InstanceofCatchParameter
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
    }
  }
}
