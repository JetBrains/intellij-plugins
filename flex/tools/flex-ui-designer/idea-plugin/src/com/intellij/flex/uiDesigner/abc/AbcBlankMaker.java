package com.intellij.flex.uiDesigner.abc;

import com.intellij.flex.uiDesigner.Client;
import com.intellij.flex.uiDesigner.io.ByteArrayOutputStreamEx;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("UnusedDeclaration")
final class AbcBlankMaker {
  public static void main(String[] args) throws IOException {
    //movieSymbolTranscoder();
    //new AbcFilter(false).filter(new File("abc-blank-maker/src/o/library.swf"), new File("idea-plugin/resources/BitmapAsset.abc"), new AbcNameFilterByEquals("_b000"));

    d();

    //u();

    //new AbcFilter().filter(new File("/Users/develar/workspace/idea/flex/tools/flex-ui-designer/idea-plugin/testData/libraryWithIncompatibleMxFlexModuleFactory.swf"), new File("/Users/develar/workspace/idea/flex/tools/flex-ui-designer/idea-plugin/testData/o.swf"), null);
    //new AbcFilter().filter(new File("/Users/develar/workspace/idea/flex/tools/flex-ui-designer/test-data-libs/target/test-data-libs 2/library.swf"), new File("/Users/develar/workspace/idea/flex/tools/flex-ui-designer/test-data-libs/target/test-data-libs 2/o.swf"), null);
    //new AbcFilter().filter(new File("/Developer/SDKs/flex_sdk_4.5.0.19786/frameworks/libs/core/library.swf"), new File("/Users/develar/ot.swf"), null);
    //new FlexSdkAbcInjector("4.5", null, new AssetCounter()).filter(new File("/Users/develar/workspace/idea/flex/tools/flex-ui-designer/idea-plugin/testData/sdk/4.5/mobilecomponents/library.swf"), new File("/Users/develar/ot.swf"), null);
    //new AbcFilter().filter(new File("/Users/develar/output.swf"), new File("/Users/develar/ot.swf"), null);

    //makeBlanks();

    //fillAssetClassPoolGenerator();
    //new MovieSymbolTranscoder().extract(new File("/Developer/SDKs/flex_4.5.1/frameworks/projects/framework/assets/Assets.swf"), new File("/Users/develar/r.swf"), "mx.containers.FormItem.Required".getBytes());
  }

  private static void movieSymbolTranscoder() throws IOException {
    //new MovieSymbolTranscoder().transcode(new File("/Users/develar/Documents/idea/flex/tools/flex-ui-designer/idea-plugin/testData/mxml/AuxAnim.swf"), new File("/Users/develar/a.swf"), "myMC".getBytes());

    new EntireMovieTranscoder().transcode(new File("/Users/develar/Documents/idea/flex/tools/flex-ui-designer/idea-plugin/testData/mxml/AuxAnim.swf"), new File("/Users/develar/a.swf"));
  }

  private static void fillAssetClassPoolGenerator() throws IOException {
    ByteArrayOutputStreamEx out = new ByteArrayOutputStreamEx(1024);
    AssetClassPoolGenerator.generate(Client.ClientMethod.fillImageClassPool, 3, null, out);
    FileUtil.writeToFile(new File("/Users/develar/b.swf"), out.toByteArray());
  }

  private static void makeBlanks() throws IOException {
    final AbcFilter abcTagExtractor = new AbcFilter(null, false);
    final File in = new File("abc-blank-maker/src/o/library.swf");
    abcTagExtractor.filter(in, new File("idea-plugin/resources/BitmapAsset.abc"), new AbcNameFilterByEquals("_b000"));
    abcTagExtractor.filter(in, new File("idea-plugin/resources/SpriteAsset.abc"), new AbcNameFilterByEquals("_s000"));
    // must be encoded as tag
    final AbcFilter abcFilter = new AbcFilter(null, true);
    abcFilter.filter(in, new File("idea-plugin/resources/SSymbolOwnClass.abc"), new AbcNameFilterByEquals("SSymbolOwnClass"));
    abcFilter.filter(in, new File("idea-plugin/resources/MSymbolOwnClass.abc"), new AbcNameFilterByEquals("MSymbolOwnClass"));
  }

  private static void d() throws IOException {
    final long time = System.currentTimeMillis();
    new AbcFilter(null).filter(new File("/Developer/SDKs/flex_4.5.1/frameworks/libs/frameworkUnpacked/library.swf"), new File("/Users/develar/l.swf"), null);
    System.out.print("\n");
    System.out.print(System.currentTimeMillis() - time);
  }

  private static void t() throws IOException {
    new AbcFilter(null).filter(new File("/Users/develar/Library/Caches/IntelliJIdea10/plugins-sandbox/system/flexUIDesigner/framework.59cfca2cTEST-OUTTTTTT.swf"), new File("abc-blank-maker/src/b/u.swf"), new Condition<CharSequence>() {
      @Override
      public boolean value(CharSequence name) {
        return !StringUtil.startsWith(name, "_");
      }
    });
  }
}