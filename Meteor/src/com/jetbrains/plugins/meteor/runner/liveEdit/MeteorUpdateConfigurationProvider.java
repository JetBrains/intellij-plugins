package com.jetbrains.plugins.meteor.runner.liveEdit;

import com.intellij.execution.configurations.RunProfile;
import com.intellij.util.text.MergingCharSequence;
import com.intellij.xdebugger.XDebugProcess;
import com.jetbrains.liveEdit.LiveEditOptions;
import com.jetbrains.liveEdit.UpdatePolicy;
import com.jetbrains.liveEdit.update.UpdateConfiguration;
import com.jetbrains.liveEdit.update.UpdateConfigurationProvider;
import com.jetbrains.plugins.meteor.runner.MeteorRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class MeteorUpdateConfigurationProvider extends UpdateConfigurationProvider {
  private static final String S = "(function(Npm, Assets){(function(){";
  private static final String E = "\n})();\n" +
                                  "\n" +
                                  "})";

  private static final UpdateConfiguration UPDATE_CONFIGURATION = new UpdateConfiguration() {
    @NotNull
    @Override
    public UpdatePolicy getPolicy() {
      return LiveEditOptions.getInstance().isNodeUpdateOnChanges() ? UpdatePolicy.AUTO : UpdatePolicy.MANUAL;
    }

    @Override
    public int getAutoDelay() {
      return LiveEditOptions.getInstance().getNodeAutoDelay();
    }

    @NotNull
    @Override
    public CharSequence preprocessSource(@NotNull CharSequence text) {
      return new MergingCharSequence(new MergingCharSequence(S, text), E);
    }

    @Override
    public boolean includeHtmlInAutoUpdate() {
      return false;
    }
  };

  @Nullable
  @Override
  public UpdateConfiguration getConfiguration(@NotNull XDebugProcess debugProcess) {
    boolean isMeteorProcess = debugProcess.getSession().getRunProfile() instanceof MeteorRunConfiguration;

    return isMeteorProcess ? UPDATE_CONFIGURATION : null;
  }

  @Override
  public boolean isSupported(@NotNull RunProfile runProfile) {
    return runProfile instanceof MeteorRunConfiguration;
  }
}
