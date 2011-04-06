package com.intellij.javascript.flex.css;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.psi.css.CssSupportLoader;
import com.intellij.util.ui.tree.AbstractFileTreeTable;
import com.intellij.util.ui.tree.LanguagePerFileConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.util.Collection;

/**
 * @author Eugene.Kudelevsky
 */
public class CssDialectsConfigurable extends LanguagePerFileConfigurable<CssDialect> {
  public CssDialectsConfigurable(Project project) {
    super(project, CssDialect.class, CssDialectMappings.getInstance(project), FlexBundle.message("css.dialects.caption"),
          FlexBundle.message("css.dialects.column.title"), FlexBundle.message("css.dialects.override.warning.text"),
          FlexBundle.message("css.dialects.override.warning.title"));
  }

  @Nls
  public String getDisplayName() {
    return FlexBundle.message("css.dialects.title");
  }

  @Nullable
  public Icon getIcon() {
    return CssSupportLoader.CSS_FILE_TYPE.getIcon();
  }

  @Override
  public JComponent createComponent() {
    final JComponent jComponent = super.createComponent();
    final AbstractFileTreeTable<CssDialect> treeView = getTreeView();
    treeView.setRootVisible(false);

    ((DefaultTreeModel)treeView.getTableModel()).setRoot(new AbstractFileTreeTable.ProjectRootNode(myProject) {
      @Override
      protected void appendChildrenTo(final Collection<AbstractFileTreeTable.ConvenientNode> children) {
        final Module[] modules = ModuleManager.getInstance(myProject).getModules();

        for (Module module : modules) {
          if (FlexUtils.isFlexModuleOrContainsFlexFacet(module)) {
            final VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();

            for (VirtualFile contentRoot : contentRoots) {
              children.add(new AbstractFileTreeTable.FileNode(contentRoot, myProject, new VirtualFileFilter() {
                @Override
                public boolean accept(VirtualFile file) {
                  return file.isDirectory() || canBeConfigured(file);
                }
              }));
            }
          }
        }
      }
    });

    // shouldn't actually happen because we don't show the configurable in this case
    treeView.getEmptyText().setText(FlexBundle.message("css.dialects.flex.modules.not.found"));

    return jComponent;
  }

  public static boolean canBeConfigured(@NotNull VirtualFile file) {
    return file.getFileType() == CssSupportLoader.CSS_FILE_TYPE;
  }

  @Nullable
  @NonNls
  public String getHelpTopic() {
    return null;
  }

  protected String visualize(@NotNull final CssDialect dialect) {
    return dialect.getDisplayName();
  }
}
