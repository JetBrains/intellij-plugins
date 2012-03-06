package com.intellij.lang.javascript.flex.library;

import com.intellij.ide.util.projectWizard.importSources.util.CommonSourceRootDetectionUtil;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.detection.FlexProjectStructureDetector;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: ksafonov
 */
class FlexRawAsLibrariesRootDetector extends AsRootDetectorBase {

  public FlexRawAsLibrariesRootDetector() {
    super(OrderRootType.CLASSES, FlexBundle.message("as.libraries.root.detector.name"), false);
  }

}
