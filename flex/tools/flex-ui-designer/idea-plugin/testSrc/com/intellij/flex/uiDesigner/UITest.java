package com.intellij.flex.uiDesigner;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import js.JSTestOptions;
import org.flyti.roboflest.Roboflest;
import org.flyti.roboflest.Roboflest.Assert;

import java.io.File;

import static js.JSTestOption.WithFlexSdk;
import static js.JSTestOption.WithGumboSdk;

public class UITest extends MxmlWriterTestBase {
  private Roboflest roboflest = new Roboflest();
  
  @Override
  protected String getBasePath() {
    return getName().equals("testStyleNavigationToSkinClass") ? "/css" : super.getBasePath();
  }
  
  @Override
  protected void modifySdk(Sdk sdk, SdkModificator sdkModificator) {
    super.modifySdk(sdk, sdkModificator);
    
    sdkModificator.addRoot(LocalFileSystem.getInstance().findFileByPath(flexSdkRootPath + "/src"), OrderRootType.SOURCES);
  }
  
  @JSTestOptions({WithGumboSdk, WithFlexSdk})
  @Flex(version="4.5")
  public void testStyleNavigationToExternal() throws Exception {
    testFile(new Tester() {
      @Override
      public void test(VirtualFile file, XmlFile xmlFile, VirtualFile originalFile) throws Exception {
        client.openDocument(myModule, xmlFile);
        client.test(getTestName(true), 5);

        assertResult(getTestName(true), -1);

        roboflest.setStageOffset(reader);
        assertTrue(reader.readBoolean());

        interact("styleNavigation", new Assert() {
          @Override
          public void test() throws Exception {
            assertEquals(ServerMethod.resolveExternalInlineStyleDeclarationSource, reader.read());
            assertEquals(myModule, client.getModule(reader.readInt()));

            XmlAttribute attribute = (XmlAttribute) new ResolveExternalInlineStyleSourceAction(reader, myModule).find();
            assertEquals("spark.skins.spark.ButtonBarLastButtonSkin", attribute.getDisplayValue());
            assertEquals(2186, attribute.getTextOffset());
          }
        });
      }
    }, "Form.mxml");
  }
  
  @JSTestOptions({WithGumboSdk, WithFlexSdk})
  @Flex(version="4.5")
  public void testStyleNavigationToSkinClass() throws Exception {
    testFile(new Tester() {
      @Override
      public void test(final VirtualFile file, XmlFile xmlFile, VirtualFile originalFile) throws Exception {
        client.openDocument(myModule, xmlFile);
        client.test(getTestName(true), 5);

        assertResult(getTestName(true), -1);

        roboflest.setStageOffset(reader);
        assertTrue(reader.readBoolean());

        interact("styleNavigation", new Assert() {
          @Override
          public void test() throws Exception {
            assertEquals(ServerMethod.openFile, reader.read());
            assertEquals(myModule, client.getModule(reader.readInt()));
            assertEquals(file.getUrl(), reader.readUTF());
            assertEquals(96, reader.readInt());
          }
        });
      }
    }, "ComponentWithCustomSkin.mxml", "CustomSkin.mxml");
  }

  @SuppressWarnings({"UnusedDeclaration"})
  private void interact(final Assert... asserts) throws Exception {
    interact(getTestName(true), asserts);
  }
  
  private void interact(String scriptName, final Assert... asserts) throws Exception {
    roboflest.test(new File(getTestDataPath() + "/roboflest/" + scriptName + ".txt"), asserts);
  }
}
