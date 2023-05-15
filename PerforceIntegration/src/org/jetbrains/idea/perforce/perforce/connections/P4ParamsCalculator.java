package org.jetbrains.idea.perforce.perforce.connections;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.JBIterable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.perforce.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class P4ParamsCalculator {
  private final static String ourInConfig = "(config";
  private final static String ourNoConfig = "'noconfig'";
  private final static String ourInEnvironment = "(set";
  private final static String ourInEnvironment2 = "(env";

  private static final Logger LOG = Logger.getInstance(P4ParamsCalculator.class);
  private final Project myProject;

  public P4ParamsCalculator(Project project) {
    myProject = project;
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

            if (configIdx >= 0) {
              // P4CONFIG might have suffix (config 'noconfig') if set
              int noConfigIdx = line.indexOf(ourNoConfig, configIdx);
              if (noConfigIdx < 0)
                continue;
            }

            // the ones defined in enviro or env vars also get into default parameters to be shared between connections
            setField(fieldType, value, defaultParameters);
          }
        }
      }
    }
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

  private static Map<String, P4ConfigFields> p4FieldsAsMap() {
    final P4ConfigFields[] p4ConfigFields = P4ConfigFields.values();
    final Map<String, P4ConfigFields> fields = new HashMap<>();
    for (P4ConfigFields configField : p4ConfigFields) {
      fields.put(StringUtil.toLowerCase(configField.getName()), configField);
    }
    return fields;
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
}
