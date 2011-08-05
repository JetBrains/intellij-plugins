package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.RequiredAssetsInfo;
import com.intellij.openapi.util.text.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

final class AbcBlankMaker {
  public static void main(String[] args) throws IOException {
    //new AbcFilter().filter(new File("abc-blank-maker/src/b/library.swf"), new File("idea-plugin/resources/B.abc"), new AbcNameFilterByEquals("_b000"));
    //d();

    //u();

    //new AbcFilter().filter(new File("/Users/develar/workspace/idea/flex/tools/flex-ui-designer/idea-plugin/testData/libraryWithIncompatibleMxFlexModuleFactory.swf"), new File("/Users/develar/workspace/idea/flex/tools/flex-ui-designer/idea-plugin/testData/o.swf"), null);
    //new AbcFilter().filter(new File("/Users/develar/workspace/idea/flex/tools/flex-ui-designer/test-data-libs/target/test-data-libs 2/library.swf"), new File("/Users/develar/workspace/idea/flex/tools/flex-ui-designer/test-data-libs/target/test-data-libs 2/o.swf"), null);
    //new AbcFilter().filter(new File("/Developer/SDKs/flex_sdk_4.5.0.19786/frameworks/libs/core/library.swf"), new File("/Users/develar/ot.swf"), null);
    //new FlexSdkAbcInjector("4.5", null, new RequiredAssetsInfo()).filter(new File("/Users/develar/workspace/idea/flex/tools/flex-ui-designer/idea-plugin/testData/sdk/4.5/mobilecomponents/library.swf"), new File("/Users/develar/ot.swf"), null);
    //new AbcFilter().filter(new File("/Users/develar/output.swf"), new File("/Users/develar/ot.swf"), null);

    new MovieTranscoder().extract(new File("/Developer/SDKs/flex_4.5.1/frameworks/projects/framework/assets/Assets.swf"), new File("/Users/develar/r.swf"), "mx.containers.FormItem.Required".getBytes());
  }

  private static void d() throws IOException {
    final long time = System.currentTimeMillis();
    new AbcFilter(false).filter(new File("/Developer/SDKs/flex_sdk_4.5.0.19786/frameworks/libs/framework 2/library.swf"), new File("/Developer/SDKs/flex_sdk_4.5.0.19786/frameworks/libs/framework 2/libraryOPTIMIZED.swf"), null);
    System.out.print("\n");
    System.out.print(System.currentTimeMillis() - time);
  }

  private static void t() throws IOException {
    new AbcFilter(false).filter(new File("/Users/develar/Library/Caches/IntelliJIdea10/plugins-sandbox/system/flexUIDesigner/framework.59cfca2cTEST-OUTTTTTT.swf"), new File("abc-blank-maker/src/b/u.swf"), new AbcNameFilter() {
      @Override
      public boolean accept(CharSequence name) {
        return !StringUtil.startsWith(name, "_");
      }
    });
  }

  private static void u() throws IOException {
    File file = new File("/Users/develar/Desktop/Untitled.jpg");
    ImageWrapper imageWrapper = new ImageWrapper((int)file.length());
    imageWrapper.wrap(new FileInputStream(file), new FileOutputStream(new File("/Users/develar/Desktop/u.swf")));
  }
}