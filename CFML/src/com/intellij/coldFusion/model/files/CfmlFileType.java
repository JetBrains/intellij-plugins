/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
