package com.dmarcotte.handlebars.file;

import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.HbIcons;
import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.HbTemplateHighlighter;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.FileAttribute;
import com.intellij.openapi.vfs.newvfs.NewVirtualFile;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class HbFileType extends LanguageFileType implements TemplateLanguageFileType, FileTypeIdentifiableByVirtualFile {
  private static final FileAttribute HANDLEBARS_ATTRIBUTE = new FileAttribute("is.handlebars", 1, true);
  public static final LanguageFileType INSTANCE = new HbFileType();

  @NonNls
  public static final String DEFAULT_EXTENSION = "handlebars;hbs;mustache";

  private HbFileType() {
    super(HbLanguage.INSTANCE);

    FileTypeEditorHighlighterProviders.INSTANCE.addExplicitExtension(this, new EditorHighlighterProvider() {
      public EditorHighlighter getEditorHighlighter(@Nullable Project project,
                                                    @NotNull FileType fileType,
                                                    @Nullable VirtualFile virtualFile,
                                                    @NotNull EditorColorsScheme editorColorsScheme) {
        return new HbTemplateHighlighter(project, virtualFile, editorColorsScheme);
      }
    });
  }

  @NotNull
  public String getName() {
    return "Handlebars/Mustache";
  }

  @NotNull
  public String getDescription() {
    return HbBundle.message("hb.files.file.type.description");
  }

  @NotNull
  public String getDefaultExtension() {
    return DEFAULT_EXTENSION;
  }

  public Icon getIcon() {
    return HbIcons.FILE_ICON;
  }

  public Charset extractCharsetFromFileContent(@Nullable final Project project,
                                               @Nullable final VirtualFile file,
                                               @NotNull final String content) {
    LanguageFileType associatedFileType = getAssociatedFileType(file, project);

    if (associatedFileType == null) {
      return null;
    }

    return associatedFileType.extractCharsetFromFileContent(project, file, content);
  }

  private static LanguageFileType getAssociatedFileType(VirtualFile file, Project project) {
    if (project == null) {
      return null;
    }
    Language language = TemplateDataLanguageMappings.getInstance(project).getMapping(file);

    LanguageFileType associatedFileType = null;
    if (language != null) {
      associatedFileType = language.getAssociatedFileType();
    }

    if (language == null || associatedFileType == null) {
      associatedFileType = HbLanguage.getDefaultTemplateLang();
    }
    return associatedFileType;
  }

  @Override
  public boolean isMyFileType(VirtualFile file) {
    return hasHbAttribute(file);
  }

  public static boolean hasHbAttribute(@Nullable VirtualFile file) {
    if (!(file instanceof NewVirtualFile) || !file.isValid()) {
      return false;
    }
    try {
      DataInputStream inputStream = HANDLEBARS_ATTRIBUTE.readAttribute(file);
      try {
        return inputStream != null && inputStream.readBoolean();
      }
      finally {
        if (inputStream != null) {
          inputStream.close();
        }
      }
    }
    catch (IOException e) {
      return false;
    }
  }

  public static void markAsHbFile(@Nullable VirtualFile file, boolean value) {
    if (!(file instanceof NewVirtualFile) || !file.isValid()) {
      return;
    }
    DataOutputStream outputStream = HANDLEBARS_ATTRIBUTE.writeAttribute(file);
    try {
      try {
        outputStream.writeBoolean(value);
      }
      finally {
        outputStream.close();
      }
    }
    catch (IOException e) {
      // ignore
    }
  }
}
