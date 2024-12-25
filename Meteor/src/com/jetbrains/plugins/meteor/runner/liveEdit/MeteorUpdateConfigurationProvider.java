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


public final class MeteorUpdateConfigurationProvider extends UpdateConfigurationProvider {
  private static final String S = "(function(Npm, Assets){(function(){";
  private static final String E = """

    })();

    })""";

  private static final UpdateConfiguration UPDATE_CONFIGURATION = new UpdateConfiguration() {
    @Override
    public @NotNull UpdatePolicy getPolicy() {
      return LiveEditOptions.getInstance().isNodeUpdateOnChanges() ? UpdatePolicy.AUTO : UpdatePolicy.MANUAL;
    }

    @Override
    public int getAutoDelay() {
      return LiveEditOptions.getInstance().getNodeAutoDelay();
    }

    @Override
    public @NotNull CharSequence preprocessSource(@NotNull CharSequence text) {
      return new MergingCharSequence(new MergingCharSequence(S, text), E);
    }

    @Override
    public boolean includeHtmlInAutoUpdate() {
      return false;
    }
  };

  @Override
  public @Nullable UpdateConfiguration getConfiguration(@NotNull XDebugProcess debugProcess) {
    boolean isMeteorProcess = debugProcess.getSession().getRunProfile() instanceof MeteorRunConfiguration;

    return isMeteorProcess ? UPDATE_CONFIGURATION : null;
  }

  @Override
  public boolean isSupported(@NotNull RunProfile runProfile) {
    return runProfile instanceof MeteorRunConfiguration;
  }
}
