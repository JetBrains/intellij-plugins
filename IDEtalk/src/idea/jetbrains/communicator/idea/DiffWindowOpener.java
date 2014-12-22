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

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManagerEx;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.vfs.VFile;

/**
 * @author Kir
 */
public class DiffWindowOpener {
  private final VFile myVFile;
  private final String myRemoteText;
  private final Project myProject;
  private final VirtualFile myVirtualFile;
  private final User myRemoteUser;

  public DiffWindowOpener(Project project, VirtualFile virtualFile, User remoteUser, VFile vFile, String remoteText) {
    myProject = project;
    myVFile = vFile;
    myRemoteUser = remoteUser;
    myVirtualFile = virtualFile;
    myRemoteText = remoteText;
  }

  public void showDiff() {
    String title = "Diff for " + myVFile.getDisplayName();

    String title1 = "My Version";
    String title2 = myRemoteUser.getDisplayName() + "'s Version";

    DiffContent content1 = DiffContentFactory.getInstance().create(myProject, myVirtualFile);
    DiffContent content2 = DiffContentFactory.getInstance().create(myRemoteText, myVirtualFile.getFileType());

    DiffRequest request = new SimpleDiffRequest(title, content1, content2, title1, title2);
    DiffManagerEx.getInstance().showDiff(myProject, request, DiffDialogHints.NON_MODAL);
  }
}
