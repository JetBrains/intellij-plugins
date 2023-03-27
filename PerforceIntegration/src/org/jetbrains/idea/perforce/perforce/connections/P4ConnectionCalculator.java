package org.jetbrains.idea.perforce.perforce.connections;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.formove.FilePathComparator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.AbstractFilterChildren;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.JBIterable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.*;

import java.io.File;
import java.io.IOException;
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
    final PerforceSettings settings = PerforceSettings.getSettings(myProject);
    final PerforcePhysicalConnectionParameters physicalParameters = settings.getPhysicalSettings();

    final List<VirtualFile> detailedVcsMappings = Registry.is("p4.new.project.mappings.handling")
                                                  ? new ArrayList<>(Arrays.asList(vcsManager.getRootsUnderVcs(vcs)))
                                                  : vcsManager.getDetailedVcsMappings(vcs);

    final String p4ConfigFileName = P4ConfigHelper.getConfigHelper(myProject).getP4Config();
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
          value = getParametersFromConfig(configParentDir, p4ConfigFileName);
        }
        value.setConfigFileName(p4ConfigFileName);
        if (!value.allFieldsDefined()) {
          // todo: reduce number of calls in 232
          P4ConnectionParameters params = runSetOnFile(physicalParameters, defaultParameters, mapping.getPath());
          value.trySetParams(params);
        }

        LOG.debug("Using " + value + " for " + mapping);
        connectionSettings.put(mapping, value);
      }

      fillDefaultValues(physicalParameters, detailedVcsMappings, defaultParameters);
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

  private void fillDefaultValues(PerforcePhysicalConnectionParametersI physicalParameters,
                                 List<VirtualFile> detailedVcsMappings, P4ConnectionParameters defaultParameters) {
    if (P4ConfigHelper.hasP4ConfigSettingInEnvironment()) {
      for (VirtualFile vcsMapping : detailedVcsMappings) {
        final P4ConnectionParameters parameters = runSetOnFile(physicalParameters, defaultParameters, vcsMapping.getPath());
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
      final P4ConnectionParameters params = runSetOnFile(physicalParameters, defaultParameters, path);
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

  public P4ConnectionParameters runSetOnFile(final PerforcePhysicalConnectionParametersI settings, P4ConnectionParameters defaultParameters, final String file) {
    final P4ConnectionParameters parameters = new P4ConnectionParameters();

    final ExecResult retVal = new ExecResult();
    if (! runSetImpl(settings, file, parameters, retVal)) return parameters;

    if (LOG.isDebugEnabled()) {
      LOG.debug("p4 set output: " + retVal);
    }

    parseSetOutput(defaultParameters, parameters, retVal.getStdout());
    return parameters;
  }

  public static void parseSetOutput(P4ConnectionParameters defaultParameters, P4ConnectionParameters parameters, String output) {
    final Map<String, P4ConfigFields> fields = p4FieldsAsMap();

    for (String line : StringUtil.splitByLines(output)) {
      final int eqIdx = line.indexOf("=");
      if (eqIdx != -1) {
        String key = line.substring(0, eqIdx);
        P4ConfigFields fieldType = fields.get(StringUtil.toLowerCase(key));
        if (fieldType != null) {
          int configIdx = line.indexOf(ourInConfig, eqIdx + 1);
          int envIdx = line.indexOf(ourInEnvironment, eqIdx + 1);
          if (envIdx < 0) {
            envIdx = line.indexOf(ourInEnvironment2, eqIdx + 1);
          }
          int valueEnd = envIdx > 0 ? envIdx : configIdx > 0 ? configIdx : line.length();
          String value = line.substring(eqIdx + 1, valueEnd).trim();
          if (!value.isEmpty()) {
            // all variables end up in parameters
            setField(fieldType, value, parameters);

            // the ones defined in enviro or env vars also get into default parameters to be shared between connections
            if (configIdx < 0) {
              setField(fieldType, value, defaultParameters);
            }
          }
        }
      }
    }
  }

  private static Map<String, P4ConfigFields> p4FieldsAsMap() {
    final P4ConfigFields[] p4ConfigFields = P4ConfigFields.values();
    final Map<String, P4ConfigFields> fields = new HashMap<>();
    for (P4ConfigFields configField : p4ConfigFields) {
      fields.put(StringUtil.toLowerCase(configField.getName()), configField);
    }
    return fields;
  }

  public static P4ConnectionParameters getParametersFromConfig(final File configParentDir, @NotNull final String name) {
    final P4ConnectionParameters parameters = new P4ConnectionParameters();
    JBIterable<File> allConfigs = JBIterable.generate(configParentDir, File::getParentFile).map(dir -> new File(dir, name)).filter(File::exists);
    try {
      for (File ioFile : ContainerUtil.reverse(allConfigs.toList())) {
        final String data = String.valueOf(FileUtil.loadFileText(ioFile));
        final Map<String, P4ConfigFields> fields = p4FieldsAsMap();
        for (String line : StringUtil.splitByLines(data)) {
          List<String> split = Lists.newArrayList(Splitter.on('=').limit(2).trimResults().split(line));
          if (split.size() == 2) {
            String key = split.get(0);
            P4ConfigFields p4ConfigField = fields.get(StringUtil.toLowerCase(key));
            setField(p4ConfigField, split.get(1), parameters);
          }
        }
      }
    }
    catch (IOException e) {
      parameters.setException(e);
    }
    return parameters;
  }

  private boolean runSetImpl(PerforcePhysicalConnectionParametersI settings,
                             String file,
                             P4ConnectionParameters parameters,
                             ExecResult retVal) {
    final P4Connection localConnection = new PerforceLocalConnection(file);
    try {
      localConnection.runP4Command(settings, new String[]{"set"}, retVal, null);
    }
    catch (VcsException | InterruptedException | IOException | PerforceTimeoutException e) {
      parameters.setException(e);
      return false;
    }

    try {
      // todo hack =((
      // do not expect "password invalid" when P4 SET is executed, so just use empty settings
      PerforceRunner.checkError(retVal, new PerforceSettings(myProject), localConnection);
    }
    catch (VcsException e) {
      parameters.setException(e);
      return false;
    }
    return true;
  }

  private static void setField(final P4ConfigFields fieldType,
                               final String value,
                               final P4ConnectionParameters parameters) {
    if (P4ConfigFields.P4CLIENT.equals(fieldType)) {
      parameters.setClient(value);
    } else if (P4ConfigFields.P4PASSWD.equals(fieldType)) {
      parameters.setPassword(value);
    } else if (P4ConfigFields.P4PORT.equals(fieldType)) {
      parameters.setServer(value);
    } else if (P4ConfigFields.P4USER.equals(fieldType)) {
      parameters.setUser(value);
    } else if (P4ConfigFields.P4CHARSET.equals(fieldType)) {
      parameters.setCharset(value);
    } else if (P4ConfigFields.P4CONFIG.equals(fieldType)) {
      parameters.setConfigFileName(value);
    } else if (P4ConfigFields.P4IGNORE.equals(fieldType)) {
      parameters.setIgnoreFileName(value);
    }
  }

  private final static String ourInConfig = "(config";
  private final static String ourInEnvironment = "(set";
  private final static String ourInEnvironment2 = "(env";
}
