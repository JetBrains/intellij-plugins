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

import com.intellij.execution.configurations.RunProfile;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.jetbrains.cidr.execution.debugger.CidrDebugProcess;
import com.jetbrains.cidr.execution.debugger.CidrDebuggerLanguageSupportFactory;
import com.jetbrains.cidr.execution.debugger.CidrEvaluator;
import com.jetbrains.cidr.execution.debugger.CidrStackFrame;
import com.jetbrains.cidr.execution.debugger.evaluation.CidrDebuggerTypesHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.debugger.impl.RubyDebuggerEditorsProvider;
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType;
import org.jetbrains.plugins.ruby.ruby.run.configuration.AbstractRubyRunConfiguration;

/**
 * @author Dennis.Ushakov
 */
public class MotionDebuggerLanguageSupportFactory extends CidrDebuggerLanguageSupportFactory {
  @Nullable
  @Override
  public XDebuggerEditorsProvider createEditor(RunProfile profile) {
    if (profile instanceof AbstractRubyRunConfiguration) {
      return new RubyDebuggerEditorsProvider();
    }
    return null;
  }

  @Nullable
  @Override
  public XDebuggerEditorsProvider createEditor(@NotNull XBreakpoint<? extends XBreakpointProperties> breakpoint) {
    if (breakpoint instanceof XLineBreakpoint) {
      final String extension = FileUtilRt.getExtension(((XLineBreakpoint)breakpoint).getShortFilePath());
      if (FileTypeManager.getInstance().getFileTypeByExtension(extension) == RubyFileType.RUBY) {
        return new RubyDebuggerEditorsProvider();
      }
    }
    return null;
  }

  @NotNull
  @Override
  protected CidrDebuggerTypesHelper createTypesHelper(@NotNull CidrDebugProcess process) {
    return new MotionDebuggerTypesHelper(process);
  }

  @Nullable
  @Override
  protected CidrEvaluator createEvaluator(@NotNull CidrStackFrame frame) {
    return null;
  }

  @Override
  protected boolean isFrameLanguageReliable(@Nullable RunProfile profile) {
    return !(profile instanceof AbstractRubyRunConfiguration);
  }
}
