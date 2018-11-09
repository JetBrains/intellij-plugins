// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

  @Override
  public void show() {
    super.show();
    UIUtil.requestFocus(getPreferredFocusedComponent());
  }

  @Override
  protected void dispose() {
    UIUtil.cleanupActions(getContentPane());
    super.dispose();
  }

  @Override
  public abstract JComponent getPreferredFocusedComponent();

}
