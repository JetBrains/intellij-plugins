package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.formove.FilePathComparator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vcs.AbstractFilterChildren;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.application.PerforceVcs;

import java.io.File;
import java.util.*;

public class P4ConnectionCalculator {
  private final Project myProject;
  private static final Logger LOG = Logger.getInstance(P4ConnectionCalculator.class);
  private PerforceMultipleConnections myMultipleConnections;

  public P4ConnectionCalculator(Project project) {
    myProject = project;
  }

  public void execute() {
    final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
    final PerforceVcs vcs = PerforceVcs.getInstance(myProject);

    final P4EnvHelper p4EnvHelper = P4EnvHelper.getConfigHelper(myProject);
    p4EnvHelper.reset();

    final String p4ConfigFileName = p4EnvHelper.getP4Config();
    LOG.debug("Using p4config file name: " + p4ConfigFileName);

    final List<VirtualFile> detailedVcsMappings = new ArrayList<>(Arrays.asList(vcsManager.getRootsUnderVcs(vcs)));

    final Map<VirtualFile, File> configsMap = p4ConfigFileName == null ? Collections.emptyMap()
                                                                       : fillConfigsMap(detailedVcsMappings, p4ConfigFileName);
    final Map<VirtualFile,P4ConnectionParameters> connectionSettings = new HashMap<>();
    final P4ConnectionParameters defaultParameters = p4EnvHelper.getDefaultParams();

    ApplicationManager.getApplication().runReadAction(() -> {
      // find files
      filterByConfigFiles(configsMap, detailedVcsMappings);

      for (VirtualFile mapping : detailedVcsMappings) {
        final File configParentDir = configsMap.get(mapping);
        final P4ConnectionParameters value;
        if (configParentDir == null) {
          // empty parameters
          value = new P4ConnectionParameters();
          value.setNoConfigFound(true);
        }
        else {
          assert p4ConfigFileName != null;
          // todo: working incorrect when nested p4configs
          value = P4ParamsCalculator.getParametersFromConfig(configParentDir, p4ConfigFileName);
        }
        value.setConfigFileName(p4ConfigFileName);
        p4EnvHelper.fillDefaultValues(value);

        LOG.debug("Using " + value + " for " + mapping);
        connectionSettings.put(mapping, value);
      }
    });

    // filter what left
    filterSimilarConfigFiles(connectionSettings, detailedVcsMappings);

    //copy what left
    connectionSettings.keySet().retainAll(detailedVcsMappings);
    configsMap.keySet().retainAll(detailedVcsMappings);

    myMultipleConnections = new PerforceMultipleConnections(p4ConfigFileName, defaultParameters, connectionSettings, configsMap);
  }

  public @NotNull PerforceMultipleConnections getMultipleConnections() {
    return myMultipleConnections;
  }

  @Contract(mutates = "param2")
  private static void filterSimilarConfigFiles(final Map<VirtualFile, P4ConnectionParameters> connectionSettings,
                                               List<VirtualFile> detailedVcsMappings) {
    final AbstractFilterChildren<VirtualFile> filter = new AbstractFilterChildren<>() {
      @Override
      protected void sortAscending(List<? extends VirtualFile> list) {
        list.sort(FilePathComparator.getInstance());
      }

      @Override
      protected boolean isAncestor(VirtualFile parent, VirtualFile child) {
        final P4ConnectionParameters parentSettings = connectionSettings.get(parent);
        final P4ConnectionParameters childSettings = connectionSettings.get(child);

        if (parentSettings.hasProblems() || childSettings.hasProblems()) return false;
        if (!VfsUtilCore.isAncestor(parent, child, false)) return false;

        return parentSettings.equals(childSettings);
      }
    };

    filter.doFilter(detailedVcsMappings);
  }

  private static void filterByConfigFiles(final Map<VirtualFile, File> configsMap, List<VirtualFile> detailedVcsMappings) {
    final AbstractFilterChildren<VirtualFile> filter = new AbstractFilterChildren<>() {
      @Override
      protected void sortAscending(List<? extends VirtualFile> list) {
        list.sort(FilePathComparator.getInstance());
      }

      @Override
      protected boolean isAncestor(VirtualFile parent, VirtualFile child) {
        if (!Comparing.equal(configsMap.get(parent), configsMap.get(child))) return false;
        if (!VfsUtilCore.isAncestor(parent, child, false)) return false;
        return true;
      }
    };

    filter.doFilter(detailedVcsMappings);
  }

  private static Map<VirtualFile, File> fillConfigsMap(List<VirtualFile> detailedVcsMappings, @NotNull String p4ConfigFileName) {
    P4ConfigHelper p4ConfigHelper = new P4ConfigHelper();
    Map<VirtualFile, File> result = new HashMap<>();
    for (VirtualFile vcsMapping : detailedVcsMappings) {
      result.put(vcsMapping, p4ConfigHelper.findDirWithP4ConfigFile(vcsMapping, p4ConfigFileName));
    }
    return result;
  }
}
