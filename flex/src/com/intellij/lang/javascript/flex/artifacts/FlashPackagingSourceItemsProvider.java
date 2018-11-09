package com.intellij.lang.javascript.flex.artifacts;

import com.intellij.flex.model.bc.OutputType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.FlexBuildConfigurationsExtension;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.FlexProjectConfigurationEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.packaging.ui.ArtifactEditorContext;
import com.intellij.packaging.ui.PackagingSourceItem;
import com.intellij.packaging.ui.PackagingSourceItemsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FlashPackagingSourceItemsProvider extends PackagingSourceItemsProvider {

  @Override
  @NotNull
  public Collection<? extends PackagingSourceItem> getSourceItems(final @NotNull ArtifactEditorContext editorContext,
                                                                  final @NotNull Artifact artifact,
                                                                  final @Nullable PackagingSourceItem parent) {
    if (parent == null) {
      return createFlashModuleSourceItems(editorContext);
    }
    else if (parent instanceof FlashModuleSourceItem) {
      return createFlashBCOutputSourceItems(((FlashModuleSourceItem)parent).getModule());
    }

    return Collections.emptyList();
  }

  private static Collection<? extends PackagingSourceItem> createFlashModuleSourceItems(final ArtifactEditorContext editorContext) {
    final List<PackagingSourceItem> result = new ArrayList<>();

    for (Module module : editorContext.getModulesProvider().getModules()) {
      if (ModuleType.get(module) == FlexModuleType.getInstance()) {
        result.add(new FlashModuleSourceItem(module));
      }
    }

    return result;
  }

  private static Collection<? extends PackagingSourceItem> createFlashBCOutputSourceItems(final Module module) {
    final List<PackagingSourceItem> result = new ArrayList<>();

    int orderNumber = 0;

    final FlexProjectConfigurationEditor configEditor = FlexBuildConfigurationsExtension.getInstance().getConfigurator().getConfigEditor();
    assert configEditor != null; // because Project Structure is open

    for (FlexBuildConfiguration bc : configEditor.getConfigurations(module)) {
      final String outputFilePath = bc.getActualOutputFilePath().toLowerCase();
      if (!outputFilePath.endsWith(".swf") && !outputFilePath.endsWith(".swc")) {
        continue; // BC is not configured properly yet
      }

      result.add(new FlashBCOutputSourceItem(bc, FlashBCOutputSourceItem.Type.OutputFile, orderNumber++));
      if (bc.getOutputType() == OutputType.Application && bc.getTargetPlatform() == TargetPlatform.Web && bc.isUseHtmlWrapper()) {
        result.add(new FlashBCOutputSourceItem(bc, FlashBCOutputSourceItem.Type.OutputFileAndHtmlWrapper, orderNumber++));
      }
      result.add(new FlashBCOutputSourceItem(bc, FlashBCOutputSourceItem.Type.OutputFolderContents, orderNumber++));
    }

    return result;
  }
}
