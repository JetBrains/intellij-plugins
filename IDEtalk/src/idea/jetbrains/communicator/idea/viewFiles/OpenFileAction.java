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
package jetbrains.communicator.idea.viewFiles;

import com.intellij.openapi.actionSystem.AnActionEvent;
import icons.IdetalkCoreIcons;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.util.StringUtil;

import javax.swing.*;

/**
 * @author Kir
 */
class OpenFileAction extends BaseVFileAction {

  OpenFileAction(JTree fileTree, IDEFacade ideFacade) {
    super(StringUtil.getMsg("open.local.version"), "", IdetalkCoreIcons.EditSource,
        fileTree, ideFacade);
  }

  public void actionPerformed(AnActionEvent e) {
    myIdeFacade.open(getVFile(myFileTree));
  }
}
