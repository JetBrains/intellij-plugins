package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.FlexUIDesignerBaseTestCase;
import com.intellij.flex.uiDesigner.abc.AbcTranscoder;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.util.xml.NanoXmlUtil;
import gnu.trove.THashMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class AbcMergerTest {
  private File out;

  @Before
  public void runBeforeEveryTest() throws Exception {
    out = File.createTempFile("abc_", ".swf");
  }

  @After
  public void runAfterEveryTest() {
    //noinspection ResultOfMethodCallIgnored
    out.delete();
  }

  @Test
  public void merge() throws IOException {
    final THashMap<CharSequence, Definition> definitionMap = new THashMap<CharSequence, Definition>(AbcTranscoder.HASHING_STRATEGY);
    final Set<CharSequence> globalDefinitions = LibraryUtil.getDefinitions(LibraryUtil.getTestGlobalLibrary(false));

    final AbcMerger abcMerger = new AbcMerger(definitionMap, out, null);
    final Pair<CharArrayReader, ByteArrayInputStream> data =
      LibraryUtil.openSwc(new File(FlexUIDesignerBaseTestCase.getTestDataPath(), "lib/MinimalComps_0_9_10.swc"));

    NanoXmlUtil.parse(data.first, new CatalogXmlBuilder(definitionMap, new Condition<String>() {
      @Override
      public boolean value(String name) {
        return globalDefinitions.contains(name);
      }
    }));

    abcMerger.process(data.second);
  }
}