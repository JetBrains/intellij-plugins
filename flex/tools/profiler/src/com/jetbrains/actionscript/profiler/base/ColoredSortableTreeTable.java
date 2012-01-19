package com.jetbrains.actionscript.profiler.base;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.FileColorManager;
import com.intellij.util.ui.ColumnInfo;
import com.jetbrains.actionscript.profiler.util.ResolveUtil;
import gnu.trove.THashMap;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.Map;

/**
 * @author: Fedor.Korotkov
 */
public class ColoredSortableTreeTable extends BaseSortableTreeTable {
  @Nullable
  private final Project project;
  @Nullable
  private final ProjectFileIndex projectFileIndex;

  private final Map<String, Color> qName2ColorCache = new THashMap<String, Color>();
  private final Map<String, Color> path2ColorCache = new THashMap<String, Color>();

  public ColoredSortableTreeTable(ColumnInfo[] columns, @Nullable Project project) {
    super(columns);
    this.project = project;
    projectFileIndex = project == null ? null : ProjectFileIndex.SERVICE.getInstance(project);
  }

  public void clearColorCaches() {
    qName2ColorCache.clear();
    path2ColorCache.clear();
  }

  @Override
  public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    final JComponent jComponent = (JComponent)super.prepareRenderer(renderer, row, column);
    if (jComponent.getBackground() == getSelectionBackground()) {
      return jComponent;
    }
    final Object value = getValueAt(row, 0);

    Color color = tryGetBackgroundByPath(value);
    if (color == null) {
      color = tryGetBackgroundByQName(value);
    }

    jComponent.setOpaque(true);
    jComponent.setBackground(color);

    return jComponent;
  }

  @Nullable
  private Color tryGetBackgroundByQName(Object value) {
    QNameProducer qNameProducer = null;

    if (value instanceof QNameProducer) {
      qNameProducer = (QNameProducer)value;
    }

    if (value instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode)value).getUserObject() instanceof QNameProducer) {
      qNameProducer = (QNameProducer)((DefaultMutableTreeNode)value).getUserObject();
    }
    if (project == null || qNameProducer == null || qNameProducer.getQName() == null) {
      return null;
    }
    Color color = qName2ColorCache.get(qNameProducer.getQName());
    if (color != null) {
      return color;
    }
    final PsiElement psiElement = ResolveUtil.findClassByQName(qNameProducer.getQName(), GlobalSearchScope.allScope(project));
    if (psiElement != null) {
      color = FileColorManager.getInstance(project).getRendererBackground(psiElement.getContainingFile());
    }
    qName2ColorCache.put(qNameProducer.getQName(), color);
    return color;
  }

  @Nullable
  private Color tryGetBackgroundByPath(Object value) {
    FilePathProducer filePathProducer = null;
    if (value instanceof FilePathProducer) {
      filePathProducer = (FilePathProducer)value;
    }
    if (value instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode)value).getUserObject() instanceof FilePathProducer) {
      filePathProducer = (FilePathProducer)((DefaultMutableTreeNode)value).getUserObject();
    }
    if (projectFileIndex == null || filePathProducer == null || filePathProducer.getFilePath() == null) {
      return null;
    }
    Color color = path2ColorCache.get(filePathProducer.getFilePath());
    final VirtualFile vf = VirtualFileManager.getInstance().findFileByUrl(VfsUtil.pathToUrl(filePathProducer.getFilePath()));
    if (vf != null && projectFileIndex.isInSource(vf)) {
      color = FileColorManager.getInstance(project).getRendererBackground(vf);
    }
    path2ColorCache.put(filePathProducer.getFilePath(), color);
    return color;
  }
}
