package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xml.NanoXmlUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import static com.intellij.flex.uiDesigner.libraries.FlexLibsNames.*;

class FlexDefinitionMapProcessor implements DefinitionMapProcessor {
  private final String version;
  private final Condition<? super String> globalContains;

  FlexDefinitionMapProcessor(String version, Condition<? super String> globalContains) {
    this.version = version;
    this.globalContains = globalContains;
  }

  @SuppressWarnings("unchecked")
  final static Pair<String, String>[] FLEX_LIBS_PATTERNS = new Pair[]{
    Pair.create(FRAMEWORK, "FrameworkClasses"),
    Pair.create(AIRFRAMEWORK, "AIRFrameworkClasses"),
    Pair.create(SPARK, "SparkClasses"),
    Pair.create(AIRSPARK, "AIRSparkClasses"),

    Pair.create("mobile.swc", "MobileThemeClasses"),

    Pair.create(MX, "MxClasses"),
    Pair.create(RPC, "RPCClasses"),
    Pair.create(MOBILECOMPONENTS, "MobileComponentsClasses"),
    Pair.create("charts", "ChartsClasses"),
    Pair.create("sparkskins", "SparkSkinsClasses")};

  @Override
  public void process(THashMap<CharSequence, Definition> definitionMap, AbcMerger abcMerger) throws IOException {
    for (Pair<String, String> pair : FLEX_LIBS_PATTERNS) {
      definitionMap.remove(pair.second);
    }

    definitionMap.remove("SparkDmvClasses");

    abcMerger.setDefinitionProcessor(null);
    inject(definitionMap, abcMerger);
  }

  private void inject(THashMap<CharSequence, Definition> definitionMap, AbcMerger abcMerger) throws IOException {
    final THashSet<String> overloadedMasked = new THashSet<>(FlexDefinitionProcessor.OVERLOADED.length);
    for (String origin : FlexDefinitionProcessor.OVERLOADED) {
      int index = origin.indexOf(':') + 1;
      overloadedMasked.add(origin.substring(0, index) + FlexDefinitionProcessor.OVERLOADED_AND_BACKED_CLASS_MARK + origin.substring(index + 1));
    }

    Pair<CharArrayReader, ByteArrayInputStream> data = getInjection();
    final Set<CharSequence> ownDefinitions = LibraryUtil.getDefinitions(data.first);
    NanoXmlUtil.parse(data.first, new CatalogXmlBuilder(definitionMap, name -> globalContains.value(name) || (name.startsWith("com.intellij.") && !ownDefinitions.contains(name)), name -> globalContains.value(name) || overloadedMasked.contains(name)
    ));
    abcMerger.process(data.second);
  }

  private Pair<CharArrayReader, ByteArrayInputStream> getInjection() throws IOException {
    String injectionName = "flex-injection-" + (StringUtil.compareVersionNumbers(version, "4.6") < 0 ? version : "4.6") + ".swc";
    if (DebugPathManager.IS_DEV) {
      // maven build
      File file = new File(DebugPathManager.getFudHome() + "/flex-injection/target", injectionName);
      if (!file.exists()) {
        // gant build
        file = new File(DebugPathManager.getFudHome(), injectionName);
      }
      if (file.exists()) {
        return LibraryUtil.openSwc(file);
      }
    }

    URL resource = getClass().getClassLoader().getResource(injectionName);
    assert resource != null;
    return LibraryUtil.openSwc(resource.openStream());
  }
}