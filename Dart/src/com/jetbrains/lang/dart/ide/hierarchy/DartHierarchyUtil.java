package com.jetbrains.lang.dart.ide.hierarchy;

import com.intellij.ide.hierarchy.HierarchyBrowserManager;
import com.intellij.ide.util.treeView.AlphaComparator;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.marker.DartServerOverrideMarkerProvider;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.dartlang.analysis.server.protocol.Element;
import org.dartlang.analysis.server.protocol.Location;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class DartHierarchyUtil {
  private static final Comparator<NodeDescriptor> NODE_DESCRIPTOR_COMPARATOR = new Comparator<NodeDescriptor>() {
    public int compare(final NodeDescriptor first, final NodeDescriptor second) {
      return first.getIndex() - second.getIndex();
    }
  };

  private DartHierarchyUtil() {
  }

  @Nullable
  public static DartClass findDartClass(@NotNull final Project project, @NotNull final TypeHierarchyItem item) {
    final Element classElement = item.getClassElement();
    final Location location = classElement.getLocation();
    final DartComponent component = DartServerOverrideMarkerProvider.findDartComponent(project, location);
    return component instanceof DartClass ? (DartClass)component : null;
  }

  public static Comparator<NodeDescriptor> getComparator(Project project) {
    if (HierarchyBrowserManager.getInstance(project).getState().SORT_ALPHABETICALLY) {
      return AlphaComparator.INSTANCE;
    }
    else {
      return NODE_DESCRIPTOR_COMPARATOR;
    }
  }

  @NotNull
  public static List<TypeHierarchyItem> getTypeHierarchyItems(@NotNull DartClass dartClass) {
    final VirtualFile file = dartClass.getContainingFile().getVirtualFile();
    final int nameOffset = dartClass.getTextRange().getStartOffset();
    return DartAnalysisServerService.getInstance().search_getTypeHierarchy(file, nameOffset, false);
  }
}
