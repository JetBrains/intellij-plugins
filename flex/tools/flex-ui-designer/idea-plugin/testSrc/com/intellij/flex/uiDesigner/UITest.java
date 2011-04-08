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

import static org.hamcrest.MatcherAssert.assertThat;
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
  
  public void testStyleNavigationToExternal() throws Exception {
    testFile(new Tester() {
      @Override
      public void test(VirtualFile file, XmlFile xmlFile, VirtualFile originalFile) throws Exception {
        init(xmlFile);

        interact("styleNavigation", new Assert() {
          @Override
          public void test() throws Exception {
            assertThat(reader.read(), equalTo(ServerMethod.resolveExternalInlineStyleDeclarationSource));
            assertThat(client.getModule(reader.readUnsignedShort()), equalTo(myModule));

            XmlAttribute attribute = (XmlAttribute) new ResolveExternalInlineStyleSourceAction(reader, myModule).find();
            assertThat(attribute.getDisplayValue(), equalTo("spark.skins.spark.ButtonBarLastButtonSkin"));
            assertThat(attribute.getTextOffset(), equalTo(2186));
          }
        });
      }
    }, "Form.mxml");
  }
  
  public void testStyleNavigationToSkinClass() throws Exception {
    testFile(new Tester() {
      @Override
      public void test(final VirtualFile file, XmlFile xmlFile, VirtualFile originalFile) throws Exception {
        init(xmlFile);

        interact("styleNavigation", new Assert() {
          @Override
          public void test() throws Exception {
            assertThat(reader.read(), equalTo(ServerMethod.openFile));
            assertThat(client.getProject(reader.readUnsignedShort()), equalTo(myProject));
            assertThat(reader.readUTF(), equalTo(file.getUrl()));
            assertThat(reader.readInt(), equalTo(96));
          }
        });
      }
    }, "ComponentWithCustomSkin.mxml", "CustomSkin.mxml");
  }

  public void _testCloseDocument() throws Exception {
    testFile(new Tester() {
      @Override
      public void test(final VirtualFile file, XmlFile xmlFile, VirtualFile originalFile) throws Exception {
        client.openDocument(myModule, xmlFile);
        client.test(getTestName(true), TEST_CLASS_ID);

        assertResult(getTestName(true), -1);

        roboflest.setStageOffset(reader);
        assertTrue(reader.readBoolean());

        interact("styleNavigation", new Assert() {
          @Override
          public void test() throws Exception {
            assertThat(reader.read(), equalTo(ServerMethod.openFile));
            assertThat(client.getProject(reader.readUnsignedShort()), equalTo(myProject));
            assertThat(reader.readUTF(), equalTo(file.getUrl()));
            assertThat(reader.readUTF(), equalTo(file.getUrl()));
            assertThat(reader.readInt(), equalTo(96));
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
}
