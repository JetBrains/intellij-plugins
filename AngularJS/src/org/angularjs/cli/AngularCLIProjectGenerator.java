package org.angularjs.cli;

import com.intellij.execution.filters.Filter;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.lang.javascript.boilerplate.NpmPackageProjectGenerator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import icons.AngularJSIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;

/**
 * @author Dennis.Ushakov
 */
public class AngularCLIProjectGenerator extends NpmPackageProjectGenerator {
  public static final String PACKAGE_NAME = "@angular/cli";
  private static final Key<Boolean> NG4 = Key.create("angular.cli.ng4");

  @Nls
  @NotNull
  @Override
  public String getName() {
    return "Angular CLI";
  }

  @Override
  public String getDescription() {
    return "The Angular2 CLI makes it easy to create an application that already works, right out of the box. It already follows Angular best practices!";
  }

  @Override
  public Icon getIcon() {
    return AngularJSIcons.Angular2;
  }

  @Override
  protected void customizeModule(@NotNull VirtualFile baseDir, ContentEntry entry) {
    if (entry != null) {
      AngularJSProjectConfigurator.excludeDefault(baseDir, entry);
    }
  }

  @Override
  @NotNull
  protected String[] generatorArgs(@NotNull Project project, @NotNull VirtualFile baseDir) {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  @Override
  @NotNull
  protected String[] generatorArgs(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull Settings settings) {
    if (settings.getUserData(NG4) == Boolean.TRUE) {
      return new String[]{"new", baseDir.getName(), "--dir=.", "--ng4"};
    }
    return new String[]{"new", baseDir.getName(), "--dir=."};
  }

  @NotNull
  @Override
  protected Filter[] filters(@NotNull Project project, @NotNull VirtualFile baseDir) {
    return new Filter[] {new AngularCLIFilter(project, baseDir.getPath())};
  }

  @Override
  @NotNull
  protected String executable(String path) {
    return ng(path);
  }

  @NotNull
  public static String ng(String path) {
    return path + File.separator + "bin" + File.separator + "ng";
  }

  @Override
  @NotNull
  protected String packageName() {
    return PACKAGE_NAME;
  }

  @Override
  @NotNull
  protected String presentablePackageName() {
    return "Angular &CLI:";
  }

  @NotNull
  @Override
  public GeneratorPeer<Settings> createPeer() {
    return new NpmPackageGeneratorPeer() {
      private JCheckBox ng4;

      @Override
      protected JPanel createPanel() {
        JPanel panel = super.createPanel();
        ng4 = new JCheckBox("Create Angular 4 project");
        panel.add(ng4);
        return panel;
      }

      @Override
      public void buildUI(@NotNull SettingsStep settingsStep) {
        super.buildUI(settingsStep);
        settingsStep.addSettingsComponent(ng4);
      }

      @NotNull
      @Override
      public Settings getSettings() {
        Settings settings = super.getSettings();
        settings.putUserData(NG4, ng4.isSelected());
        return settings;
      }
    };
  }
}
