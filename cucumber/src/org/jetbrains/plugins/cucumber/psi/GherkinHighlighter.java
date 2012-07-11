package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.colors.TextAttributesKeyDefaults;
import com.intellij.openapi.editor.markup.TextAttributes;
import org.jetbrains.annotations.NonNls;

import java.awt.*;

/**
 * @author Roman.Chernyatchik
 */
public class GherkinHighlighter {
  @NonNls
  static final String COMMENT_ID = "GHERKIN_COMMENT";
  public static final TextAttributes COMMENT_DEFAULT_ATTRS = TextAttributesKeyDefaults.getDefaultAttributes(SyntaxHighlighterColors.DOC_COMMENT).clone();
  public static final TextAttributesKey COMMENT = TextAttributesKeyDefaults.createTextAttributesKey(
    COMMENT_ID,
    COMMENT_DEFAULT_ATTRS
  );

  @NonNls
  static final String KEYWORD_ID = "GHERKIN_KEYWORD";
  public static final TextAttributes KEYWORD_DEFAULT_ATTRS = TextAttributesKeyDefaults.getDefaultAttributes(SyntaxHighlighterColors.KEYWORD).clone();
  public static final TextAttributesKey KEYWORD = TextAttributesKeyDefaults.createTextAttributesKey(
    KEYWORD_ID,
    KEYWORD_DEFAULT_ATTRS
  );


  @NonNls
  static final String GHERKIN_TABLE_HEADER_CELL_ID = "GHERKIN_TABLE_HEADER_CELL";
  static final String GHERKIN_OUTLINE_PARAMETER_SUBSTITUTION_ID = "GHERKIN_OUTLINE_PARAMETER_SUBSTITUTION";
  public static final TextAttributes PARAMETER_DEFAULT_ATTRS = TextAttributesKeyDefaults.getDefaultAttributes(HighlighterColors.TEXT).clone();
  public static final TextAttributesKey TABLE_HEADER_CELL = TextAttributesKeyDefaults.createTextAttributesKey(
    GHERKIN_TABLE_HEADER_CELL_ID,
    PARAMETER_DEFAULT_ATTRS
  );
  public static final TextAttributesKey OUTLINE_PARAMETER_SUBSTITUTION = TextAttributesKeyDefaults.createTextAttributesKey(
    GHERKIN_OUTLINE_PARAMETER_SUBSTITUTION_ID,
    PARAMETER_DEFAULT_ATTRS
  );

  @NonNls
  static final String GHERKIN_TAG_ID = "GHERKIN_TAG";
  public static final TextAttributes TAG_ATTRS = TextAttributesKeyDefaults.getDefaultAttributes(HighlighterColors.TEXT).clone();
  public static final TextAttributesKey TAG = TextAttributesKeyDefaults.createTextAttributesKey(
    GHERKIN_TAG_ID,
    TAG_ATTRS
  );

  @NonNls
  static final String GHERKIN_TABLE_CELL_ID = "GHERKIN_TABLE_CELL";
  @NonNls
  static final String GHERKIN_REGEXP_PARAMETER_ID = "GHERKIN_REGEXP_PARAMETER";
  public static final TextAttributes HEREDOC_CONTENT_DEFAULT_ATTRS = TextAttributesKeyDefaults.getDefaultAttributes(HighlighterColors.TEXT).clone();
  public static final TextAttributesKey TABLE_CELL = TextAttributesKeyDefaults.createTextAttributesKey(
    GHERKIN_TABLE_CELL_ID,
    HEREDOC_CONTENT_DEFAULT_ATTRS
  );

  public static final TextAttributesKey REGEXP_PARAMETER = TextAttributesKeyDefaults.createTextAttributesKey(
    GHERKIN_REGEXP_PARAMETER_ID,
    HEREDOC_CONTENT_DEFAULT_ATTRS
  );

  @NonNls
  static final String GHERKIN_PYSTRING_ID = "GHERKIN_PYSTRING";
  public static final TextAttributes GHERKIN_PYSTRING_ATTRS = TextAttributesKeyDefaults.getDefaultAttributes(HighlighterColors.TEXT).clone();
  public static final TextAttributesKey PYSTRING = TextAttributesKeyDefaults.createTextAttributesKey(
    GHERKIN_PYSTRING_ID,
    GHERKIN_PYSTRING_ATTRS
  );

  public static final TextAttributesKey TEXT = TextAttributesKeyDefaults.createTextAttributesKey("GHERKIN_TEXT",
                                                                                         TextAttributesKeyDefaults.getDefaultAttributes(HighlighterColors.TEXT));

  public static final TextAttributesKey PIPE =
    TextAttributesKeyDefaults.createTextAttributesKey("GHERKIN_TABLE_PIPE", TextAttributesKeyDefaults.getDefaultAttributes(KEYWORD));

  static {
    //Init additional color, font types and effects
    KEYWORD_DEFAULT_ATTRS.setFontType(Font.BOLD);
    KEYWORD_DEFAULT_ATTRS.setForegroundColor(new Color(0, 0, 128));

    COMMENT_DEFAULT_ATTRS.setFontType(Font.ITALIC);
    COMMENT_DEFAULT_ATTRS.setForegroundColor(Color.GRAY);

    GHERKIN_PYSTRING_ATTRS.setFontType(Font.BOLD);
    GHERKIN_PYSTRING_ATTRS.setForegroundColor(new Color(0, 128, 128));

    HEREDOC_CONTENT_DEFAULT_ATTRS.setFontType(Font.BOLD);
    HEREDOC_CONTENT_DEFAULT_ATTRS.setForegroundColor(new Color(41, 123, 222));

    TAG_ATTRS.setFontType(Font.BOLD);
    TAG_ATTRS.setForegroundColor(new Color(102, 14, 122));

    PARAMETER_DEFAULT_ATTRS.setFontType(Font.ITALIC);
    PARAMETER_DEFAULT_ATTRS.setForegroundColor(new Color(195, 117, 34));
  }

  private GherkinHighlighter() {
  }
}
