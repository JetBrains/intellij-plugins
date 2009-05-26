package com.intellij.tapestry.intellij.util;

import com.intellij.facet.FacetManager;
import com.intellij.javaee.web.WebRoot;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.DataConstantsEx;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.tapestry.core.java.IJavaType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaArrayType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.core.java.IntellijJavaPrimitiveType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

/**
 * Utility methods for IDEA.
 */
public class IdeaUtils {

    /**
     * Checks if the given event was created from a module node.
     *
     * @param event the event.
     * @return <code>true</code> if the given event was created from a module node, <code>false</code> otherwise.
     */
    public static boolean isModuleNode(AnActionEvent event) {
        final DataContext dataContext = event.getDataContext();
        final Project project = (Project) dataContext.getData(DataConstantsEx.PROJECT);
        final Module module = (Module) dataContext.getData(DataConstantsEx.MODULE_CONTEXT);

        return project != null && module != null;
    }

    /**
     * Finds all web roots expect the WEB-INF directory.
     *
     * @param module the module to search for web roots.
     * @return all the module web roots expect the WEB-INF directory.
     */
    public static List<WebRoot> findWebRoots(Module module) {
        List<WebRoot> finalWebRoots = new ArrayList<WebRoot>();

        WebFacet webFacet = IdeaUtils.getWebFacet(module);
        if (webFacet == null)
            return finalWebRoots;

        List<WebRoot> webRoots = webFacet.getWebRoots(false);

        for (WebRoot webRoot : webRoots)
            if (!webRoot.getRelativePath().equals("/WEB-INF")) {
                finalWebRoots.add(webRoot);
            }

        return finalWebRoots;
    }

    /**
     * Ensures that the given package exists in the given source directory.
     *
     * @param sourceDirectory the source directory.
     * @param packageName     the package.
     * @return the new/existing directory.
     * @throws com.intellij.util.IncorrectOperationException
     *          if an error occurs executing.
     */
    public static PsiDirectory findOrCreateDirectoryForPackage(PsiDirectory sourceDirectory, String packageName) throws IncorrectOperationException {
        PsiDirectory finalDirectory = sourceDirectory;
        StringTokenizer packageTokens = new StringTokenizer(packageName, ".", false);
        while (packageTokens.hasMoreTokens()) {
            String currentToken = packageTokens.nextToken();

            if (finalDirectory.findSubdirectory(currentToken) != null) {
                finalDirectory = finalDirectory.findSubdirectory(currentToken);
            } else {
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
     * @return <code>true</code> if the given directory is a web root in the given module, <code>false</false> otherwise.
     */
    public static boolean isWebRoot(Module module, VirtualFile directory) {
        WebFacet webFacet = IdeaUtils.getWebFacet(module);
        if (webFacet == null)
            return false;

        List<WebRoot> webRoots = webFacet.getWebRoots(false);

        for (WebRoot webRoot : webRoots)
            if (webRoot.getFile().getPath().equals(directory.getPath())) {
                return true;
            }

        return false;
    }

    /**
     * Finds the first public class in an array of classes.
     *
     * @param classes the arrays of classes to search.
     * @return the first public class in the given array of classes, <code>null</code> if none is found.
     */
    @Nullable
    public static PsiClass findPublicClass(PsiClass[] classes) {
        for (PsiClass clazz : classes)
            if (clazz.isValid() && clazz.getModifierList().hasExplicitModifier(PsiModifier.PUBLIC)) {
                return clazz;
            }

        return null;
    }

    /**
     * Executes some code inside a write action commmand block.
     *
     * @param project  the project executing the code.
     * @param runnable the code to execute.
     */
    public static void runWriteCommand(Project project, final Runnable runnable) {
        CommandProcessor.getInstance().executeCommand(
                project, new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(runnable);
            }
        }, "", null
        );
    }

    /**
     * Gets a fresh instance of a PsiClass.
     *
     * @param module   the module the PsiClass belongs to.
     * @param classFqn the class fqn to reload.
     * @return the reloaded instance of the given PsiClass.
     */
    public static PsiClass reloadPsiClass(Module module, String classFqn) {
        if (classFqn == null) {
            return null;
        }

        return JavaPsiFacade.getInstance(module.getProject()).findClass(classFqn, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
    }

    /**
     * Finds the first parent of a given type.
     *
     * @param node  the node to start the search from.
     * @param clazz the type of the parent node.
     * @return the first parent node of the given class.
     */
    public static <T> T findFirstParent(DefaultMutableTreeNode node, Class<T> clazz) {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();

        while (parent != null) {
            if (clazz.isInstance(parent.getUserObject())) {
                return (T) parent;
            } else {
                parent = (DefaultMutableTreeNode) parent.getParent();
            }
        }

        return null;
    }

    /**
     * Create a JavaType instance from a PsiType
     *
     * @param module the module the type belongs to.
     * @param type   the PsiType instance.
     * @return the corresponding JavaType instance, or <code>null</code> if type can't be converted into a JavaType.
     */
    public static IJavaType createJavaTypeFromPsiType(Module module, PsiType type) {
        if (type instanceof PsiClassType) {
            PsiClass psiClass = null;

            try {
                psiClass = ((PsiClassType) type).resolve();
            } catch (ProcessCanceledException ex) {
                // ignore
            }

            if (psiClass != null)
                return new IntellijJavaClassType(module, psiClass.getContainingFile());
            else
                return null;
        }

        if (type instanceof PsiPrimitiveType)
            return new IntellijJavaPrimitiveType((PsiPrimitiveType) type);

        if (type instanceof PsiArrayType)
            return new IntellijJavaArrayType(module, (PsiArrayType) type);

        return null;
    }

    /**
     * Finds the module configured content roots.
     *
     * @param module the module to look for content roots.
     * @return the given module configured content roots.
     */
    @NotNull
    public static Collection<VirtualFile> getModuleContentRoots(@NotNull Module module) {

        Collection<VirtualFile> contentRoots = new ArrayList<VirtualFile>();
        contentRoots.addAll(Arrays.asList(ModuleRootManager.getInstance(module).getContentRoots()));
        return contentRoots;
    }

    /**
     * Finds the web facet of a module.
     * @param module the module to find the facet in.
     * @return the web facet of the given module or <code>null</code> if the module doesn't have one.
     */
    @Nullable
    public static WebFacet getWebFacet(@NotNull Module module) {
        return FacetManager.getInstance(module).getFacetByType(WebFacet.ID);
    }//getWebFacet


    public static PsiPackage getPackage(PsiDirectory psiDirectory) {
        Project project = psiDirectory.getProject();
        ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        String packageName = projectFileIndex.getPackageNameByDirectory(psiDirectory.getVirtualFile());
        if (packageName == null) return null;
        return JavaPsiFacade.getInstance(project).findPackage(packageName);
    }

  @NotNull
  public static XmlElement getNameElement(@NotNull XmlTag tag) {
    return (XmlElement)tag.getFirstChild().getNextSibling();
  }

  @NotNull
  public static XmlElement getNameElementClosing(@NotNull XmlTag tag) {
    return (XmlElement)tag.getLastChild().getPrevSibling();
  }

  @Nullable
  public static Module getModule(XmlTag tag) {
    VirtualFile vFile = tag.getContainingFile().getViewProvider().getVirtualFile();
    return ProjectRootManager.getInstance(tag.getManager().getProject()).getFileIndex().getModuleForFile(vFile);
  }
}//IdeaUtils
