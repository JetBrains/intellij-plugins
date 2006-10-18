/*
 * Copyright 2000-2006 JetBrains s.r.o.
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
package jetbrains.communicator.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import jetbrains.communicator.util.UIUtil;

import javax.swing.*;
import java.awt.*;

/**
 * @author Kir
 */
public abstract class IdeaDialog extends DialogWrapper {

  protected IdeaDialog(Project project, boolean canBeParent) {
    super(project, canBeParent);
  }

  protected IdeaDialog(boolean canBeParent) {
    super(canBeParent);
  }

  protected IdeaDialog(Component parent, boolean canBeParent) {
    super(parent, canBeParent);
  }

  public void show() {
    super.show();
    UIUtil.requestFocus(getPreferredFocusedComponent());
  }

  protected void dispose() {
    UIUtil.cleanupActions(getContentPane());
    super.dispose();
  }

  public abstract JComponent getPreferredFocusedComponent();

}
