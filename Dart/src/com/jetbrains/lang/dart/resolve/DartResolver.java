package com.jetbrains.lang.dart.resolve;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
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

  @Nullable
  @Override
  public List<? extends PsiElement> resolve(@NotNull DartReference reference, boolean incompleteCode) {
    if (reference instanceof DartThisExpression) {
      return toResult(PsiTreeUtil.getParentOfType(reference, DartClass.class));
    }
    if (reference instanceof DartParameterNameReferenceExpression) {
      final DartCallExpression callExpression = PsiTreeUtil.getParentOfType(reference, DartCallExpression.class);
      final DartExpression expression = callExpression != null ? callExpression.getExpression() : null;
      final PsiElement target = expression instanceof DartReference ? ((DartReference)expression).resolve() : null;
      final DartFormalParameterList parameters =
        PsiTreeUtil.getChildOfType(target != null ? target.getParent() : null, DartFormalParameterList.class);
      return toResult(DartResolveUtil.findParameterByName(parameters, reference.getText()));
    }
    if (DartResolveUtil.aloneOrFirstInChain(reference)) {
      final List<? extends PsiElement> result = resolveSimpleReference(reference);
      // If "reference" resolves to an import prefix, find the exact one.
      // There might be more than one import statement with the same prefix name.
      if (result.size() == 1 &&
          result.get(0).getParent() instanceof DartImportStatement &&
          reference.getParent() instanceof DartReference) {
        final DartReference parent = (DartReference)reference.getParent();
        // This has a side effect - the exact prefix if set into "reference" as a user data.
        parent.resolve();
        // Extract the exact prefix and return.
        final DartComponentName importPrefixName = DartResolveUtil.getImportPrefixName(reference);
        if (importPrefixName != null) {
          return toResult(importPrefixName);
        }
      }
      // Not an import prefix - a local variable, type, etc.
      return result;
    }
    final DartReference leftReference = DartResolveUtil.getLeftReference(reference);
    // reference [node, node]
    final DartReference[] references = PsiTreeUtil.getChildrenOfType(reference, DartReference.class);
    if (references != null && references.length == 2) {
      // import prefix
      final List<DartComponentName> result = new SmartList<DartComponentName>();
      final String componentName = references[1].getCanonicalText();
      DartResolveUtil.processDeclarationsInImportedFileByImportPrefix(reference, references[0],
                                                                      new DartResolveProcessor(result, componentName), componentName);
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
      final String componentName = reference.getCanonicalText();
      DartResolveUtil.processDeclarationsInImportedFileByImportPrefix(reference, leftReference,
                                                                      new DartResolveProcessor(result, componentName), componentName);
      if (!result.isEmpty()) {
        return result;
      }
    }

    return null;
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

  @NotNull
  private static List<? extends PsiElement> resolveSimpleReference(@NotNull DartReference reference) {
    final List<? extends PsiElement> result = resolveSimpleReference(reference, reference.getCanonicalText());
    final PsiElement parent = reference.getParent();
    final PsiElement superParent = parent.getParent();
    final boolean isSimpleConstructor = parent instanceof DartType
                                        && superParent instanceof DartNewExpression
                                        && ((DartNewExpression)superParent).getReferenceExpression() == null;
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
