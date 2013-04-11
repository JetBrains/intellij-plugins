package com.dmarcotte.handlebars.editor.actions;

import com.dmarcotte.handlebars.config.HbConfig;

public class HbEnterHandlerTest extends HbActionHandlerTest {

  private boolean myPrevFormatSetting;

  @Override
  protected void setUp()
    throws Exception {
    super.setUp();

    // disable the formatter for these tests
    myPrevFormatSetting = HbConfig.isFormattingEnabled();
    HbConfig.setFormattingEnabled(false);
  }

  @Override
  protected void tearDown()
    throws Exception {
    HbConfig.setFormattingEnabled(myPrevFormatSetting);

    super.tearDown();
  }

  /**
   * On Enter between matching open/close tags,
   * expect an extra newline to be inserted with the caret placed
   * between the tags
   */
  public void testEnterBetweenMatchingHbTags() {
    doEnterTest(

      "{{#foo}}<caret>{{/foo}}",

      "{{#foo}}\n" +
      "<caret>\n" +
      "{{/foo}}"
    );
  }

  /**
   * On Enter between MIS-matched open/close tags,
   * we still get the standard behavior
   */
  public void testEnterBetweenMismatchedHbTags() {
    doEnterTest(

      "{{#foo}}<caret>{{/bar}}" +
      "stuff",

      "{{#foo}}\n" +
      "<caret>\n" +
      "{{/bar}}" +
      "stuff"
    );
  }

  /**
   * On Enter at an open tag with no close tag,
   * expect a standard newline
   * (Notice that we have "other stuff" our test string.  When the caret is at the file
   * boundary, it's actually a special case.  See {@link #testEnterAtOpenTagOnFileBoundary}
   */
  public void testEnterAtOpenTag() {
    doEnterTest(

      "{{#foo}}<caret>" +
      "other stuff",

      "{{#foo}}\n" +
      "<caret>" +
      "other stuff"

    );
  }

  /**
   * On Enter at an open tag with no close tag,
   * expect a standard newline.
   * <p/>
   * Note: this used to result in an error.  The was a bug where we checked beyond the
   * end of the file for a close tag to go with this open tag.
   */
  public void testEnterAtOpenTagOnFileBoundary() {
    doEnterTest(

      "{{#foo}}<caret>",

      "{{#foo}}\n" +
      "<caret>"
    );
  }
}
