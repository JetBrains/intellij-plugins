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

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import jetbrains.communicator.core.vfs.ProjectsData;
import jetbrains.communicator.core.vfs.VFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kir
 */
public class ProjectsDataFiller {
  private final ProjectsData myProjectsData;

  public ProjectsDataFiller(ProjectsData projectsData) {
    myProjectsData = projectsData;
  }

  public void fillProjectsData() {
    Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
    for (final Project openProject : openProjects) {
      fillProjectData(openProject, myProjectsData);
    }
  }

  private void fillProjectData(Project openProject, final ProjectsData result) {
    VirtualFile[] openFiles = FileEditorManager.getInstance(openProject).getOpenFiles();

    List<VFile> fileInfos = new ArrayList<>(openFiles.length);
    for (VirtualFile openFile : openFiles) {
      VFile vFile = VFSUtil.createFileFrom(openFile, openProject);
      if (vFile != null) {
        if (vFile.getProjectName() != null) {
          fileInfos.add(vFile);
        } else {
          result.addNonProjectFile(vFile);
        }
      }
    }
    result.setProjectFiles(openProject.getName(),
        fileInfos.toArray(new VFile[0]));
  }

}
