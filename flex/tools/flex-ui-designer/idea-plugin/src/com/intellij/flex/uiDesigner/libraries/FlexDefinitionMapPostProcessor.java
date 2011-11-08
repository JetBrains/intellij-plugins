package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.ComplementSwfBuilder;
import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Pass;
import com.intellij.openapi.util.io.FileUtil;
import gnu.trove.THashMap;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

import static com.intellij.flex.uiDesigner.libraries.FlexLibsNames.*;

class FlexDefinitionMapPostProcessor extends Pass<THashMap<CharSequence, Definition>> {
  private final String version;

  FlexDefinitionMapPostProcessor(String version) {
    this.version = version;
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
  public void pass(THashMap<CharSequence, Definition> definitionMap) {
    for (Pair<String, String> pair : FLEX_LIBS_PATTERNS) {
      if (pair.first.equals(AIRSPARK)) {
        // todo replace air spark definitions
        //set = createSet(FlexOverloadedClasses.AIR_SPARK_CLASSES.size() + 1);
        //set.addAll(FlexOverloadedClasses.AIR_SPARK_CLASSES);
      }

      definitionMap.remove(pair.second);
    }

    try {
      inject(definitionMap);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void inject(THashMap<CharSequence, Definition> definitionMap) throws IOException {
    final byte[] injectionData = FileUtil
      .loadFileBytes(ComplementSwfBuilder.createAbcFile(DebugPathManager.getFudHome() + "/flex-injection/target", version));

    final DataInputStream in = new DataInputStream(new ByteArrayInputStream(injectionData));
    short size = in.readShort();
    definitionMap.ensureCapacity(size);


    in.readShort();
  }
}