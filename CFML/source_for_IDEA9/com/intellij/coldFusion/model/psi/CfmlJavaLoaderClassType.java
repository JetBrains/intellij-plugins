package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.regex.Matcher;

/**
 * @author vnikolaenko
 */
public class CfmlJavaLoaderClassType extends PsiType {
  private GlobalSearchScope mySearchScope;
  private Project myProject;

  private class JarFileScope extends GlobalSearchScope {
    private final VirtualFile myVirtualFile;
    private final Module myModule;

    private JarFileScope(@NotNull VirtualFile file) {
      super();
      myVirtualFile = file;
      ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
      myModule = myVirtualFile != null ? fileIndex.getModuleForFile(myVirtualFile) : null;
    }

    public boolean contains(VirtualFile file) {
      return VfsUtil.isAncestor(myVirtualFile, file, true);
    }

    public int compare(VirtualFile file1, VirtualFile file2) {
      return 0;
    }

    public boolean isSearchInModuleContent(@NotNull Module aModule) {
      return aModule == myModule;
    }

    public boolean isSearchInLibraries() {
      return myModule == null;
    }
  }

  public CfmlJavaLoaderClassType(PsiComment comment, Project project) {
    super(PsiAnnotation.EMPTY_ARRAY);
    final String text = comment.getText();

    myProject = project;
    Matcher javaLoaderMatcher = CfmlFile.LOADER_DECL_PATTERN_TEMP.matcher(text);
    mySearchScope = GlobalSearchScope.allScope(project);//EMPTY_SCOPE;

    if (javaLoaderMatcher.matches()) {
      Collection<String> collection = CfmlPsiUtil.findBetween(text, "loadPaths=\"", "\"");
      if (collection != null) {
        for (String str : collection) {
          VirtualFile file = JarFileSystem.getInstance().findFileByPath(str + JarFileSystem.JAR_SEPARATOR);
          if (file != null) {
            mySearchScope = mySearchScope.uniteWith(new JarFileScope(file));
          }
        }
      }
    }
  }

  public GlobalSearchScope getSearchScope() {
    return mySearchScope;
  }

  public String getPresentableText() {
    return "JavaLoader";
  }

  public String getCanonicalText() {
    return "JavaLoader";
  }

  public String getInternalCanonicalText() {
    return "JavaLoader";
  }

  public boolean isValid() {
    return true;
  }

  public boolean equalsToText(@NonNls String text) {
    return false;
  }

  public <A> A accept(@NotNull PsiTypeVisitor<A> visitor) {
    return visitor.visitType(this);
  }

  public GlobalSearchScope getResolveScope() {
    return null;
  }

  @NotNull
  public PsiType[] getSuperTypes() {
    return new PsiType[0];
  }
}
