package com.intellij.flex.uiDesigner;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import js.JSTestOptions;

import java.io.IOException;

import static js.JSTestOption.WithFlexSdk;
import static js.JSTestOption.WithGumboSdk;

public class UITest extends MxmlWriterTestBase {
  @JSTestOptions({WithGumboSdk, WithFlexSdk})
  @Flex(version="4.5")
  public void test41() throws Exception {
    testFile("Form.mxml", new Tester() {
      @Override
      public void test(VirtualFile file, XmlFile xmlFile, VirtualFile originalFile) throws IOException {
        client.openDocument(myModule, xmlFile);
      }
    });
  }
}
