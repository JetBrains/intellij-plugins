// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.dmarcotte.handlebars.file;

import com.dmarcotte.handlebars.HbBundle;
import com.dmarcotte.handlebars.HbLanguage;
import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings;
import icons.HandlebarsIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.charset.Charset;

public class HbFileType extends XmlLikeFileType implements TemplateLanguageFileType {
  public static final LanguageFileType INSTANCE = new HbFileType();

  @NonNls
  public static final String DEFAULT_EXTENSION = "handlebars;hbs;mustache";

  private HbFileType() {
    this(HbLanguage.INSTANCE);
  }

  protected HbFileType(Language lang) {
    super(lang);
  }

  @Override
  @NotNull
  public String getName() {
    return "Handlebars/Mustache";
  }

  @Override
  @NotNull
  public String getDescription() {
    return HbBundle.message("filetype.hb.description");
  }

  @Override
  @NotNull
  public String getDefaultExtension() {
    return DEFAULT_EXTENSION;
  }

  @Override
  public Icon getIcon() {
    return HandlebarsIcons.Handlebars_icon;
  }

  @Override
  public Charset extractCharsetFromFileContent(@Nullable final Project project,
                                               @Nullable final VirtualFile file,
                                               @NotNull final CharSequence content) {
    LanguageFileType associatedFileType = getAssociatedFileType(file, project);

    if (associatedFileType == null) {
      return null;
    }

    return CharsetUtil.extractCharsetFromFileContent(project, file, associatedFileType, content);
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
}
