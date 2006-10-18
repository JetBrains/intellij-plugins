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

import com.intellij.openapi.diff.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.VFile;

/**
 * @author Kir
 */
public class DiffWindowOpener {
  private VFile myVFile;
  private String myRemoteText;
  private final Project myProject;
  private VirtualFile myVirtualFile;
  private User myRemoteUser;

  public DiffWindowOpener(Project project, VirtualFile virtualFile, User remoteUser, VFile vFile, String remoteText) {
    myProject = project;
    myVFile = vFile;
    myRemoteUser = remoteUser;
    myVirtualFile = virtualFile;
    myRemoteText = remoteText;
  }

  public void showDiff() {
    DiffTool diffTool = DiffManager.getInstance().getIdeaDiffTool();
    SimpleDiffRequest diffRequest = new SimpleDiffRequest(myProject, "Diff for " + myVFile.getDisplayName());
    diffRequest.addHint(DiffTool.HINT_SHOW_NOT_MODAL_DIALOG);

    Document localDocument = FileDocumentManager.getInstance().getDocument(myVirtualFile);

    diffRequest.setContentTitles("My Version", myRemoteUser.getDisplayName() + "'s Version");
    diffRequest.setContents(new DocumentContent(localDocument), new SimpleContent(myRemoteText));

    diffTool.show(diffRequest);
  }
}
