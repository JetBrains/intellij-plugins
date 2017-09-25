package com.intellij.javascript.flex.resolve;

import com.intellij.lang.javascript.DialectOptionHolder;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.JSResolveHelper;
import com.intellij.lang.javascript.index.JSIndexedRootProvider;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.JSCommonTypeNames;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.ActionScriptResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSClassResolver;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.stubs.JSQualifiedElementIndex;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.indexing.AdditionalIndexedRootsScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.intellij.lang.javascript.psi.JSCommonTypeNames.*;

/**
 * @author Konstantin.Ulitin
 */
public class ActionScriptClassResolver extends JSClassResolver {

  private static ActionScriptClassResolver INSTANCE = null;

  protected ActionScriptClassResolver() {
  }

  @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
  public static ActionScriptClassResolver getInstance() {
    if (INSTANCE == null) INSTANCE = new ActionScriptClassResolver();
    return INSTANCE;
  }

  @Override
  public PsiElement findClassByQName(@NotNull String link, @NotNull PsiElement context) {
    return findClassByQNameStatic(link, context);
  }

  public static PsiElement findClassByQNameStatic(@NotNull String link, @NotNull PsiElement context) {
    return getInstance().findClassByQName(link, JavaScriptIndex.getInstance(context.getProject()), JSResolveUtil.getResolveScope(context),
                                          DialectOptionHolder.ECMA_4);
  }

  public static PsiElement findClassByQName(@NotNull final String link, final JavaScriptIndex index, final Module module) {
    GlobalSearchScope searchScope = JSInheritanceUtil.getEnforcedScope();
    if (searchScope == null) {
      searchScope =
        module != null ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module) : GlobalSearchScope.allScope(index.getProject());
    }
    return getInstance().findClassByQName(link, index, searchScope, DialectOptionHolder.ECMA_4);
  }

  @Nullable
  @Override
  public PsiElement findClassByQName(@NotNull String link, @NotNull GlobalSearchScope scope) {
    return findClassByQNameStatic(link, scope);
  }

  public static PsiElement findClassByQNameStatic(@NotNull final String link, @NotNull GlobalSearchScope scope) {
    return getInstance().findClassByQName(link, JavaScriptIndex.getInstance(scope.getProject()), scope, DialectOptionHolder.ECMA_4);
  }

  public static boolean isParentClass(JSClass clazz, String className) {
    return isParentClass(clazz, className, true);
  }

  public static boolean isParentClass(JSClass clazz, String className, boolean strict) {
    final PsiElement parentClass = findClassByQNameStatic(className, clazz.getResolveScope());
    if (!(parentClass instanceof JSClass)) return false;

    return JSInheritanceUtil.isParentClass(clazz, (JSClass)parentClass, strict);
  }

  protected PsiElement doFindClassByQName(@NotNull String link, final JavaScriptIndex index, GlobalSearchScope searchScope,
                                          boolean allowFileLocalSymbols, @NotNull DialectOptionHolder dialect) {
    Project project = index.getProject();
    boolean clazzShouldBeTakenFromOurLibrary = OBJECT_CLASS_NAME.equals(link) || "Arguments".equals(link);
    if (clazzShouldBeTakenFromOurLibrary && !(searchScope instanceof AdditionalIndexedRootsScope)) {
      // object from swf do not contain necessary members!
      searchScope = new AdditionalIndexedRootsScope(searchScope, JSIndexedRootProvider.class);
    }
    final Collection<JSQualifiedNamedElement> candidates = StubIndex.getElements(JSQualifiedElementIndex.KEY, link.hashCode(),
                                                                                 project, searchScope,
                                                                                 JSQualifiedNamedElement.class);
    ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    JSQualifiedNamedElement resultFromSourceContent = null;
    JSQualifiedNamedElement resultFromLibraries = null;
    long resultFromLibrariesTimestamp = 0;

    for (JSQualifiedNamedElement classCandidate : candidates) {
      if (!(classCandidate instanceof JSQualifiedNamedElement)) continue;
      if (JSResolveUtil.isConstructorFunction(classCandidate)) continue;
      JSQualifiedNamedElement clazz = classCandidate;

      if (link.equals(clazz.getQualifiedName())) {
        PsiFile file = clazz.getContainingFile();
        if (!file.getLanguage().isKindOf(JavaScriptSupportLoader.ECMA_SCRIPT_L4)) continue;
        VirtualFile vFile = file.getVirtualFile();
        if (clazzShouldBeTakenFromOurLibrary &&
            !JavaScriptIndex.ECMASCRIPT_JS2.equals(vFile.getName()) // object from swf do not contain necessary members!
          ) {
          continue;
        }
        if (!allowFileLocalSymbols && ActionScriptResolveUtil.isFileLocalSymbol(clazz)) {
          continue;
        }

        if (projectFileIndex.isInSourceContent(vFile)) {
          // the absolute preference is for classes from sources
          resultFromSourceContent = clazz;
          continue;
        }

        // choose the right class in the same way as compiler does: with the latest timestamp in catalog.xml file
        if (resultFromLibraries == null) {
          resultFromLibraries = clazz;
          // do not initialize resultFromLibrariesTimestamp here, it is expensive and may be not required if only 1 candidate
        }
        else if (JSCommonTypeNames.VECTOR_CLASS_NAME.equals(link)) {
          if (clazz instanceof JSClass && resultFromLibraries instanceof JSClass &&
              ((JSClass)clazz).getFunctions().length > ((JSClass)resultFromLibraries).getFunctions().length) {
            resultFromLibraries = clazz;
          }
        }
        else {
          if (resultFromLibrariesTimestamp == 0) {
            // was not initialized yet
            resultFromLibrariesTimestamp = getResolveResultTimestamp(resultFromLibraries);
          }

          final long classTimestamp = getResolveResultTimestamp(clazz);
          if (classTimestamp > resultFromLibrariesTimestamp) {
            resultFromLibraries = clazz;
            resultFromLibrariesTimestamp = classTimestamp;
          }
        }
      }
    }

    PsiElement result = resultFromSourceContent != null ? resultFromSourceContent : resultFromLibraries;
    if (result == null) {
      String className = link.substring(link.lastIndexOf('.') + 1);
      if (className.length() > 0 && !isBuiltInClassName(className) &&
          (Character.isLetter(className.charAt(0)) || '_' == className.charAt(0))) {
        // TODO optimization, remove when packages will be properly handled
        result = findClassByQNameViaHelper(link, project, className, searchScope);
      }
    }
    return result;
  }

  private static boolean isBuiltInClassName(final String className) {
    return OBJECT_CLASS_NAME.equals(className) ||
           BOOLEAN_CLASS_NAME.equals(className) ||
           FUNCTION_CLASS_NAME.equals(className) ||
           STRING_CLASS_NAME.equals(className);
  }

  @Nullable
  private static PsiElement findClassByQNameViaHelper(final String link,
                                                      final Project project,
                                                      final String className,
                                                      final GlobalSearchScope scope) {
    for (JSResolveHelper helper : Extensions.getExtensions(JSResolveHelper.EP_NAME)) {
      PsiElement result = helper.findClassByQName(link, project, className, scope);
      if (result != null) return result;
    }
    return null;
  }
}

