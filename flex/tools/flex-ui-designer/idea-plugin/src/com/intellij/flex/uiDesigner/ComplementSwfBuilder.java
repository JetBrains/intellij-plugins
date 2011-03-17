package com.intellij.flex.uiDesigner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ComplementSwfBuilder {
  public static void build(File source, File abcInjection, String rootPath, String flexVersion) throws IOException {
    final AbcNameFilter sparkInclusionNameFilter = new AbcNameFilterStartsWith("com.intellij.flex.uiDesigner.flex", true);
    final Collection<String> airsparkDefinitions = new ArrayList<String>(1);
    airsparkDefinitions.add("spark.components:WindowedApplication");

    if (abcInjection != null) {
      new AbcFilter().filter(source, abcInjection, new AbcNameFilterByNameSetAndStartsWith(Collections.<CharSequence>emptyList(), new String[]{"mx.", "spark."}) {
        @Override
        public boolean accept(String name) {
          return name.equals("mx.styles:StyleProtoChain") || name.equals("mx.styles:StyleManager") || (super.accept(name) && !sparkInclusionNameFilter.accept(name) && !airsparkDefinitions.contains(name));
        }
      });
    }

    new AbcFilter().filter(source, new File(rootPath + "/complement-flex" + flexVersion + ".swf"), sparkInclusionNameFilter);
    new AbcFilter().filter(source, new File(rootPath + "/complement-air4.swf"), new AbcNameFilter() {
      @Override
      public boolean accept(String name) {
        return airsparkDefinitions.contains(name);
      }
    });
  }
}