package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.abc.AbcExtractor;
import com.intellij.flex.uiDesigner.abc.AbcNameFilterByNameSetAndStartsWith;
import com.intellij.flex.uiDesigner.abc.BufferWrapper;
import com.intellij.flex.uiDesigner.libraries.FlexOverloadedClasses;
import com.intellij.openapi.util.text.StringUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ComplementSwfBuilder {
  private ComplementSwfBuilder() {
  }

  public static void main(String[] args) throws IOException {
    if (args.length == 1) {
      // build all
      build(args[0], "4.1");
      build(args[0], "4.5");
    }
    else if (args.length == 2) {
      build(args[0], args[1]);
    }
    else {
      throw new IllegalArgumentException("Usage: ComplementSwfBuilder <folder> [<flexVersion>]");
    }
  }

  private static void build(String rootPath, String flexVersion) throws IOException {
    final Collection<CharSequence> commonDefinitions = new ArrayList<CharSequence>(1);
    commonDefinitions.add("com.intellij.flex.uiDesigner:SpecialClassForAdobeEngineers");

    List<BufferWrapper> list = new AbcExtractor().extract(getSourceFile(rootPath, flexVersion),
      new AbcNameFilterByNameSetAndStartsWith(commonDefinitions, new String[]{"mx.", "spark."}) {
        @Override
        public boolean value(CharSequence name) {
          return StringUtil.equals(name, FlexOverloadedClasses.STYLE_PROTO_CHAIN) ||
                 StringUtil.equals(name, FlexOverloadedClasses.SKINNABLE_COMPONENT) ||
                 FlexOverloadedClasses.MX_CLASSES.contains(name) ||
                 FlexOverloadedClasses.AIR_SPARK_CLASSES.contains(name) ||
                 super.value(name);
        }
      });

    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(createAbcFile(rootPath, flexVersion))));
    try {
      out.writeShort(list.size());
      for (BufferWrapper buffer : list) {
        out.writeInt(buffer.getSize());
        buffer.writeTo(out);
      }
    }
    finally {
      out.close();
    }
  }

  public static File getSourceFile(String folder, String flexVersion) {
    return new File(folder, "flex-injection-" + flexVersion + "-1.0-SNAPSHOT.swf");
  }

  public static File createAbcFile(String directory, String flexVersion) {
    return new File(directory, generateInjectionName(flexVersion));
  }

  public static String generateInjectionName(String flexSdkVersion) {
    return "flex-injection-" + flexSdkVersion + ".swc";
  }
}