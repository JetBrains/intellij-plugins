package com.jetbrains.lang.dart.resolve;

import com.google.dart.server.generated.types.NavigationRegion;
import com.google.dart.server.generated.types.NavigationTarget;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DartResolver implements ResolveCache.AbstractResolver<DartReference, List<? extends PsiElement>> {
  public static final DartResolver INSTANCE = new DartResolver();

  public static final boolean USE_SERVER = false;

  @Nullable
  @Override
  public List<? extends PsiElement> resolve(@NotNull DartReference reference, boolean incompleteCode) {
    if (USE_SERVER) {
      final PsiFile refPsiFile = reference.getContainingFile();
      //if (!DartAnalysisServerAnnotator.serverReadyForRequest(refPsiFile)) {
      //  return null;
      //}
      final VirtualFile refVirtualFile = refPsiFile.getVirtualFile();
      if (refVirtualFile != null) {
        if (isIdentifier(reference)) {
          Project project = reference.getProject();
          final int refOffset = reference.getTextOffset();
          final List<NavigationRegion> regions = DartAnalysisServerService.getInstance().getNavigation(refVirtualFile);
          for (NavigationRegion region : regions) {
            if (region.containsInclusive(refOffset)) {
              NavigationTarget target = region.getTargetObjects().get(0);
              return getResultForNavigationTarget(project, target);
            }
            if (region.getOffset() + region.getLength() > refOffset) {
              break;
            }
          }
        }
      }
      return null;
    }

    if (reference instanceof DartThisExpression) {
      return toResult(PsiTreeUtil.getParentOfType(reference, DartClass.class));
    }
    if (reference instanceof DartParameterNameReferenceExpression) {
      PsiElement target;
      {
        final DartCallExpression callExpression = PsiTreeUtil.getParentOfType(reference, DartCallExpression.class);
        final DartExpression expression = callExpression != null ? callExpression.getExpression() : null;
        target = expression instanceof DartReference ? ((DartReference)expression).resolve() : null;
        target = target != null ? target.getParent() : null;
      }
      if (target == null) {
        final DartNewExpression newExpression = PsiTreeUtil.getParentOfType(reference, DartNewExpression.class);
        if (newExpression != null) {
          target = DartResolveUtil.findConstructorDeclaration(newExpression);
        }
      }
      final DartFormalParameterList parameters = PsiTreeUtil.getChildOfType(target, DartFormalParameterList.class);
      return toResult(DartResolveUtil.findParameterByName(parameters, reference.getText()));
    }
    if (DartResolveUtil.aloneOrFirstInChain(reference)) {
      return resolveSimpleReference(reference);
    }
    final DartReference leftReference = DartResolveUtil.getLeftReference(reference);
    // reference [node, node]
    final DartReference[] references = PsiTreeUtil.getChildrenOfType(reference, DartReference.class);
    if (references != null && references.length == 2) {
      // import prefix
      final List<DartComponentName> result = new SmartList<DartComponentName>();
      final String importPrefix = references[0].getCanonicalText();
      final String componentName = references[1].getCanonicalText();
      DartResolveUtil
        .processDeclarationsInImportedFileByImportPrefix(reference, importPrefix, new DartResolveProcessor(result, componentName),
                                                         componentName);
      if (!result.isEmpty()) {
        return result;
      }

      return toResult(references[1].resolve());
    }
    else if (leftReference != null) {
      final DartClassResolveResult classResolveResult = leftReference.resolveDartClass();
      final DartClass dartClass = classResolveResult.getDartClass();
      if (dartClass != null) {
        final String name = reference.getCanonicalText();
        final DartComponent subComponent = leftReference instanceof DartType
                                           ? dartClass.findNamedConstructor(name)
                                           : filterAccess(reference, dartClass.findMembersByName(name));
        return toResult(subComponent == null ? null : subComponent.getComponentName());
      }

      // import prefix
      final List<DartComponentName> result = new SmartList<DartComponentName>();
      final String importPrefix = leftReference.getCanonicalText();
      final String componentName = reference.getCanonicalText();
      DartResolveUtil
        .processDeclarationsInImportedFileByImportPrefix(reference, importPrefix, new DartResolveProcessor(result, componentName),
                                                         componentName);
      if (!result.isEmpty()) {
        return result;
      }
    }

    return null;
  }

  @Nullable
  private static List<? extends PsiElement> getResultForNavigationTarget(Project project, NavigationTarget target) {
    String targetPath = target.getFile();
    VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(targetPath);
    if (virtualFile != null) {
      PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
      if (file != null) {
        int targetOffset = target.getOffset();
        for (int i = 0; i < 2; i++) {
          Class<? extends PsiElement> clazz = DartComponentName.class;
          if (i == 1) {
            clazz = DartReferenceExpression.class;
          }
          PsiElement elementAt = PsiTreeUtil.findElementOfClassAtOffset(file, targetOffset, clazz, false);
          if (elementAt != null) {
            return toResult(elementAt);
          }
        }
      }
    }
    return null;
  }

  private static boolean isIdentifier(@NotNull DartReference reference) {
    return reference.getChildren().length == 1 && reference.getChildren()[0] instanceof DartId;
  }

  @NotNull
  private static List<PsiElement> toResult(@Nullable PsiElement element) {
    if (element == null) {
      return Collections.emptyList();
    }
    return new SmartList<PsiElement>(element);
  }

  @Nullable
  private static DartComponent filterAccess(PsiElement element, List<DartComponent> components) {
    final boolean lValue = DartResolveUtil.isLValue(element);
    return ContainerUtil.find(components, new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        if (lValue && component.isSetter()) {
          return true;
        }
        else if (!lValue && component.isGetter()) {
          return true;
        }
        boolean isGetterOrSetter = component.isSetter() || component.isGetter();
        return !isGetterOrSetter;
      }
    });
  }

  private static List<? extends PsiElement> resolveSimpleReference(@NotNull DartReference reference) {
    final List<? extends PsiElement> result = resolveSimpleReference(reference, reference.getCanonicalText());
    final PsiElement parent = reference.getParent();
    final PsiElement superParent = parent.getParent();
    final boolean isSimpleConstructor = parent instanceof DartType &&
                                        superParent instanceof DartNewExpression &&
                                        ((DartNewExpression)superParent).getReferenceExpression() == null;
    if (!isSimpleConstructor || result.isEmpty()) {
      return result;
    }
    final List<PsiElement> filteredResult = new ArrayList<PsiElement>(result.size());
    for (PsiElement element : result) {
      final PsiElement elementParent = element.getParent();
      if (element instanceof DartComponentName && elementParent instanceof DartClass) {
        final DartComponent component = ((DartClass)elementParent).findNamedConstructor(reference.getCanonicalText());
        if (component != null && DartComponentType.typeOf(component) == DartComponentType.CONSTRUCTOR) {
          filteredResult.add(component.getComponentName());
          continue;
        }
      }
      filteredResult.add(element);
    }
    return filteredResult;
  }

  @NotNull
  public static List<? extends PsiElement> resolveSimpleReference(@NotNull final PsiElement scopeElement, @NotNull final String name) {
    final List<DartComponentName> result = new ArrayList<DartComponentName>();
    // local
    final DartResolveProcessor dartResolveProcessor = new DartResolveProcessor(result, name, DartResolveUtil.isLValue(scopeElement));
    PsiTreeUtil.treeWalkUp(dartResolveProcessor, scopeElement, null, ResolveState.initial());

    // supers
    final DartClass dartClass = PsiTreeUtil.getParentOfType(scopeElement, DartClass.class);
    final boolean inClass = PsiTreeUtil.getParentOfType(scopeElement, DartClassBody.class, false) != null;
    if (result.isEmpty() && dartClass != null && inClass) {
      final DartComponent field = filterAccess(scopeElement, dartClass.findMembersByName(name));
      if (field != null) {
        return toResult(field.getComponentName());
      }
    }

    // global
    if (result.isEmpty()) {
      final List<VirtualFile> libraryFiles = DartResolveUtil.findLibrary(scopeElement.getContainingFile());
      DartResolveUtil.processTopLevelDeclarations(scopeElement, dartResolveProcessor, libraryFiles, name);
    }

    return result;
  }
}
