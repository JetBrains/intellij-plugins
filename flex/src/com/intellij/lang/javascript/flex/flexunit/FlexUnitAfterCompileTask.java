// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flexunit;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileTask;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;

public class FlexUnitAfterCompileTask implements CompileTask {

  @Override
  public boolean execute(@NotNull final CompileContext context) {
    deleteTempFlexUnitFiles(context);
    return true;
  }

  private static void deleteTempFlexUnitFiles(final CompileContext context) {
    if (!FlexCommonUtils.KEEP_TEMP_FILES) {
      final Collection<String> filesToDelete = context.getUserData(FlexUnitPrecompileTask.FILES_TO_DELETE);
      if (filesToDelete != null) {
        for (String path : filesToDelete) {
          FileUtil.delete(new File(path));
        }
      }
    }

    final File tmpDir = new File(FlexUnitPrecompileTask.getPathToFlexUnitTempDirectory(context.getProject()));
    if (tmpDir.isDirectory() && tmpDir.list().length == 0) {
      FileUtil.delete(tmpDir);
    }
  }
}
