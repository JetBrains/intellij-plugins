package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.abc.*;
import com.intellij.openapi.util.text.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class ComplementSwfBuilder {
  public static void build(String rootPath, String flexVersion) throws IOException {
    final AbcNameFilter sparkInclusionNameFilter = new AbcNameFilterStartsWith("com.intellij.flex.uiDesigner.flex", true);
    final Collection<CharSequence> airsparkDefinitions = new ArrayList<CharSequence>(1);
    airsparkDefinitions.add("spark.components:WindowedApplication");

    final Collection<CharSequence> commonDefinitions = new ArrayList<CharSequence>(1);
    commonDefinitions.add("com.intellij.flex.uiDesigner:SpecialClassForAdobeEngineers");

    // SpriteLoaderAsset must be loaded with framework.swc, because _s000 located in framework.swc
    AbcNameFilterByNameSetAndStartsWith filter =
      new AbcNameFilterByNameSetAndStartsWith(commonDefinitions, new String[]{"mx.", "spark."}) {
        @Override
        public boolean accept(CharSequence name) {
          return StringUtil.startsWith(name, "com.intellij.flex.uiDesigner.flex:SpriteLoaderAsset") || StringUtil.startsWith(name, FlexSdkAbcInjector.STYLE_PROTO_CHAIN) || StringUtil.startsWith(name, "mx.styles:StyleManager") ||
                 StringUtil.startsWith(name, FlexSdkAbcInjector.LAYOUT_MANAGER) || StringUtil.startsWith(name, FlexSdkAbcInjector.RESOURCE_MANAGER) ||
                 (super.accept(name) && !sparkInclusionNameFilter.accept(name) && !airsparkDefinitions.contains(name));
        }
      };

    File source = getSourceFile(rootPath, flexVersion);
    new AbcFilter(false).filter(source, createAbcFile(rootPath, flexVersion), filter);
    new AbcFilter(false).filter(source, new File(rootPath + "/complement-flex" + flexVersion + ".swf"), sparkInclusionNameFilter);
    new AbcFilter(false).filter(source, new File(rootPath + "/complement-air4.swf"), new AbcNameFilter() {
      @Override
      public boolean accept(CharSequence name) {
        return airsparkDefinitions.contains(name);
      }
    });
  }

  public static File getSourceFile(String folder, String flexVersion) {
    return new File(folder, "flex-injection-" + flexVersion + "-1.0-SNAPSHOT.swf");
  }

  public static File createAbcFile(String folder, String flexVersion) {
    return new File(folder, generateInjectionName(flexVersion));
  }
  
  public static String generateInjectionName(String flexSdkVersion) {
    return "flex-injection-" + flexSdkVersion + ".abc";
  }
}