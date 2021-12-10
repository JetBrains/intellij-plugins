package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.jetbrains.cidr.cpp.execution.debugger.embedded.svd.SvdWindowState;

@State(
  name = "PlatformioSvdWindowState",
  storages = @Storage(StoragePathMacros.WORKSPACE_FILE)
)
public class PlatformioSvdWindowState extends SvdWindowState {

}
