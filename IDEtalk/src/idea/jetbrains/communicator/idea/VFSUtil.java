// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import jetbrains.communicator.core.vfs.VFile;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Kir
 *
 */
public final class VFSUtil {
  private static Project[] ourProjects;

  private VFSUtil() {
  }

  public static VirtualFile getVirtualFile(final VFile file) {
    final VirtualFile [] result = new VirtualFile[1];
    ApplicationManager.getApplication().runReadAction(() -> {
      result[0] = _getVirtualFile(file);
    });
    return result[0];
  }

  public static VFile createFileFrom(final VirtualFile file, final Project project) {
    final VFile []result = new VFile[1];

    ApplicationManager.getApplication().runReadAction(() -> {
      result[0] = _createFileFrom(project, file);
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
          assert psiJavaFile != null;
          final PsiClass[] classes = psiJavaFile.getClasses();
          if (classes.length > 0) {
            result = createResultIfNeeded(result, file);
            result.setFQName(classes[0].getQualifiedName());
          }
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
      final Set<VirtualFile> candidates = new HashSet<>();

      Module[] modules = ModuleManager.getInstance(project).getModules();
      for (Module module : modules) {
        findFileInModule(candidates, module, file);
      }

      int currWeight = 0;
      for (VirtualFile candidate : candidates) {
        final int newWeight = weight(candidate, file);
        if (result == null || newWeight > currWeight) {
          currWeight = newWeight;
          result = candidate;
        }
      }
    }

    return result;
  }

  private static int weight(final VirtualFile candidate, final VFile file) {
    final String pattern = file.getFullPath().replace('\\', '/');
    final String candidatePath = candidate.getPath();

    int weight = 0;
    while(
      weight < pattern.length() &&
      weight < candidatePath.length() &&
      pattern.charAt(pattern.length() - weight - 1) == candidatePath.charAt(candidatePath.length() - weight - 1)
      ) weight ++;

    return weight;
  }

  private static VirtualFile findFileByFQName(VFile file, Project project) {
    VirtualFile result = null;
    if (file.getFQName() != null) {
      PsiClass aClass = JavaPsiFacade.getInstance(project).findClass(file.getFQName(), GlobalSearchScope.allScope(project));
      if (aClass != null && isJavaFile(aClass.getNavigationElement().getContainingFile())) {
        result = aClass.getNavigationElement().getContainingFile().getVirtualFile();
      }
    }
    return result;
  }


  private static void findFileInModule(final Set<VirtualFile> found, Module module, VFile file) {
    ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
    findInRoots(found, rootManager.getSourceRoots(), file.getSourcePath());
    findInRoots(found, rootManager.getContentRoots(), file.getContentPath());
  }

  private static void findInRoots(final Set<VirtualFile> found, VirtualFile[] roots, String relativePath) {
    if (relativePath == null) return;

    for (VirtualFile root : roots) {
      String probeName;
      if (isArchive(root)) {
        probeName = root.getPath() + '!' + relativePath;
      } else {
        probeName = root.getPath() + relativePath;
      }
      VirtualFile virtualFile = root.getFileSystem().findFileByPath(probeName);
      if (virtualFile != null) {
        found.add(virtualFile);
      }
    }
  }

  public static boolean isArchive(VirtualFile sourceRoot) {
    return FileTypeRegistry.getInstance().isFileOfType(sourceRoot, getArchiveFileType());
  }

  private static FileType getArchiveFileType() {
    FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension("zip");
    if (fileType == FileTypeManager.getInstance().getFileTypeByExtension("kokoko")) {
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
