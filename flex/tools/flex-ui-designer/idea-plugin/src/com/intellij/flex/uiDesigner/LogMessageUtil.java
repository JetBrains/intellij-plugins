// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.flex.uiDesigner;

import com.intellij.diagnostic.AttachmentFactory;
import com.intellij.openapi.diagnostic.Attachment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

public final class LogMessageUtil {
  public static final Logger LOG = Logger.getInstance(DesignerApplicationManager.class);

  public static void appendLineNumber(StringBuilder builder, ProblemDescriptor problemDescriptor) {
    builder.append(" (line: ").append(problemDescriptor.getLineNumber()).append(')');
  }
  
  @Nullable
  public static Attachment createAttachment(@Nullable VirtualFile file) {
    return file == null ? null : AttachmentFactory.createAttachment(file);
  }

  public static void processInternalError(Throwable t, @Nullable VirtualFile mxmlFile) {
    LOG.error(null, t, createAttachment(mxmlFile));
  }

  public static void processInternalError(Throwable t) {
    LOG.error(t);
  }
}