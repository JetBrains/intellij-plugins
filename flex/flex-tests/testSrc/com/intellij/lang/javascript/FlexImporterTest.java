package com.intellij.lang.javascript;

import com.intellij.lang.javascript.flex.importer.FlexImporter;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import junit.framework.TestCase;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @by Maxim.Mossienko
 */
@SuppressWarnings({"ALL"})
public class FlexImporterTest extends TestCase {
  protected static final String BASE_PATH = "/flex_importer/";

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
      final ZipFile zipFile = new ZipFile(file);
      final ZipEntry zipEntry = zipFile.getEntry("library.swf");
      final InputStream inputStream = zipFile.getInputStream(zipEntry);
      contents = FileUtil.loadBytes(inputStream, (int)zipEntry.getSize());
      inputStream.close();
      zipFile.close();
    } else {
       contents = FileUtil.loadFileBytes(file);
    }

    String result = FlexImporter.buildInterfaceFromStream(new ByteArrayInputStream(contents));
    String resultFileName = getTestDataPath() + fileName + ".txt";

    try {
      String expected =
        StringUtil.convertLineSeparators(FileUtil.loadFile(new File(resultFileName), CharsetToolkit.UTF8_CHARSET));

      assertEquals("interface stubs do not match",expected,result);

      result = FlexImporter.dumpContentsFromStream(new ByteArrayInputStream(contents), getName().equals("testAbc"));
      resultFileName = getTestDataPath() + fileName + ".il";
      expected =
        StringUtil.convertLineSeparators(FileUtil.loadFile(new File(resultFileName), CharsetToolkit.UTF8_CHARSET));

      assertEquals(expected,result);
    } catch (IOException ex) {
      if (!new File(resultFileName).exists()) {
        FileOutputStream fileOutputStream = new FileOutputStream(resultFileName);
        fileOutputStream.write(result.getBytes(CharsetToolkit.UTF8_CHARSET));
        fileOutputStream.close();
        System.out.println("File " + resultFileName + " was created");
        throw ex;
      }
    }
  }

  protected String getTestDataPath() {
    return JSTestUtils.getTestDataPath() + BASE_PATH;
  }
}
