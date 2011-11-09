package com.intellij.flex.uiDesigner.libraries;

import com.google.common.base.Charsets;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.xml.NanoXmlUtil;
import gnu.trove.THashMap;

import java.io.*;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.intellij.flex.uiDesigner.libraries.FlexLibsNames.*;

class FlexDefinitionMapProcessor implements DefinitionMapProcessor {
  private final String version;
  private final Condition<String> globalContains;

  FlexDefinitionMapProcessor(String version, Condition<String> globalContains) {
    this.version = version;
    this.globalContains = globalContains;
  }

  @SuppressWarnings("unchecked")
  final static Pair<String, String>[] FLEX_LIBS_PATTERNS = new Pair[]{
    new Pair<String, String>(FRAMEWORK, "FrameworkClasses"),
    new Pair<String, String>(AIRFRAMEWORK, "AIRFrameworkClasses"),
    new Pair<String, String>(SPARK, "SparkClasses"),
    new Pair<String, String>(AIRSPARK, "AIRSparkClasses"),

    new Pair<String, String>(MX, "MxClasses"),
    new Pair<String, String>(RPC, "RPCClasses"),
    new Pair<String, String>(MOBILECOMPONENTS, "MobileComponentsClasses"),
    new Pair<String, String>("charts", "ChartsClasses"),
    new Pair<String, String>("sparkskins", "SparkSkinsClasses")};

  @Override
  public void process(THashMap<CharSequence, Definition> definitionMap, AbcMerger abcMerger) throws IOException {
    for (Pair<String, String> pair : FLEX_LIBS_PATTERNS) {
      definitionMap.remove(pair.second);
    }

    abcMerger.setDefinitionProcessor(null);
    inject(definitionMap, abcMerger);
  }

  public static File createAbcFile(String directory, String flexVersion) {
    return new File(directory, generateInjectionName(flexVersion));
  }

  public static String generateInjectionName(String flexSdkVersion) {
    return "flex-injection-" + flexSdkVersion + ".swc";
  }

  private void inject(THashMap<CharSequence, Definition> definitionMap, AbcMerger abcMerger) throws IOException {
    final MyZipInputStream zipIn;
    if (DebugPathManager.IS_DEV) {
      zipIn = new MyZipInputStream(new FileInputStream(createAbcFile(DebugPathManager.getFudHome() + "/flex-injection/target", version)));
    }
    else {
      zipIn = new MyZipInputStream(getClass().getClassLoader().getResource(generateInjectionName(version)).openStream());
    }

    MyCharArrayReader catalogReader = null;
    ByteArrayInputStream swfIn = null;
    try {
      ZipEntry entry;
      while ((entry = zipIn.getNextEntry()) != null) {
        if (entry.getName().equals("catalog.xml")) {
          final InputStreamReader reader = new InputStreamReader(zipIn, Charsets.UTF_8);
          try {
            catalogReader = new MyCharArrayReader(FileUtil.adaptiveLoadText(reader));
          }
          finally {
            reader.close();
          }
        }
        else if (entry.getName().equals("library.swf")) {
          swfIn = new ByteArrayInputStream(FileUtil.adaptiveLoadBytes(zipIn));
        }
      }
    }
    finally {
      zipIn.ignoreClose = false;
      zipIn.close();
    }

    assert catalogReader != null;
    assert swfIn != null;

    final Set<CharSequence> ownDefinitions = LibrarySorter.getDefinitions(catalogReader);
    NanoXmlUtil.parse(catalogReader, new CatalogXmlBuilder(definitionMap, new Condition<String>() {
      @Override
      public boolean value(String name) {
        return globalContains.value(name) || (name.startsWith("com.intellij.") && !ownDefinitions.contains(name));
      }
    }, new Condition<String>() {
      @Override
      public boolean value(String name) {
        return globalContains.value(name) || name.equals("mx.styles:FtyleProtoChain") ||
               name.equals("spark.components.supportClasses:FkinnableComponent");
      }
    }
    ));
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

  private static class MyCharArrayReader extends CharArrayReader {
    public MyCharArrayReader(char[] buf) {
      super(buf);
    }

    @Override
    public void close() {
      pos = 0;
    }
  }
}