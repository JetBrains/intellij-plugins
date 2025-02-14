// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.debug;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@ApiStatus.Internal
public class DebuggerCommand {
  private final String myText;
  private final CommandOutputProcessingType myOutputProcessingType; // program will consume immediate server response (no infinite blocking allowed)
  private final VMState myEndVMState;
  private final VMState myStartVMState;

  DebuggerCommand(@NotNull @NonNls String _text) { this(_text, CommandOutputProcessingType.NO_PROCESSING); }
  DebuggerCommand(@NotNull @NonNls String _text, CommandOutputProcessingType outputProcessingType) {
    this(_text, outputProcessingType, VMState.SUSPENDED, VMState.SUSPENDED);
  }

  DebuggerCommand(@NotNull @NonNls String _text, CommandOutputProcessingType outputProcessingType, VMState startState, VMState endState) {
    myText = _text;
    myOutputProcessingType = outputProcessingType;
    myStartVMState = startState;
    myEndVMState = endState;
  }

  @NotNull @NonNls String getText() { return myText; }

  public CommandOutputProcessingType getOutputProcessingMode() {
    return myOutputProcessingType;
  }

  public VMState getEndVMState() {
    return myEndVMState;
  }

  public VMState getStartVMState() {
    return myStartVMState;
  }

  CommandOutputProcessingMode onTextAvailable(@NonNls String s) {
    assert myOutputProcessingType != CommandOutputProcessingType.NO_PROCESSING;
    return CommandOutputProcessingMode.DONE;
  }

  @Override
  public String toString() {
    return getClass().getName();
  }

  public void post(final FlexDebugProcess flexDebugProcess) throws IOException {
    flexDebugProcess.doSendCommandText(this);
  }

  public String read(FlexDebugProcess flexDebugProcess) throws IOException {
    return flexDebugProcess.defaultReadCommand(this);
  }
}
