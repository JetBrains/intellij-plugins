package org.intellij.plugins.postcss;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.css.CssFileType;
import icons.PostcssIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.charset.Charset;

public final class PostCssFileType extends LanguageFileType {
  public static final PostCssFileType POST_CSS = new PostCssFileType();
  @NonNls public static final String DEFAULT_EXTENSION = "pcss";

  private PostCssFileType() {
    super(PostCssLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "PostCSS";
  }

  @NotNull
  @Override
  public String getDescription() {
    return PostCssBundle.message("filetype.postcss.description");
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return DEFAULT_EXTENSION;
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return PostcssIcons.Postcss;
  }

  @Override
  public String getCharset(@NotNull VirtualFile file, byte @NotNull [] content) {
    Charset charset = CssFileType.getCharsetFromCssContent(content);
    return charset != null ? charset.name() : null;
  }

  @Override
  public Charset extractCharsetFromFileContent(@Nullable Project project, @Nullable VirtualFile file, @NotNull CharSequence content) {
    return CssFileType.getCharsetFromCssContent(content);
  }
}
