package com.dmarcotte.handlebars.editor.actions;

import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.lang.Language;

public class HbCommentActionTest extends HbActionHandlerTest {

  private Language myPrevCommenterLang;

  @Override
  protected void setUp()
    throws Exception {
    super.setUp();

    myPrevCommenterLang = HbConfig.getCommenterLanguage();

    // ensure that no commenter is selected to make sure that we test defaulting to HTML comments
    HbConfig.setCommenterLanguage(null);
  }

  public void testInsertLineComment1() {
    doLineCommentTest(

      "{{#foo}}<caret>",

      "<!--{{#foo}}<caret>-->"
    );
  }

  public void testInsertLineComment2() {
    doLineCommentTest(

      "{{#foo}}\n" +
      "<caret>    {{bar}}\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    <!--{{bar}}-->\n" +
      "<caret>{{/foo}}"
    );
  }

  public void testInsertBlockComment1() {
    doBlockCommentTest(

      "{{#foo}}<caret>",

      "{{#foo}}<!--<caret>-->"
    );
  }

  public void testInsertBlockComment2() {
    doBlockCommentTest(

      "{{#foo}}\n" +
      "    <caret>{{bar}}\n" +
      "{{/foo}",

      "{{#foo}}\n" +
      "    <!--<caret>-->{{bar}}\n" +
      "{{/foo}"
    );
  }

  public void testInsertBlockCommentWithSelection() {
    doBlockCommentTest(

      "<selection><caret>{{#foo}}" +
      "    {{bar}}</selection>" +
      "{{/foo}",

      "<selection><!--<caret>{{#foo}}" +
      "    {{bar}}--></selection>" +
      "{{/foo}"
    );
  }

  public void _testInsertNonDefaultLineComment() {
    Language prevCommenterLanguage = HbConfig.getCommenterLanguage();
    // TODO HbConfig.setCommenterLanguage(JavaLanguage.INSTANCE);

    doLineCommentTest(

      "{{#foo}}<caret>",

      "//{{#foo}}<caret>"
    );

    HbConfig.setCommenterLanguage(prevCommenterLanguage);
  }

  public void _testInsertNonDefaultBlockComment() {
    Language prevCommenterLanguage = HbConfig.getCommenterLanguage();
    // TODO HbConfig.setCommenterLanguage(JavaLanguage.INSTANCE);

    doBlockCommentTest(

      "{{#foo}}<caret>",

      "{{#foo}}/*<caret>*/"
    );

    HbConfig.setCommenterLanguage(prevCommenterLanguage);
  }

  public void testNativeInsertLineComment1() {
    Language prevCommenterLang = HbConfig.getCommenterLanguage();
    HbConfig.setCommenterLanguage(HbLanguage.INSTANCE);

    doLineCommentTest(

      "{{#foo}}<caret>",

      "{{!--{{#foo}}<caret>--}}"
    );

    HbConfig.setCommenterLanguage(prevCommenterLang);
  }

  public void testNativeInsertLineComment2() {
    Language prevCommenterLang = HbConfig.getCommenterLanguage();
    HbConfig.setCommenterLanguage(HbLanguage.INSTANCE);

    doLineCommentTest(

      "{{#foo}}\n" +
      "<caret>    {{bar}}\n" +
      "{{/foo}}",

      "{{#foo}}\n" +
      "    {{!--{{bar}}--}}\n" +
      "<caret>{{/foo}}"
    );

    HbConfig.setCommenterLanguage(prevCommenterLang);
  }

  public void testNativeInsertBlockComment1() {
    Language prevCommenterLang = HbConfig.getCommenterLanguage();
    HbConfig.setCommenterLanguage(HbLanguage.INSTANCE);

    doBlockCommentTest(

      "{{#foo}}<caret>",

      "{{#foo}}{{!--<caret>--}}"
    );

    HbConfig.setCommenterLanguage(prevCommenterLang);
  }

  public void testNativeInsertBlockComment2() {
    Language prevCommenterLang = HbConfig.getCommenterLanguage();
    HbConfig.setCommenterLanguage(HbLanguage.INSTANCE);

    doBlockCommentTest(

      "{{#foo}}\n" +
      "    <caret>{{bar}}\n" +
      "{{/foo}",

      "{{#foo}}\n" +
      "    {{!--<caret>--}}{{bar}}\n" +
      "{{/foo}"
    );

    HbConfig.setCommenterLanguage(prevCommenterLang);
  }

  public void testNativeInsertBlockCommentWithSelection() {
    Language prevCommenterLang = HbConfig.getCommenterLanguage();
    HbConfig.setCommenterLanguage(HbLanguage.INSTANCE);

    doBlockCommentTest(

      "<selection><caret>{{#foo}}" +
      "    {{bar}}</selection>" +
      "{{/foo}",

      "<selection>{{!--<caret>{{#foo}}" +
      "    {{bar}}--}}</selection>" +
      "{{/foo}"
    );

    HbConfig.setCommenterLanguage(prevCommenterLang);
  }

  @Override
  protected void tearDown()
    throws Exception {
    HbConfig.setCommenterLanguage(myPrevCommenterLang);
    super.tearDown();
  }
}
