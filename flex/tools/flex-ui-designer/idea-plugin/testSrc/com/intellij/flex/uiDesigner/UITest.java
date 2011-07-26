package com.intellij.flex.uiDesigner;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import org.flyti.roboflest.Roboflest;
import org.flyti.roboflest.Roboflest.Assert;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static com.intellij.flex.uiDesigner.MatcherAssert.assertThat;
import static com.intellij.flex.uiDesigner.TestSocketInputHandler.MessageHandler;
import static org.hamcrest.Matchers.*;

@Flex(version="4.5")
public class UITest extends MxmlTestBase {
  private static final int UI_TEST_CLASS_ID = 5;

  private static Roboflest roboflest;

  private static SocketInputHandlerImpl.Reader reader;
  
  @Override
  protected String getBasePath() {
    return getName().equals("testStyleNavigationToSkinClass") ? "/css" : super.getBasePath();
  }
  
  @Override
  protected void modifySdk(Sdk sdk, SdkModificator sdkModificator) {
    super.modifySdk(sdk, sdkModificator);
    
    sdkModificator.addRoot(LocalFileSystem.getInstance().findFileByPath(flexSdkRootPath + "/src"), OrderRootType.SOURCES);
  }

  private void init(XmlFile xmlFile) throws IOException, AWTException {
    client.openDocument(myModule, xmlFile);

    if (roboflest == null) {
      roboflest = new Roboflest();
      reader = socketInputHandler.getReader();
      client.test("getStageOffset", UI_TEST_CLASS_ID);
      socketInputHandler.process(new TestSocketInputHandler.CustomMessageHandler() {
        @Override
        public void process() throws IOException {
          roboflest.setStageOffset(reader);
        }
      });
    }
    else {
      client.flush();
    }
  }

  private void assertClient() throws IOException {
    assertClient(getTestName(false));
  }

  private void assertClient(String methodName) throws IOException {
    client.test(methodName, UI_TEST_CLASS_ID);
    socketInputHandler.process();
  }

  public void _testStyleNavigationToExternal() throws Exception {
    testFile(new MyTester("styleNavigation", new UIMessageHandler(ServerMethod.resolveExternalInlineStyleDeclarationSource) {
        @Override
        public void process() throws IOException {
          assertThat(client.getModule(reader.readUnsignedShort()), equalTo(myModule));

          XmlAttribute attribute = (XmlAttribute)new ResolveExternalInlineStyleSourceAction(reader, myModule).find();
          assertThat(attribute.getDisplayValue(), "spark.skins.spark.ButtonBarLastButtonSkin");
          assertThat(attribute.getTextOffset(), 2186);
        }
      }) {
      }, SPARK_COMPONENTS_FILE);
  }

  public void _testStyleNavigationToSkinClass() throws Exception {
    testFile(new MyTester("styleNavigation", new UIMessageHandler(ServerMethod.openFile) {
        @Override
        public void process() throws IOException {
          assertMyProject();
          assertThat(reader.readUTF(), file.getUrl());
          assertThat(reader.readInt(), 96);
        }
      }) {
      }, "ComponentWithCustomSkin.mxml", "CustomSkin.mxml");
  }

  private void assertMyProject() throws IOException {
    assertThat(client.getProject(socketInputHandler.getReader().readUnsignedShort()), equalTo(myProject));
  }

  public void testCloseDocument() throws Exception {
    testFile(new MyTester("closeDocument", new UIMessageHandler(ServerMethod.unregisterDocumentFactories) {
        @Override
        public void process() throws IOException {
          assertMyProject();
          assertThat(reader.readIntArray(), 0);

          assertNotAvailable();

          assertClient();
        }
      }) {
      }, "Embed.mxml");
  }

  protected void assertNotAvailable() throws IOException {
    try {
      Thread.sleep(50); // wait data
    }
    catch (InterruptedException e) {
      fail(e.getMessage());
    }
    
    assertThat(reader.available(), 0);
  }

  @SuppressWarnings({"UnusedDeclaration"})
  private void interact(final Assert... asserts) throws Exception {
    interact(getTestName(true), asserts);
  }
  
  private void interact(String scriptName, final Assert... asserts) throws Exception {
    roboflest.test(new File(getTestDataPath() + "/roboflest/" + scriptName + ".txt"), asserts);
  }

  private abstract class MyTester implements Tester {
    private final String scriptName;
    private final UIMessageHandler messageHandler;

    public MyTester(String scriptName, UIMessageHandler messageHandler) {
      this.scriptName = scriptName;
      this.messageHandler = messageHandler;
    }

    @Override
    public final void test(VirtualFile file, XmlFile xmlFile, VirtualFile originalFile) throws Exception {
      init(xmlFile);
      test(file);
      assertNotAvailable();
    }

    private void test(final VirtualFile file) throws Exception {
      interact(scriptName, new Assert() {
        @Override
        public void test() throws Exception {
          messageHandler.file = file;
          socketInputHandler.process(messageHandler);
        }
      });
    }
  }

  private abstract static class UIMessageHandler implements MessageHandler {
    protected VirtualFile file;
    public final int command;

    public UIMessageHandler(int command) {
      this.command = command;
    }

    @Override
    public final int getExpectedCommand() {
      return command;
    }
  }
}
