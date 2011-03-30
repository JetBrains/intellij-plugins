package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.AmfOutputStream;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.xml.XmlFile;
import js.JSTestOption;
import js.JSTestOptions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AppTest extends AppTestBase {
  private static CountDownLatch lock = new CountDownLatch(1);

  @JSTestOptions({JSTestOption.WithGumboSdk, JSTestOption.WithFlexSdk})
  @Flex(version="4.5")
  public void testCloseAndOpenProject() throws Exception {
    changeServiceImplementation(SocketInputHandler.class, MySocketInputHandler.class);
            
    configureByFiles(null, LocalFileSystem.getInstance().findFileByPath(getTestPath() + "/injectedAS/Transitions.mxml"));
    ApplicationManager.getApplication().getMessageBus().connect().subscribe(FlexUIDesignerApplicationManager.MESSAGE_TOPIC, new FlexUIDesignerApplicationListener() {
      @Override
      public void initialDocumentOpened() {
        lock.countDown();
      }

      @Override
      public void applicationClosed() {
      }
    });

    copySwfAndDescriptor(new File(PathManager.getSystemPath(), "flexUIDesigner"));
    FlexUIDesignerApplicationManager designerAppManager = FlexUIDesignerApplicationManager.getInstance();
    designerAppManager.openDocument(myProject, myModule, (XmlFile) myFile, false);
    assertTrue(lock.await(10, TimeUnit.SECONDS));
    
    designerAppManager.projectManagerListener.projectClosed(myProject);

    lock = new CountDownLatch(1);
    AmfOutputStream out = designerAppManager.getClient().getOutput();
    out.write(1);
    out.writeAmfUtf("close", false);
    out.write(3);
    designerAppManager.getClient().flush();
    
    assertTrue(lock.await(10, TimeUnit.SECONDS));
  }
  
  @Override
  protected void tearDown() throws Exception {
    FlexUIDesignerApplicationManager.getInstance().dispose();
    
    super.tearDown();
  }
  
  private static class MySocketInputHandler extends SocketInputHandlerImpl {
    @Override
    public void read(InputStream inputStream) throws IOException {
      try {
        createReader(inputStream);
        boolean result = reader.readBoolean();
        if (result) {
          System.out.print("App passed");
        }
        else {
          fail(reader.readUTF());
        }
      }
      finally {
        lock.countDown();
      }
    }
  }
}
