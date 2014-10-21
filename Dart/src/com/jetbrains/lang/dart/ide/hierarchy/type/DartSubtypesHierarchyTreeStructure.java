package com.jetbrains.lang.dart.ide.hierarchy.type;

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.util.ArrayUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.index.DartInheritanceIndex;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartType;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DartSubtypesHierarchyTreeStructure extends HierarchyTreeStructure {
  private final String myCurrentScopeType;

  protected DartSubtypesHierarchyTreeStructure(final Project project,
                                               final HierarchyNodeDescriptor descriptor,
                                               final String currentScopeType) {
    super(project, descriptor);
    myCurrentScopeType = currentScopeType;
  }

  public DartSubtypesHierarchyTreeStructure(final Project project, final DartClass dartClass, final String currentScopeType) {
    super(project, new DartTypeHierarchyNodeDescriptor(project, null, dartClass, true));
    myCurrentScopeType = currentScopeType;
  }

  @NotNull
  protected final Object[] buildChildren(@NotNull final HierarchyNodeDescriptor descriptor) {
    final DartClass dartClass = ((DartTypeHierarchyNodeDescriptor)descriptor).getDartClass();
    if (dartClass == null) return ArrayUtil.EMPTY_OBJECT_ARRAY;

    if (DartResolveUtil.OBJECT.equals(dartClass.getName())) {
      return new Object[]{DartBundle.message("dart.hierarchy.object")};
    }
    final List<DartClass> subClasses = DartInheritanceIndex.getItemsByName(dartClass);
    final List<DartTypeHierarchyNodeDescriptor> descriptors = new ArrayList<DartTypeHierarchyNodeDescriptor>(subClasses.size());
    for (DartClass aSubClass : subClasses) {
      if (doesDirectlySubclass(dartClass, aSubClass)) {
        descriptors.add(new DartTypeHierarchyNodeDescriptor(myProject, descriptor, aSubClass, false));
      }
    }
    return descriptors.toArray(new HierarchyNodeDescriptor[descriptors.size()]);
  }

  private static boolean doesDirectlySubclass(final DartClass dartClass, final DartClass subClass) {
    final DartType type = subClass.getSuperClass();
    if (type != null && dartClass.equals(DartResolveUtil.resolveClassByType(type).getDartClass())) {
      return true;
    }
    final List<DartType> interfacesAndMixins = DartResolveUtil.getImplementsAndMixinsList(subClass);
    for (DartType type1 : interfacesAndMixins) {
      if (type1 != null && dartClass.equals(DartResolveUtil.resolveClassByType(type1).getDartClass())) {
        return true;
      }
    }
    return false;
  }
}
