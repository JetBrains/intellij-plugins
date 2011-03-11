package com.intellij.flex.uiDesigner;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.sun.deploy.xml.XMLAttribute;
import js.JSTestOptions;
import org.flyti.roboflest.Roboflest;
import org.flyti.roboflest.Roboflest.Assert;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static js.JSTestOption.WithFlexSdk;
import static js.JSTestOption.WithGumboSdk;

public class UITest extends MxmlWriterTestBase {
  private Roboflest roboflest = new Roboflest();
  
  @Override
  protected void modifySdk(Sdk sdk, SdkModificator sdkModificator) {
    super.modifySdk(sdk, sdkModificator);
    
    sdkModificator.addRoot(LocalFileSystem.getInstance().findFileByPath(flexSdkRootPath + "/src"), OrderRootType.SOURCES);
  }
  
  @JSTestOptions({WithGumboSdk, WithFlexSdk})
  @Flex(version="4.5")
  public void testStyleNavigation() throws Exception {
    testFile("Form.mxml", new Tester() {
      @Override
      public void test(VirtualFile file, XmlFile xmlFile, VirtualFile originalFile) throws Exception {
        client.openDocument(myModule, xmlFile);
        client.test(getTestName(true), 5);

        assertResult(getTestName(true), -1);

        roboflest.setStageOffset(reader);
        assertTrue(reader.readBoolean());

        interact(new Assert() {
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
    });
  }

  private void interact(final Assert... asserts) throws Exception {
    ApplicationManager.getApplication().executeOnPooledThread(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        roboflest.test(new File(getTestDataPath() + "/roboflest/" + getTestName(true) + ".txt"), asserts);
        return null;
      }
    }).get(10, TimeUnit.SECONDS);
  }
}
