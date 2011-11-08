package com.intellij.flex.uiDesigner.libraries;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Pass;

import java.util.Map;

import static com.intellij.flex.uiDesigner.libraries.FlexLibsNames.*;

class FlexLibraryDefinitionsPostProcessor extends Pass<Map<CharSequence, Definition>> {
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
  public void pass(Map<CharSequence, Definition> definitionMap) {
    for (Pair<String, String> pair : FLEX_LIBS_PATTERNS) {
      if (pair.first.equals(AIRSPARK)) {
        // todo replace air spark definitions
        //set = createSet(FlexOverloadedClasses.AIR_SPARK_CLASSES.size() + 1);
        //set.addAll(FlexOverloadedClasses.AIR_SPARK_CLASSES);
      }

      definitionMap.remove(pair.second);
    }
  }
}