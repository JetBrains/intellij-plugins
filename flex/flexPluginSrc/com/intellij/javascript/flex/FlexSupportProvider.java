package com.intellij.javascript.flex;

import com.intellij.ide.util.frameworkSupport.FrameworkSupportConfigurable;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportProvider;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModel;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.flex.FlexFacet;
import com.intellij.lang.javascript.flex.FlexFacetType;
import com.intellij.lang.javascript.flex.FlexModuleBuilder;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.sdk.FlexSdkComboBoxWithBrowseButton;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;

/**
 * @author Maxim.Mossienko
 */
public class FlexSupportProvider extends FrameworkSupportProvider {
  protected FlexSupportProvider() {
    super("Support Provider:"+FlexFacet.ID.toString(), JSBundle.message("add.flex.facet.title"));
  }

  @NotNull
  public FrameworkSupportConfigurable createConfigurable(final @NotNull FrameworkSupportModel model) {
    return new FlexSupportConfigurable();
  }

  @Override
  public Icon getIcon() {
    return FlexFacetType.ourFlexIcon;
  }

  public boolean isEnabledForModuleType(@NotNull ModuleType moduleType) {
    return moduleType instanceof JavaModuleType;
  }

  private static class FlexSupportConfigurable extends FrameworkSupportConfigurable {
    private JPanel myMainPanel;
    private FlexSdkComboBoxWithBrowseButton myFlexSdkComboWithBrowse;

    public JComponent getComponent() {
      return myMainPanel;
    }

    public void addSupport(@NotNull final Module module, @NotNull final ModifiableRootModel model, final @Nullable Library library) {
      final Sdk flexSdk = myFlexSdkComboWithBrowse.getSelectedSdk();
      final FlexFacet flexFacet = FlexUtils.addFlexFacet(module, flexSdk, model);

      String mainClassName = module.getName().replaceAll("[^\\p{Alnum}]", "_");
      if (mainClassName.length() > 0 && Character.isLowerCase(mainClassName.charAt(0))) {
        mainClassName = Character.toUpperCase(mainClassName.charAt(0)) + mainClassName.substring(1);
      }
      final FlexBuildConfiguration config = FlexBuildConfiguration.getInstance(flexFacet);
      config.MAIN_CLASS = mainClassName;
      config.OUTPUT_FILE_NAME = mainClassName + ".swf";
      final Ref<String> mainClassNameRef = Ref.create(mainClassName);

      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          if (module.isDisposed()) return;
          final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
          final VirtualFile[] contentRoots = rootManager.getContentRoots();
          final VirtualFile[] sourceRoots = rootManager.getSourceRoots();
          if (contentRoots.length == 0 || sourceRoots.length == 0) return;

          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              try {
                FlexUtils
                  .setupFlexConfigFileAndSampleCode(module, config, flexSdk, null, contentRoots[0], mainClassNameRef.get() + ".mxml",
                                                    sourceRoots[0]);
                FlexModuleBuilder.createFlexRunConfiguration(module, config.OUTPUT_FILE_NAME);
              }
              catch (IOException ex) {
                throw new RuntimeException(ex);
              }
            }
          });
        }
      }, ModalityState.NON_MODAL);
    }
  }
}
