package com.intellij.flex.uiDesigner.abc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

final class AbcBlankMaker {
  public static void main(String[] args) throws IOException {
    final long time = System.currentTimeMillis();
    //new AbcFilter().filter(new File("abc-blank-maker/src/b/library.swf"), new File("idea-plugin/resources/B.abc"), new AbcNameFilterByEquals("_b000"));
    new AbcFilter().filter(new File("/Developer/SDKs/flex_sdk_4.5.0.19786/frameworks/libs/framework 2/library.swf"), new File("/Developer/SDKs/flex_sdk_4.5.0.19786/frameworks/libs/framework 2/libraryOPTIMIZED.swf"), new AbcNameFilter() {
      @Override
      public boolean accept(String name) {
        return true;
      }
    });
    System.out.print("\n");
    System.out.print(System.currentTimeMillis() - time);

    //u();
  }

  private static void t() throws IOException {
    new AbcFilter().filter(new File("/Users/develar/Library/Caches/IntelliJIdea10/plugins-sandbox/system/flexUIDesigner/framework.59cfca2cTEST-OUTTTTTT.swf"), new File("abc-blank-maker/src/b/u.swf"), new AbcNameFilter() {
      @Override
      public boolean accept(String name) {
        return !name.startsWith("_");
      }
    });
  }

  private static void u() throws IOException {
    File file = new File("/Users/develar/Desktop/Untitled.jpg");
    ImageWrapper imageWrapper = new ImageWrapper((int)file.length());
    imageWrapper.wrap(new FileInputStream(file), new FileOutputStream(new File("/Users/develar/Desktop/u.swf")));
  }
}