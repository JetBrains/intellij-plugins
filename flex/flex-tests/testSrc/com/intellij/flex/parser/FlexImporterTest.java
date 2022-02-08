// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.parser;

import com.intellij.flex.util.FlexTestUtils;
import com.intellij.lang.javascript.flex.importer.FlexImporter;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FlexImporterTest extends TestCase {

  public final void testAbc() throws Exception {
    doTestFor("builtin.abc");
  }

  public final void testEmptySwf() throws Exception {
    doTestFor("Assets.swf");
  }

  public final void testHelloWorld() throws Exception {
    doTestFor("HelloWorld.swc");
  }

  public final void testDoNotCreateNsReference() throws Exception {
    doTestFor("debugger-0.2alpha2.swc");
  }

  public final void testSwf() throws Exception {
    doTestFor("employeedirectory.swf");
  }

  public final void testParamNames() throws Exception {
    doTestFor("ParamNames.swc");
  }

  public final void testCustomNsForMethod() throws Exception {
    doTestFor("Lib1.swc");
  }

  public final void testAlchemy() throws Exception {
    doTestFor("Alchemy.swc");
  }

  public final void testGenericSyntax() throws Exception {
    doTestFor("PlayerGlobal10.swc");
  }

  public final void testObfuscated() throws Exception {
    doTestFor("Obfuscated.swc");
  }

  public final void testAirGlobal_1_5() throws Exception {
    doTestFor("airglobal_1_5.swc");
  }

  public final void testDs() throws Exception {
    doTestFor("ds.swc");
  }

  public final void testHotbook() throws Exception {
    doTestFor("hotbook.swf");
  }

  private void doTestFor(final String fileName) throws IOException {
    final File file = new File(getTestDataPath() + fileName);
    final byte[] contents;

    if (file.getName().endsWith(".swc")) {
      try (ZipFile zipFile = new ZipFile(file)) {
        final ZipEntry zipEntry = zipFile.getEntry("library.swf");
        final InputStream inputStream = zipFile.getInputStream(zipEntry);
        contents = FileUtil.loadBytes(inputStream, (int)zipEntry.getSize());
        inputStream.close();
      }
    }
    else {
      contents = FileUtil.loadFileBytes(file);
    }

    String result = FlexImporter.buildInterfaceFromStream(new ByteArrayInputStream(contents));
    String resultFileName = getTestDataPath() + fileName + ".txt";

    try {
      String expected =
        StringUtil.convertLineSeparators(FileUtil.loadFile(new File(resultFileName), StandardCharsets.UTF_8));

      assertEquals("interface stubs do not match", expected, result);

      result = FlexImporter.dumpContentsFromStream(new ByteArrayInputStream(contents), getName().equals("testAbc"));
      resultFileName = getTestDataPath() + fileName + ".il";
      expected =
        StringUtil.convertLineSeparators(FileUtil.loadFile(new File(resultFileName), StandardCharsets.UTF_8));

      assertEquals(expected, result);
    }
    catch (IOException ex) {
      if (!new File(resultFileName).exists()) {
        FileUtil.writeToFile(new File(resultFileName), result);
        System.out.println("File " + resultFileName + " was created");
        throw ex;
      }
    }
  }

  public static String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("flex_importer") + "/";
  }
}
