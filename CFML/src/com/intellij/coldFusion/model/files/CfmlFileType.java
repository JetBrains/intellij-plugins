package com.intellij.coldFusion.model.files;

import com.intellij.coldFusion.UI.highlighting.CfmlHighlighter;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.EditorHighlighterProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeEditorHighlighterProviders;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import icons.CFMLIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by Lera Nikolaenko
 * Date: 30.09.2008
 */
public class CfmlFileType extends LanguageFileType {
  public static final CfmlFileType INSTANCE = new CfmlFileType();

  private CfmlFileType() {
    super(CfmlLanguage.INSTANCE);
    FileTypeEditorHighlighterProviders.INSTANCE.addExplicitExtension(this, new EditorHighlighterProvider() {
      @Override
      public EditorHighlighter getEditorHighlighter(@Nullable Project project,
                                                    @NotNull FileType fileType, @Nullable VirtualFile virtualFile,
                                                    @NotNull EditorColorsScheme colors) {
        return new CfmlHighlighter(project, virtualFile, colors);
      }
    });
  }

  @NotNull
  public String getName() {
    return "CFML";
  }

  @NotNull
  public String getDescription() {
    return "Cold Fusion";
  }

  @NotNull
  public String getDefaultExtension() {
    return "cfm";
  }

  public Icon getIcon() {
    return CFMLIcons.Cfml;
  }

  @NotNull
  @NonNls
  public String[] getExtensions() {
    return new String[]{"cfm", "cfml", "cfc"};
  }
}
