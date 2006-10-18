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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import jetbrains.communicator.core.vfs.VFile;

/**
 * @author Kir
 *
 */
public class VFSUtil {
  private static Project[] ourProjects;

  private VFSUtil() {
  }

  public static VirtualFile getVirtualFile(final VFile file) {
    final VirtualFile [] result = new VirtualFile[1];
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        result[0] = _getVirtualFile(file);
      }
    });
    return result[0];
  }

  public static VFile createFileFrom(final VirtualFile file, final Project project) {
    final VFile []result = new VFile[1];

    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        result[0] = _createFileFrom(project, file);
      }
    });

    return result[0];
  }

  private static VFile _createFileFrom(Project project, VirtualFile file) {
    VFile result = null;

    Document document = FileDocumentManager.getInstance().getDocument(file);

    Project[] openProjects = getOpenProjects();
    for (int i = 0; i < openProjects.length && result == null; i++) {
      Project openProject = openProjects[i];
      if (!openProject.isInitialized() && !ApplicationManager.getApplication().isUnitTestMode()) continue;

      if (document != null) {
        PsiFile psiFile = PsiDocumentManager.getInstance(openProject).getPsiFile(document);
        if (isJavaFile(psiFile)) {
          PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
          result = createResultIfNeeded(result, file);
          result.setFQName(psiJavaFile.getClasses()[0].getQualifiedName());
        }
      }

      ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(openProject).getFileIndex();
      if (projectFileIndex.isInSource(file)) {
        VirtualFile sourceRoot = projectFileIndex.getSourceRootForFile(file);
        result = createResultIfNeeded(result, file);
        result.setSourcePath(getRelativePath(file, sourceRoot));
      }

      if (projectFileIndex.isInContent(file)) {
        VirtualFile contentRoot = projectFileIndex.getContentRootForFile(file);
        result = createResultIfNeeded(result, file);
        result.setContentPath(getRelativePath(file, contentRoot));
      }
    }

    if (result == null) {
      result = VFile.create(file.getPath(), null, file.isWritable());
    }

    if (project != null) {
      result.setProjectName(project.getName());
    }

    return result;
  }

  private static boolean isJavaFile(PsiFile psiFile) {
    if (psiFile instanceof PsiJavaFile) {
      PsiJavaFile file = (PsiJavaFile) psiFile;
      String name = file.getName();
      return name != null && name.endsWith(".java");
    }
    return false;
  }

  private static VFile createResultIfNeeded(VFile result, VirtualFile file) {
    if (result == null) {
      result = VFile.create(file.getPath(), file.isWritable());
    }
    return result;
  }

  private static String getRelativePath(VirtualFile file, VirtualFile rootForFile) {
    if (file == null || rootForFile == null) return null;
    return file.getPath().substring(rootForFile.getPath().length());
  }

  private static VirtualFile _getVirtualFile(VFile file) {
    VirtualFile resultFile = null;

    Project fileProject = getProjectByName(file.getProjectName());
    if (fileProject != null) {
      resultFile = _getVirtualFile(fileProject, file);
    }

    if (resultFile == null) {
      resultFile = findFileInAllOpenProjects(fileProject, file);
    }

    return resultFile;
  }

  private static VirtualFile findFileInAllOpenProjects(Project fileProject, VFile file) {
    for (Project openProject : getOpenProjects()) {
      if (fileProject != openProject) {
        VirtualFile virtualFile = _getVirtualFile(openProject, file);
        if (virtualFile != null) {
          return virtualFile;
        }
      }
    }
    return null;
  }

  private static VirtualFile _getVirtualFile(Project project, VFile file) {
    VirtualFile result = findFileByFQName(file, project);

    if (result == null) {
      Module[] modules = ModuleManager.getInstance(project).getModules();
      for (int i = 0; i < modules.length && result == null; i++) {
        result = findFileInModule(modules[i], file);
      }
    }

    return result;
  }

  private static VirtualFile findFileByFQName(VFile file, Project project) {
    VirtualFile result = null;
    if (file.getFQName() != null) {
      PsiClass aClass = PsiManager.getInstance(project).findClass(file.getFQName(), GlobalSearchScope.allScope(project));
      if (aClass != null && isJavaFile(aClass.getNavigationElement().getContainingFile())) {
        result = aClass.getNavigationElement().getContainingFile().getVirtualFile();
      }
    }
    return result;
  }


  private static VirtualFile findFileInModule(Module module, VFile file) {
    ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
    VirtualFile[] files = rootManager.getFiles(OrderRootType.SOURCES);
    VirtualFile result = findInRoots(files, file.getSourcePath());

    if (result == null) {
      result = findInRoots(rootManager.getContentRoots(), file.getContentPath());
    }
    return result;
  }

  private static VirtualFile findInRoots(VirtualFile[] roots, String relativePath) {
    if (relativePath == null) return null;

    for (VirtualFile root : roots) {
      String probeName;
      if (isArchive(root)) {
        probeName = root.getPath() + '!' + relativePath;
      } else {
        probeName = root.getPath() + relativePath;
      }
      VirtualFile virtualFile = root.getFileSystem().findFileByPath(probeName);
      if (virtualFile != null) {
        return virtualFile;
      }
    }
    return null;
  }

  public static boolean isArchive(VirtualFile sourceRoot) {
    return FileTypeManager.getInstance().getFileTypeByFile(sourceRoot) == getArchiveFileType();
  }

  private static FileType getArchiveFileType() {
    FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension(".zip");
    if (fileType == FileTypeManager.getInstance().getFileTypeByExtension(".kokoko")) {
      fileType = FileTypeManager.getInstance().getFileTypeByExtension("zip");
    }
    return fileType;
  }

  private static Project getProjectByName(String projectName) {
    if (projectName == null) return null;

    for (Project openProject : getOpenProjects()) {
      if (openProject.getName().equals(projectName)) {
        return openProject;
      }
    }
    return null;
  }

  private static Project[] getOpenProjects() {
    Project[] openProjects = ourProjects;
    if (openProjects == null) {
      openProjects = ProjectManager.getInstance().getOpenProjects();
    }
    return openProjects;
  }

  public static void _setProject(Project project) {
    ourProjects = new Project[] {project};
  }
}
