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

import java.io.File;
import java.io.IOException;

import static com.intellij.flex.uiDesigner.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Flex(version="4.5")
public class UITest extends MxmlWriterTestBase {
  private static final int TEST_CLASS_ID = 5;

  private final Roboflest roboflest = new Roboflest();
  
  @Override
  protected String getBasePath() {
    return getName().equals("testStyleNavigationToSkinClass") ? "/css" : super.getBasePath();
  }
  
  @Override
  protected void modifySdk(Sdk sdk, SdkModificator sdkModificator) {
    super.modifySdk(sdk, sdkModificator);
    
    sdkModificator.addRoot(LocalFileSystem.getInstance().findFileByPath(flexSdkRootPath + "/src"), OrderRootType.SOURCES);
  }

  private void init(XmlFile xmlFile) throws IOException {
    client.openDocument(myModule, xmlFile);
    client.test("getStageOffset", TEST_CLASS_ID);

    roboflest.setStageOffset(reader);
  }

  private void assertClient() throws IOException {
    assertClient(getTestName(false));
  }

  private void assertClient(String methodName) throws IOException {
    client.test(methodName, TEST_CLASS_ID);
    assertResult(methodName, -1);
  }

  public void testStyleNavigationToExternal() throws Exception {
    testFile(new MyTester() {
      @Override
      public void test(final VirtualFile file) throws Exception {
        interact("styleNavigation", new Assert() {
          @Override
          public void test() throws Exception {
            assertThat(reader.read(), equalTo(ServerMethod.resolveExternalInlineStyleDeclarationSource));
            assertThat(client.getModule(reader.readUnsignedShort()), equalTo(myModule));

            XmlAttribute attribute = (XmlAttribute) new ResolveExternalInlineStyleSourceAction(reader, myModule).find();
            assertThat(attribute.getDisplayValue(), "spark.skins.spark.ButtonBarLastButtonSkin");
            assertThat(attribute.getTextOffset(), 2186);
          }
        });
      }
    }, "Form.mxml");
  }

  public void testStyleNavigationToSkinClass() throws Exception {
    testFile(new MyTester() {
      @Override
      public void test(final VirtualFile file) throws Exception {
        interact("styleNavigation", new Assert() {
          @Override
          public void test() throws Exception {
            assertThat(reader.read(), equalTo(ServerMethod.openFile));
            assertMyProject();
            assertThat(reader.readUTF(), file.getUrl());
            assertThat(reader.readInt(), 96);
          }
        });
      }
    }, "ComponentWithCustomSkin.mxml", "CustomSkin.mxml");
  }

  private void assertMyProject() throws IOException {
    assertThat(client.getProject(reader.readUnsignedShort()), equalTo(myProject));
  }

  public void testCloseDocument() throws Exception {
    testFile(new MyTester() {
      @Override
      public void test(final VirtualFile file) throws Exception {
        interact("closeDocument", new Assert() {
          @Override
          public void test() throws Exception {
            assertThat(reader.read(), ServerMethod.unregisterDocumentFactories);
            assertMyProject();
            assertThat(reader.readIntArray(), 0);

            assertNotAvailable();

            assertClient();
          }
        });
      }
    }, "Embed.mxml");
  }

  @SuppressWarnings({"UnusedDeclaration"})
  private void interact(final Assert... asserts) throws Exception {
    interact(getTestName(true), asserts);
  }
  
  private void interact(String scriptName, final Assert... asserts) throws Exception {
    roboflest.test(new File(getTestDataPath() + "/roboflest/" + scriptName + ".txt"), asserts);
  }

  private abstract class MyTester implements Tester {
    @Override
    public final void test(VirtualFile file, XmlFile xmlFile, VirtualFile originalFile) throws Exception {
      init(xmlFile);
      test(file);
      assertNotAvailable();
    }

    protected abstract void test(final VirtualFile file) throws Exception;

    protected void assertNotAvailable() throws InterruptedException, IOException {
      Thread.sleep(50); // wait data
      assertThat(reader.available(), 0);
    }
  }
}
