// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.detection;

import com.intellij.ide.util.DelegatingProgressIndicator;
import com.intellij.ide.util.importProject.LibrariesDetectionStep;
import com.intellij.ide.util.importProject.ModulesDetectionStep;
import com.intellij.ide.util.importProject.ProjectDescriptor;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.importSources.DetectedProjectRoot;
import com.intellij.ide.util.projectWizard.importSources.ProjectFromSourcesBuilder;
import com.intellij.ide.util.projectWizard.importSources.ProjectStructureDetector;
import com.intellij.ide.util.projectWizard.importSources.util.CommonSourceRootDetectionUtil;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.javascript.ActionScriptFileType;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.NullableFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class FlexProjectStructureDetector extends ProjectStructureDetector {

  public static final NullableFunction<CharSequence, String> PACKAGE_NAME_FETCHER = charSequence -> {
    Lexer lexer = LanguageParserDefinitions.INSTANCE.forLanguage(FlexSupportLoader.ECMA_SCRIPT_L4).createLexer(null);
    lexer.start(charSequence);
    return readPackageName(charSequence, lexer);
  };

  public static boolean isActionScriptFile(File file) {
    return FileTypeRegistry.getInstance().getFileTypeByFileName(file.getName()) == ActionScriptFileType.INSTANCE;
  }

  @Override
  public @NotNull DirectoryProcessingResult detectRoots(final @NotNull File dir,
                                                        final File @NotNull [] children,
                                                        final @NotNull File base,
                                                        final @NotNull List<DetectedProjectRoot> result) {
    for (File child : children) {
      if (child.isFile()) {
        if (isActionScriptFile(child)) {

          Pair<File, String> root =
            CommonSourceRootDetectionUtil.IO_FILE.suggestRootForFileWithPackageStatement(child, base, PACKAGE_NAME_FETCHER, false);
          if (root != null) {
            result.add(new FlexModuleSourceRoot(root.getFirst()));
            return DirectoryProcessingResult.skipChildrenAndParentsUpTo(root.getFirst());
          }
          else {
            return DirectoryProcessingResult.SKIP_CHILDREN;
          }
        }
        else if (FlexSupportLoader.isFlexMxmFile(child.getName())) {
          result.add(new FlexModuleSourceRoot(dir));
          // don't skip this folder since .as files can be located here, and they will make some parent folder marked as source root
          // essentially 'dir' will be marked as source root *only* if no .as files exist at this level or below
        }
      }

      if ("node_modules".equals(child.getName())) {
        return DirectoryProcessingResult.SKIP_CHILDREN;
      }
    }
    return DirectoryProcessingResult.PROCESS_CHILDREN;
  }

  @Override
  public List<ModuleWizardStep> createWizardSteps(final ProjectFromSourcesBuilder builder,
                                                  final ProjectDescriptor projectDescriptor,
                                                  final Icon stepIcon) {
    FlexModuleInsight moduleInsight =
      new FlexModuleInsight(new DelegatingProgressIndicator(), builder.getExistingModuleNames(), builder.getExistingProjectLibraryNames());
    final List<ModuleWizardStep> steps = new ArrayList<>();
    steps.add(
      new LibrariesDetectionStep(builder, projectDescriptor, moduleInsight, stepIcon, "reference.dialogs.new.project.fromCode.page1"));
    steps.add(
      new ModulesDetectionStep(this, builder, projectDescriptor, moduleInsight, stepIcon, "reference.dialogs.new.project.fromCode.page2"));
    steps.add(new FlexSdkStep(builder.getContext()));
    return steps;
  }

  public static @Nullable String readPackageName(final CharSequence charSequence, final Lexer lexer) {
    skipWhiteSpaceAndComments(lexer);
    if (!JSTokenTypes.PACKAGE_KEYWORD.equals(lexer.getTokenType())) {
      return null;
    }
    lexer.advance();
    skipWhiteSpaceAndComments(lexer);

    return readQualifiedName(charSequence, lexer, false);
  }

  static @Nullable String readQualifiedName(final CharSequence charSequence, final Lexer lexer, boolean allowStar) {
    final StringBuilder buffer = new StringBuilder();
    while (true) {
      if (lexer.getTokenType() != JSTokenTypes.IDENTIFIER && !(allowStar && lexer.getTokenType() != JSTokenTypes.MULT)) break;
      buffer.append(charSequence, lexer.getTokenStart(), lexer.getTokenEnd());
      if (lexer.getTokenType() == JSTokenTypes.MULT) break;
      lexer.advance();
      if (lexer.getTokenType() != JSTokenTypes.DOT) break;
      buffer.append('.');
      lexer.advance();
    }
    String packageName = buffer.toString();
    if (StringUtil.endsWithChar(packageName, '.')) return null;
    return packageName;
  }

  private static final TokenSet WHITESPACE_AND_COMMENTS =
    TokenSet.create(JSTokenTypes.WHITE_SPACE, JSTokenTypes.DOC_COMMENT, JSTokenTypes.C_STYLE_COMMENT, JSTokenTypes.END_OF_LINE_COMMENT);

  public static void skipWhiteSpaceAndComments(Lexer lexer) {
    while (WHITESPACE_AND_COMMENTS.contains(lexer.getTokenType())) {
      lexer.advance();
    }
  }
}
