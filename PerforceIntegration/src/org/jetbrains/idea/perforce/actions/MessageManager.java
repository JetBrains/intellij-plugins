/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package org.jetbrains.idea.perforce.actions;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.WaitForProgressToShow;

import javax.swing.*;

/**
 * @author AKireyev
 */
public final class MessageManager {
  private static final Logger LOG = Logger.getInstance(MessageManager.class.getName());

  public static void showMessageDialog(final Project project, final @NlsContexts.DialogMessage String msg, final @NlsContexts.DialogTitle String title, final Icon icon) {
    Runnable runnable = () -> Messages.showMessageDialog(project, msg, title, icon);
    runShowAction(runnable);
  }

  public static void runShowAction(Runnable runnable) {
    try {
      WaitForProgressToShow.runOrInvokeAndWaitAboveProgress(runnable);
    }
    catch (Exception e) {
      LOG.error(e);
    }
  }

  public static int showDialog(final Project project, final @NlsContexts.DialogMessage String msg, final @NlsContexts.DialogTitle String title, final String[] options, final int defaultOptionIndex, final Icon icon) {
    final int[] result = new int[1];
    Runnable runnable = () -> result[0] = Messages.showDialog(project, msg, title, options, defaultOptionIndex, icon);
    runShowAction(runnable);
    return result[0];
  }


}
