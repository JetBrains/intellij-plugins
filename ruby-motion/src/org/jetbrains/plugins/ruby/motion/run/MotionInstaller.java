/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.encoding.EncodingManager;
import com.jetbrains.cidr.execution.Installer;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriver;
import com.jetbrains.cidr.execution.deviceSupport.AMDevice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Dennis.Ushakov
 */
class MotionInstaller implements Installer {
  private final DeviceProcessHandler myHandler;

  public MotionInstaller(DeviceProcessHandler handler) {
    myHandler = handler;
  }

  @NotNull
  @Override
  public GeneralCommandLine install() throws ExecutionException {
    final GeneralCommandLine result = new GeneralCommandLine();
    result.setExePath(myHandler.getRemotePath());
    result.setWorkDirectory(myHandler.getWorkingDirectory());
    result.getEnvironment().putAll(myHandler.getEnvironment());
    final AMDevice device = myHandler.getDevice();
    try {
      device.lock();
      device.connect();
      Pair<Integer, String> pair = device.startDebugserver();
      result.putUserData(DebuggerDriver.DEBUGSERVER_ID, pair.first);
      result.putUserData(DebuggerDriver.DEBUGSERVER_SOCKET, pair.second);
    }
    finally {
      device.disconnect();
    }

    result.setCharset(EncodingManager.getInstance().getDefaultCharset());
    return result;
  }

  @NotNull
  @Override
  public File getExecutableFile() {
    try {
      return new File(myHandler.getLocalPath());
    }
    catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  @Override
  public File getAppWorkingDir() {
    String result = myHandler.getWorkingDirectory();
    return StringUtil.isEmptyOrSpaces(result) ? null : new File(result);
  }
}
