package com.intellij.tapestry.intellij.util;

import com.intellij.facet.FacetManager;
import com.intellij.javaee.web.WebRoot;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.tapestry.core.java.IJavaType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaArrayType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaPrimitiveType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Utility methods for IDEA.
 */
public final class IdeaUtils {

  /**
   * Checks if the given event was created from a module node.
   *
   * @param event the event.
   * @return {@code true} if the given event was created from a module node, {@code false} otherwise.
   */
  public static boolean isModuleNode(AnActionEvent event) {
    final Project project = event.getData(CommonDataKeys.PROJECT);
    final Module module = event.getData(LangDataKeys.MODULE_CONTEXT);

    return project != null && module != null;
  }

  /**
   * Finds all web roots expect the WEB-INF directory.
   *
   * @param module the module to search for web roots.
   * @return all the module web roots expect the WEB-INF directory.
   */
  public static List<WebRoot> findWebRoots(Module module) {
    List<WebRoot> finalWebRoots = new ArrayList<>();

    WebFacet webFacet = getWebFacet(module);
    if (webFacet == null) return finalWebRoots;

    List<WebRoot> webRoots = webFacet.getWebRoots();

    for (WebRoot webRoot : webRoots) {
      if (!webRoot.getRelativePath().equals("/WEB-INF")) {
        finalWebRoots.add(webRoot);
      }
    }

    return finalWebRoots;
  }

  /**
   * Ensures that the given package exists in the given source directory.
   *
   * @param sourceDirectory the source directory.
   * @param packageName     the package.
   * @return the new/existing directory.
   * @throws IncorrectOperationException if an error occurs executing.
   */
  @Nullable
  public static PsiDirectory findOrCreateDirectoryForPackage(PsiDirectory sourceDirectory, String packageName)
    throws IncorrectOperationException {
    PsiDirectory finalDirectory = sourceDirectory;
    StringTokenizer packageTokens = new StringTokenizer(packageName, ".", false);
    while (packageTokens.hasMoreTokens()) {
      String currentToken = packageTokens.nextToken();

      if (finalDirectory.findSubdirectory(currentToken) != null) {
        finalDirectory = finalDirectory.findSubdirectory(currentToken);
      }
      else {
        finalDirectory = finalDirectory.createSubdirectory(currentToken);
      }
    }

    return finalDirectory;
  }

  /**
   * Checks if a directory is a web root.
   *
   * @param module    the module to check the web root.
   * @param directory the directory to check.
   * @return {@code true} if the given directory is a web root in the given module, {@code false} otherwise.
   */
  public static boolean isWebRoot(Module module, VirtualFile directory) {
    WebFacet webFacet = getWebFacet(module);
    if (webFacet == null) return false;

    List<WebRoot> webRoots = webFacet.getWebRoots();

    for (WebRoot webRoot : webRoots) {
      if (directory.equals(webRoot.getFile())) {
        return true;
      }
    }

    return false;
  }

  @Nullable
  public static PsiClass findPublicClass(PsiFile psiFile) {
    return psiFile instanceof PsiClassOwner ? findPublicClass(((PsiClassOwner)psiFile).getClasses()) : null;
  }

  /**
   * Finds the first public class in an array of classes.
   *
   * @param classes the arrays of classes to search.
   * @return the first public class in the given array of classes, {@code null} if none is found.
   */
  @Nullable
  public static PsiClass findPublicClass(PsiClass[] classes) {
    PsiModifierList modifierList;
    for (PsiClass clazz : classes) {
      if (clazz.isValid() &&
          (modifierList = clazz.getModifierList()) != null &&
          modifierList.hasModifierProperty(PsiModifier.PUBLIC) &&
          !clazz.isEnum() &&
          !clazz.isInterface() &&
          PsiUtil.hasDefaultConstructor(clazz)
        ) {
        return clazz;
      }
    }

    return null;
  }

  /**
   * Executes some code inside a write action command block.
   *
   * @param project  the project executing the code.
   * @param runnable the code to execute.
   */
  public static void runWriteCommand(Project project, final Runnable runnable) {
    CommandProcessor.getInstance().executeCommand(project, () -> ApplicationManager.getApplication().runWriteAction(runnable), "", null);
  }

  /**
   * Finds the first parent of a given type.
   *
   * @param node  the node to start the search from.
   * @param clazz the type of the parent node.
   * @return the first parent node of the given class.
   */
  @Nullable
  public static <T> T findFirstParent(DefaultMutableTreeNode node, Class<T> clazz) {
    DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();

    while (parent != null) {
      if (clazz.isInstance(parent.getUserObject())) {
        return (T)parent;
      }
      else {
        parent = (DefaultMutableTreeNode)parent.getParent();
      }
    }

    return null;
  }

  /**
   * Create a JavaType instance from a PsiType
   *
   * @param module the module the type belongs to.
   * @param type   the PsiType instance.
   * @return the corresponding JavaType instance, or {@code null} if type can't be converted into a JavaType.
   */
  public static IJavaType createJavaTypeFromPsiType(Module module, PsiType type) {
    if (type instanceof PsiClassType) {
      PsiClass psiClass = null;

      try {
        psiClass = ((PsiClassType)type).resolve();
        if (psiClass instanceof PsiTypeParameter) { // let's consider generic type T as Object
          psiClass = JavaPsiFacade.getInstance(module.getProject()).findClass("java.lang.Object", GlobalSearchScope.moduleWithLibrariesScope(module));
        }
      }
      catch (ProcessCanceledException ex) {
        // ignore
      }

      if (psiClass != null) {
        return new IntellijJavaClassType(module, psiClass.getContainingFile());
      }
      else {
        return null;
      }
    }

    if (type instanceof PsiPrimitiveType) return new IntellijJavaPrimitiveType((PsiPrimitiveType)type);

    if (type instanceof PsiArrayType) return new IntellijJavaArrayType(module, (PsiArrayType)type);

    return null;
  }

  /**
   * Finds the web facet of a module.
   *
   * @param module the module to find the facet in.
   * @return the web facet of the given module or {@code null} if the module doesn't have one.
   */
  @Nullable
  public static WebFacet getWebFacet(@NotNull Module module) {
    return FacetManager.getInstance(module).getFacetByType(WebFacet.ID);
  }//getWebFacet


  @Nullable
  public static PsiPackage getPackage(@Nullable PsiElement psiElement) {
    if (psiElement instanceof PsiDirectory) {
      Project project = psiElement.getProject();
      ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
      String packageName = projectFileIndex.getPackageNameByDirectory(((PsiDirectory)psiElement).getVirtualFile());
      return packageName == null ? null : JavaPsiFacade.getInstance(project).findPackage(packageName);
    }
    return psiElement instanceof PsiPackage ? (PsiPackage)psiElement : null;
  }

  @Nullable
  public static XmlElement getNameElement(@NotNull XmlTag tag) {
    PsiElement child = tag.getFirstChild();
    while (child != null) {
      child = child.getNextSibling();
      if (child instanceof XmlElement) return (XmlElement)child;
    }
    return null;
  }

  @Nullable
  public static XmlElement getNameElementClosing(@NotNull XmlTag tag) {
    PsiElement child = tag.getLastChild();
    while (child != null) {
      child = child.getPrevSibling();
      if (child instanceof XmlElement) return (XmlElement)child;
    }
    return null;
  }

}//IdeaUtils