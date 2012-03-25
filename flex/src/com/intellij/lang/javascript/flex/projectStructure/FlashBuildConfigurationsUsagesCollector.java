package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.internal.statistic.AbstractApplicationUsagesCollector;
import com.intellij.internal.statistic.CollectUsagesException;
import com.intellij.internal.statistic.beans.GroupDescriptor;
import com.intellij.internal.statistic.beans.UsageDescriptor;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.project.Project;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * User: ksafonov
 */
public class FlashBuildConfigurationsUsagesCollector extends AbstractApplicationUsagesCollector {

  private static final String GROUP_ID = "Flash build configurations";

  @NotNull
  @Override
  public Set<UsageDescriptor> getProjectUsages(@NotNull final Project project) throws CollectUsagesException {
    Set<String> usedBcs = new HashSet<String>();
    for (Module module : ModuleManager.getInstance(project).getModules()) {
      if (ModuleType.get(module) != FlexModuleType.getInstance()) {
        continue;
      }

      for (FlexIdeBuildConfiguration bc : FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
        usedBcs.add(bc.getStatisticsEntry());
      }
    }
    return ContainerUtil.map2Set(usedBcs, new Function<String, UsageDescriptor>() {
      @Override
      public UsageDescriptor fun(String s) {
        return new UsageDescriptor(s, 1);
      }
    });
  }

  @NotNull
  @Override
  public GroupDescriptor getGroupId() {
    return GroupDescriptor.create(GROUP_ID, GroupDescriptor.DEFAULT_PRIORITY);
  }
}
