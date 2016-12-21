package com.intellij.lang.javascript.linter.tslint;

import com.intellij.execution.ExecutionException;
import com.intellij.javascript.nodejs.AbstractNodeBinFileVersionManager;
import com.intellij.util.text.SemVer;
import org.jetbrains.annotations.NotNull;

public class TsLintBinFileVersionManager extends AbstractNodeBinFileVersionManager {
  @NotNull
  @Override
  public SemVer parse(@NotNull String stdout) throws ExecutionException {
    SemVer version = SemVer.parseFromText(stdout.trim());
    if (version != null) {
      return version;
    }
    throw new ExecutionException("Cannot parse tslint version from '" + stdout + "'");
  }
}
