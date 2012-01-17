package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.openapi.Disposable;

public interface ModuleOrProjectCompilerOptions extends ModifiableCompilerOptions {
  void addOptionsListener(CompilerOptionsListener listener, Disposable parentDisposable);
}
