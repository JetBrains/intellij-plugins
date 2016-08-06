package org.intellij.plugins.postcss.editor;

import com.intellij.ide.highlighter.HighlighterFactory;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.css.impl.CssElementTypes;
import com.intellij.psi.tree.IElementType;
import org.intellij.plugins.postcss.PostCssFileType;
import org.intellij.plugins.postcss.PostCssFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class PostCssEditorHighlightingTest extends PostCssFixtureTestCase {

  public void testCustomMedia() throws Exception {
    //               10        20        30        40        50        60        70        80        90
    //     0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
    doTest("@custom-media --my all and (100px >= height), (min-width: 20px);",
           Pair.create(20, CssElementTypes.CSS_PROPERTY_VALUE),
           Pair.create(29, CssElementTypes.CSS_NUMBER),
           Pair.create(32, CssElementTypes.CSS_PROPERTY_VALUE),
           Pair.create(38, CssElementTypes.CSS_PROPERTY_NAME),
           Pair.create(48, CssElementTypes.CSS_PROPERTY_NAME),
           Pair.create(59, CssElementTypes.CSS_NUMBER),
           Pair.create(61, CssElementTypes.CSS_PROPERTY_VALUE));
  }

  private void doTest(@NotNull final String text, Pair<Integer, IElementType>... pairs) {
    EditorHighlighter highlighter = HighlighterFactory
      .createHighlighter(PostCssFileType.POST_CSS, EditorColorsManager.getInstance().getGlobalScheme(), getProject());
    highlighter.setText(text);
    for (Pair<Integer, IElementType> pair : pairs) {
      assertTrue(highlighter.createIterator(pair.first).getTokenType() == pair.second);
    }
  }
}