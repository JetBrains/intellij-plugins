package com.intellij.flex.uiDesigner.libraries;

import com.google.common.base.Charsets;
import com.intellij.flex.uiDesigner.ComplementSwfBuilder;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xml.NanoXmlUtil;
import gnu.trove.THashMap;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.intellij.flex.uiDesigner.libraries.FlexLibsNames.*;

class FlexDefinitionMapProcessor implements DefinitionMapProcessor {
  private final String version;
  private final Condition<String> globalContains;

  private final Condition<String> isExternal;

  FlexDefinitionMapProcessor(String version, Condition<String> globalContains) {
    this.version = version;
    this.globalContains = globalContains;

    isExternal = new Condition<String>() {
      @Override
      public boolean value(String name) {
        return FlexDefinitionMapProcessor.this.globalContains.value(name) || name.equals("mx.styles:FtyleProtoChain") || name.equals("spark.components.supportClasses:FkinnableComponent");
      }
    };
  }

  @SuppressWarnings("unchecked")
  final static Pair<String, String>[] FLEX_LIBS_PATTERNS = new Pair[]{
    new Pair<String, String>(FRAMEWORK, "FrameworkClasses"),
    new Pair<String, String>(AIRFRAMEWORK, "AIRFrameworkClasses"),
    new Pair<String, String>(SPARK, "SparkClasses"),
    new Pair<String, String>(AIRSPARK, "AIRSparkClasses"),

    new Pair<String, String>(MX, "MxClasses"),
    new Pair<String, String>(RPC, "RPCClasses"),
    new Pair<String, String>(MOBILECOMPONENTS, "MobileComponentsClasses")};

  @Override
  public void process(THashMap<CharSequence, Definition> definitionMap, AbcMerger abcMerger) {
    for (Pair<String, String> pair : FLEX_LIBS_PATTERNS) {
      definitionMap.remove(pair.second);
    }

    try {
      inject(definitionMap, abcMerger);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void inject(THashMap<CharSequence, Definition> definitionMap, AbcMerger abcMerger) throws IOException {
    final MyZipInputStream zipIn;
    if (DebugPathManager.IS_DEV) {
      zipIn = new MyZipInputStream(new FileInputStream(ComplementSwfBuilder.createAbcFile(DebugPathManager.getFudHome() + "/flex-injection/target", version)));
    }
    else {
      zipIn = new MyZipInputStream(getClass().getClassLoader().getResource(ComplementSwfBuilder.generateInjectionName(version)).openStream());
    }

    Reader catalogReader = null;
    ByteArrayInputStream swfIn = null;
    try {
      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null) {
        if (entry.getName().equals("catalog.xml")) {
          catalogReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(FileUtil.loadBytes(zipIn)), Charsets.UTF_8));
        }
        else if (entry.getName().equals("library.swf")) {
          swfIn = new ByteArrayInputStream(FileUtil.loadBytes(zipIn));
        }
      }
    }
    finally {
      zipIn.ignoreClose = false;
      zipIn.close();
    }

    assert catalogReader != null;
    assert swfIn != null;
    NanoXmlUtil.parse(catalogReader, new CatalogXmlBuilder(definitionMap, isExternal));
    abcMerger.process(swfIn, swfIn.available());
  }

  private static class MyZipInputStream extends ZipInputStream {
    private boolean ignoreClose = true;

    public MyZipInputStream(InputStream in) {
      super(in);
    }

    @Override
    public void close() throws IOException {
      if (!ignoreClose) {
        super.close();
      }
    }
  }
}