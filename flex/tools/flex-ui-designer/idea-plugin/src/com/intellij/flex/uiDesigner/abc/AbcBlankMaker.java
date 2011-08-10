package com.intellij.flex.uiDesigner.abc;

import com.intellij.openapi.util.text.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

final class AbcBlankMaker {
  public static void main(String[] args) throws IOException {
    //new AbcFilter(false).filter(new File("abc-blank-maker/src/o/library.swf"), new File("idea-plugin/resources/BitmapAsset.abc"), new AbcNameFilterByEquals("_b000"));

    //d();

    //u();

    //new AbcFilter().filter(new File("/Users/develar/workspace/idea/flex/tools/flex-ui-designer/idea-plugin/testData/libraryWithIncompatibleMxFlexModuleFactory.swf"), new File("/Users/develar/workspace/idea/flex/tools/flex-ui-designer/idea-plugin/testData/o.swf"), null);
    //new AbcFilter().filter(new File("/Users/develar/workspace/idea/flex/tools/flex-ui-designer/test-data-libs/target/test-data-libs 2/library.swf"), new File("/Users/develar/workspace/idea/flex/tools/flex-ui-designer/test-data-libs/target/test-data-libs 2/o.swf"), null);
    //new AbcFilter().filter(new File("/Developer/SDKs/flex_sdk_4.5.0.19786/frameworks/libs/core/library.swf"), new File("/Users/develar/ot.swf"), null);
    //new FlexSdkAbcInjector("4.5", null, new RequiredAssetsInfo()).filter(new File("/Users/develar/workspace/idea/flex/tools/flex-ui-designer/idea-plugin/testData/sdk/4.5/mobilecomponents/library.swf"), new File("/Users/develar/ot.swf"), null);
    //new AbcFilter().filter(new File("/Users/develar/output.swf"), new File("/Users/develar/ot.swf"), null);

    makeBlanks();
    //new MovieTranscoder().extract(new File("/Developer/SDKs/flex_4.5.1/frameworks/projects/framework/assets/Assets.swf"), new File("/Users/develar/r.swf"), "mx.containers.FormItem.Required".getBytes());
  }

  private static void makeBlanks() throws IOException {
    final AbcFilter abcTagExtractor = new AbcFilter(false, false);
    final File in = new File("abc-blank-maker/src/o/library.swf");
    abcTagExtractor.filter(in, new File("idea-plugin/resources/BitmapAsset.abc"), new AbcNameFilterByEquals("_b000"));
    abcTagExtractor.filter(in, new File("idea-plugin/resources/SpriteAsset.abc"), new AbcNameFilterByEquals("_s000"));
    // must be encoded as tag
    final AbcFilter abcFilter = new AbcFilter(false, true);
    abcFilter.filter(in, new File("idea-plugin/resources/SSymbolOwnClass.abc"), new AbcNameFilterByEquals("SSymbolOwnClass"));
    abcFilter.filter(in, new File("idea-plugin/resources/MSymbolOwnClass.abc"), new AbcNameFilterByEquals("MSymbolOwnClass"));
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
}