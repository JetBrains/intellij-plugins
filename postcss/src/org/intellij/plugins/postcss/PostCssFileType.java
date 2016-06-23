package org.intellij.plugins.postcss;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.css.CssFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.charset.Charset;

public class PostCssFileType extends LanguageFileType {
  public static final PostCssFileType POST_CSS = new PostCssFileType();
  @NonNls public static final String DEFAULT_EXTENSION = "pcss";
  @NonNls private static final String NAME = "PostCSS";
  @NonNls private static final String DESCRIPTION = PostCssBundle.message("filetype.description.postcss");

  private PostCssFileType() {
    super(PostCssLanguage.INSTANCE);
    //TODO highlighter
    /*FileTypeEditorHighlighterProviders.INSTANCE.addExplicitExtension(this, new EditorHighlighterProvider() {
      @Override
      public EditorHighlighter getEditorHighlighter(@Nullable Project project,
                                                    @NotNull FileType fileType, @Nullable VirtualFile virtualFile,
                                                    @NotNull EditorColorsScheme colors) {
        return new LESSEditorHighlighter(colors, project, virtualFile);
      }
    });*/
  }

  @NotNull
  @Override
  public String getName() {
    return NAME;
  }

  @NotNull
  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return DEFAULT_EXTENSION;
  }

  @Nullable
  @Override
  public Icon getIcon() {
    //TODO after #DSGN-3130 will be done
    return null;
  }

  @Override
  public String getCharset(@NotNull VirtualFile file, @NotNull byte[] content) {
    Charset charset = CssFileType.getCharsetFromCssContent(content);
    return charset != null ? charset.name() : null;
  }

  @Override
  public Charset extractCharsetFromFileContent(@Nullable Project project, @Nullable VirtualFile file, @NotNull CharSequence content) {
    return CssFileType.getCharsetFromCssContent(content);
  }
}
