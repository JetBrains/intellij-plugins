package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.clion.embedded.debugger.peripheralview.SvdWindowState;

@State(
  name = "PlatformioSvdWindowState",
  storages = @Storage(StoragePathMacros.WORKSPACE_FILE)
)
public class PlatformioSvdWindowState extends SvdWindowState {

}
