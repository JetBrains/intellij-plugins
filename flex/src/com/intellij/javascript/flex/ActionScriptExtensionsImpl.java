// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex;

import com.intellij.javascript.flex.refactoring.changeSignature.ActionScriptImportProcessor;
import com.intellij.lang.javascript.flex.ActionScriptExtensions;
import com.intellij.lang.javascript.refactoring.changeSignature.JSChangeSignatureProcessor;
import org.jetbrains.annotations.NotNull;

public class ActionScriptExtensionsImpl implements ActionScriptExtensions {
  @Override
  public JSChangeSignatureProcessor.@NotNull RequiredImportProcessor createImportProcessor() {
    return new ActionScriptImportProcessor();
  }
}
