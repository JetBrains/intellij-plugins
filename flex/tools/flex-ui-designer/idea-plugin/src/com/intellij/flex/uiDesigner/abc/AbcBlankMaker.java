package com.intellij.flex.uiDesigner.abc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

final class AbcBlankMaker {
  public static void main(String[] args) throws IOException {
    new AbcFilter().filter(new File("abc-blank-maker/src/b/library.swf"), new File("out/B.abc"), new AbcNameFilterByEquals("B"));

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