package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.formove.FilePathComparator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vcs.AbstractFilterChildren;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.PerforcePhysicalConnectionParameters;
import org.jetbrains.idea.perforce.perforce.PerforcePhysicalConnectionParametersI;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import java.io.File;
import java.util.*;

public class P4ConnectionCalculator {
  private final Project myProject;
  private final P4ParamsCalculator myParamsCalculator;
  private static final Logger LOG = Logger.getInstance(P4ConnectionCalculator.class);
  private PerforceMultipleConnections myMultipleConnections;

  public P4ConnectionCalculator(Project project) {
    myProject = project;
    myParamsCalculator = new P4ParamsCalculator(myProject);
  }

  public void execute() {
    final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
    final PerforceVcs vcs = PerforceVcs.getInstance(myProject);
    final PerforceSettings settings = PerforceSettings.getSettings(myProject);
    final PerforcePhysicalConnectionParameters physicalParameters = settings.getPhysicalSettings(true);

    final List<VirtualFile> detailedVcsMappings = Registry.is("p4.new.project.mappings.handling")
                                                  ? new ArrayList<>(Arrays.asList(vcsManager.getRootsUnderVcs(vcs)))
                                                  : vcsManager.getDetailedVcsMappings(vcs);

    final P4ConfigHelper p4ConfigHelper = P4ConfigHelper.getConfigHelper(myProject);
    final String p4ConfigFileName = p4ConfigHelper.getP4Config();
    LOG.debug("Using p4config file name: " + p4ConfigFileName);

    final Map<VirtualFile, File> configsMap = p4ConfigFileName == null ? Collections.emptyMap()
                                                                       : fillConfigsMap(detailedVcsMappings, p4ConfigFileName);
    final Map<VirtualFile,P4ConnectionParameters> connectionSettings = new HashMap<>();
    final P4ConnectionParameters defaultParameters = new P4ConnectionParameters();

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
        p4ConfigHelper.fillDefaultValues(value);

        LOG.debug("Using " + value + " for " + mapping);
        connectionSettings.put(mapping, value);
      }
      // todo: default values are already defined in P4ConfigHelper. Use them instead and update inside this call
      fillDefaultValues(p4ConfigHelper, physicalParameters, detailedVcsMappings, defaultParameters);
    });

    // filter what left
    filterSimilarConfigFiles(connectionSettings, detailedVcsMappings);

    //copy what left
    connectionSettings.keySet().retainAll(detailedVcsMappings);
    configsMap.keySet().retainAll(detailedVcsMappings);

    myMultipleConnections = new PerforceMultipleConnections(p4ConfigFileName, defaultParameters, connectionSettings, configsMap);
  }

  @NotNull
  public PerforceMultipleConnections getMultipleConnections() {
    return myMultipleConnections;
  }

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

  private void fillDefaultValues(P4ConfigHelper p4ConfigHelper, PerforcePhysicalConnectionParametersI physicalParameters,
                                 List<VirtualFile> detailedVcsMappings, P4ConnectionParameters defaultParameters) {
    if (p4ConfigHelper.hasP4ConfigSetting()) {
      for (VirtualFile vcsMapping : detailedVcsMappings) {
        final P4ConnectionParameters parameters = myParamsCalculator.runSetOnFile(physicalParameters, defaultParameters, vcsMapping.getPath());
        takeProblemsInfoDefaultParams(parameters, defaultParameters);
        if (defaultParameters.allFieldsDefined()) break;
      }
    } else {
      //can run once
      VirtualFile file = myProject.getBaseDir();
      final String path;
      if (file == null) {
        final File[] roots = File.listRoots();
        if (roots == null || roots.length == 0) {
          LOG.info("File.listRoots() returned empty array");
          return;
        }
        path = roots[0].getPath();
      }
      else {
        path = file.getPath();
      }
      final P4ConnectionParameters params = myParamsCalculator.runSetOnFile(physicalParameters, defaultParameters, path);
      takeProblemsInfoDefaultParams(params, defaultParameters);
    }
  }

  private static void takeProblemsInfoDefaultParams(P4ConnectionParameters params, P4ConnectionParameters defaultParameters) {
    if (params.hasProblems()) {
      if (params.getException() != null) {
        defaultParameters.setException(params.getException());
      }
      if (! params.getWarnings().isEmpty()) {
        for (String warning : params.getWarnings()) {
          defaultParameters.addWarning(warning);
        }
      }
    }
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
