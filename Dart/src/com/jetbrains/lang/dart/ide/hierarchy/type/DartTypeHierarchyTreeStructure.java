package com.jetbrains.lang.dart.ide.hierarchy.type;

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.openapi.project.Project;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartType;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartResolveUtil;

import java.util.ArrayList;

public final class DartTypeHierarchyTreeStructure extends DartSubtypesHierarchyTreeStructure {

  public DartTypeHierarchyTreeStructure(final Project project, final DartClass dartClass, String currentScopeType) {
    super(project, buildHierarchyElement(project, dartClass), currentScopeType);
    setBaseElement(myBaseDescriptor); //to set myRoot
  }

  private static HierarchyNodeDescriptor buildHierarchyElement(final Project project, final DartClass dartClass) {
    HierarchyNodeDescriptor descriptor = null;
    final DartClass[] superClasses = createSuperClasses(dartClass);
    for (int i = superClasses.length - 1; i >= 0; i--) {
      final DartClass superClass = superClasses[i];
      final HierarchyNodeDescriptor newDescriptor = new DartTypeHierarchyNodeDescriptor(project, descriptor, superClass, false);
      if (descriptor != null) {
        descriptor.setCachedChildren(new HierarchyNodeDescriptor[]{newDescriptor});
      }
      descriptor = newDescriptor;
    }
    final HierarchyNodeDescriptor newDescriptor = new DartTypeHierarchyNodeDescriptor(project, descriptor, dartClass, true);
    if (descriptor != null) {
      descriptor.setCachedChildren(new HierarchyNodeDescriptor[]{newDescriptor});
    }
    return newDescriptor;
  }

  private static DartClass[] createSuperClasses(final DartClass dartClass) {
    if (!dartClass.isValid()) return new DartClass[0];
    final ArrayList<DartClass> superClasses = new ArrayList<DartClass>();
    DartType superType = dartClass.getSuperClass();
    while (superType != null && !DartResolveUtil.OBJECT.equals(superType.getReferenceExpression().getText())) {
      final DartClassResolveResult dartClassResolveResult = DartResolveUtil.resolveClassByType(superType);
      final DartClass dartClass1 = dartClassResolveResult.getDartClass();
      if (dartClass1 != null) {
        superClasses.add(dartClass1);
        superType = dartClass1.getSuperClass();
      }
    }
    return superClasses.toArray(new DartClass[superClasses.size()]);
  }
}
