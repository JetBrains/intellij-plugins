package org.jetbrains.lang.manifest.highlighting;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.lang.manifest.parser.ManifestLexer;
import org.jetbrains.lang.manifest.psi.ManifestTokenType;

import java.util.HashMap;
import java.util.Map;

public class ManifestSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
  public static final SyntaxHighlighter HIGHLIGHTER = new SyntaxHighlighterBase() {
    private final Map<IElementType, TextAttributesKey> myAttributes;
    {
      myAttributes = new HashMap<IElementType, TextAttributesKey>();
      myAttributes.put(ManifestTokenType.HEADER_NAME, ManifestColorsAndFonts.HEADER_NAME_KEY);
      myAttributes.put(ManifestTokenType.COLON, ManifestColorsAndFonts.HEADER_ASSIGNMENT_KEY);
      myAttributes.put(ManifestTokenType.HEADER_VALUE_PART, ManifestColorsAndFonts.HEADER_VALUE_KEY);
    }

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
      return new ManifestLexer();
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
      return pack(myAttributes.get(tokenType));
    }
  };

  @NotNull
  @Override
  public SyntaxHighlighter getSyntaxHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile) {
    return HIGHLIGHTER;
  }
}
