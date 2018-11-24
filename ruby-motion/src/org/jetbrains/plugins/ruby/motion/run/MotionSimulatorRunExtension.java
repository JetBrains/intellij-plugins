package org.jetbrains.plugins.ruby.motion.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Location;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.WriteExternalException;
import com.jetbrains.cidr.execution.ProcessOutputReaders;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.debugger.RubyDebugRunner;
import org.jetbrains.plugins.ruby.ruby.run.RubyProcessHandler;
import org.jetbrains.plugins.ruby.ruby.run.configuration.AbstractRubyRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.configuration.RubyRunConfigurationExtension;
import org.jetbrains.plugins.ruby.tasks.rake.runConfigurations.RakeConsoleModifier;
import org.jetbrains.plugins.ruby.tasks.rake.runConfigurations.RakeRunConfiguration;

import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public class MotionSimulatorRunExtension extends RubyRunConfigurationExtension {
  private static final Logger LOG = Logger.getInstance(MotionSimulatorRunExtension.class);
  public static final Key<MotionProcessOutputReaders> FILE_OUTPUT_READERS = Key.create("RubyMotion.FileOutputReaders");

  @NotNull
  @Override
  protected String getSerializationId() {
    return MotionSimulatorRunExtension.class.getName();
  }

  @Override
  protected void readExternal(@NotNull AbstractRubyRunConfiguration runConfiguration, @NotNull Element element)
    throws InvalidDataException {}

  @Override
  protected void writeExternal(@NotNull AbstractRubyRunConfiguration runConfiguration, @NotNull Element element)
    throws WriteExternalException {}

  @Nullable
  @Override
  protected <P extends AbstractRubyRunConfiguration> SettingsEditor<P> createEditor(@NotNull P configuration) {
    return null;
  }

  @Nullable
  @Override
  protected String getEditorTitle() {
    return null;
  }

  @Override
  protected boolean isApplicableFor(@NotNull AbstractRubyRunConfiguration configuration) {
    return true;
  }

  @Override
  protected boolean isEnabledFor(@NotNull AbstractRubyRunConfiguration configuration,
                                 @Nullable RunnerSettings runnerSettings) {
    if (configuration instanceof RakeRunConfiguration) {
      for (RakeConsoleModifier modifier : RakeConsoleModifier.EP_NAME.getExtensions()) {
        if (modifier.isApplicable((RakeRunConfiguration)configuration)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected void patchCommandLine(@NotNull AbstractRubyRunConfiguration configuration,
                                  RunnerSettings runnerSettings,
                                  @NotNull GeneralCommandLine cmdLine,
                                  @NotNull String runnerId) throws ExecutionException {
    final Map<String, String> env = cmdLine.getEnvironment();
    final String originalRunnerId = cmdLine.getUserData(RakeConsoleModifier.ORIGINAL_RUNNER_ID);
    if (RubyDebugRunner.ID.equals(originalRunnerId)) {
      env.put("SIM_WAIT_FOR_DEBUGGER", "1");
    }
    if (configuration instanceof RakeRunConfiguration && ((RakeRunConfiguration)configuration).getTaskName().contains("device")) {
      final boolean enableDebugger = RubyDebugRunner.ID.equals(originalRunnerId);
      if (enableDebugger) {
        env.put("debug", "1");
        env.put("no_start", "1");
      }
    }
    final MotionProcessOutputReaders readers = new MotionProcessOutputReaders(cmdLine);
    env.put("SIM_STDOUT_PATH", readers.getOutFileAbsolutePath());
    env.put("SIM_STDERR_PATH", readers.getErrFileAbsolutePath());
    env.put("output", "rubymine");
    cmdLine.putUserData(FILE_OUTPUT_READERS, readers);
  }

  @Override
  protected void validateConfiguration(@NotNull AbstractRubyRunConfiguration configuration, boolean isExecution)
    throws Exception {}

  @Override
  protected void extendCreatedConfiguration(@NotNull AbstractRubyRunConfiguration configuration, @NotNull Location location) {}

  @Override
  protected void extendTemplateConfiguration(@NotNull AbstractRubyRunConfiguration configuration) {}

  @Override
  protected void attachToProcess(@NotNull AbstractRubyRunConfiguration configuration,
                                 @NotNull ProcessHandler handler,
                                 RunnerSettings runnerSettings) {

  }

  static class MotionProcessOutputReaders extends ProcessOutputReaders {
    private RubyProcessHandler myHandler;

    public MotionProcessOutputReaders(@NotNull GeneralCommandLine commandLine) throws ExecutionException {
      init(commandLine, true);
    }

    @Override
    protected void onTextAvailable(@NotNull String text, @NotNull Key type) {
      if (myHandler != null) {
        myHandler.notifyTextAvailable(text, type);
      }
    }

    public void setHandler(RubyProcessHandler handler) {
      myHandler = handler;
    }
  }
}
