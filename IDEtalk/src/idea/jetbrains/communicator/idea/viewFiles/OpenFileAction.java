// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.viewFiles;

import com.intellij.openapi.actionSystem.AnActionEvent;
import icons.IdetalkCoreIcons;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Kir
 */
class OpenFileAction extends BaseVFileAction {

  OpenFileAction(JTree fileTree, IDEFacade ideFacade) {
    super(StringUtil.getMsg("open.local.version"), "", IdetalkCoreIcons.EditSource,
        fileTree, ideFacade);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    perform();
  }

  void perform() {
    myIdeFacade.open(getVFile(myFileTree));
  }
}
