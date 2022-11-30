package jetbrains.plugins.yeoman.projectGenerator.ui.list;


import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.util.containers.ContainerUtil;
import jetbrains.plugins.yeoman.YeomanBundle;
import jetbrains.plugins.yeoman.generators.YeomanGeneratorFullInfo;
import jetbrains.plugins.yeoman.generators.YeomanGeneratorInfo;
import jetbrains.plugins.yeoman.generators.YeomanGeneratorListProvider;
import jetbrains.plugins.yeoman.generators.YeomanInstalledGeneratorInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class YeomanAvailableGeneratorsMain extends YeomanGeneratorsMain {
  private final YeomanGeneratorListProvider myProvider;
  private Map<String, YeomanInstalledGeneratorInfo> myInstalledGenerators;
  private boolean myBusy = false;

  public YeomanAvailableGeneratorsMain(@NotNull List<YeomanInstalledGeneratorInfo> installedGenerators,
                                       YeomanGeneratorListProvider provider) {
    super();
    myProvider = provider;
    setInstalledGenerators(installedGenerators);
    myFilter = new MyPluginsFilter();
    init(true);
    main.setPreferredSize(new Dimension(550, 400));
  }

  public void setInstalledGenerators(@NotNull List<YeomanInstalledGeneratorInfo> installedGenerators) {
    myInstalledGenerators = ContainerUtil.map2MapNotNull(installedGenerators,
                                                         info -> Pair.create(info.getName(), info));
  }

  @Override
  protected ActionGroup getActionGroup(boolean b) {
    DefaultActionGroup actionGroup = new DefaultActionGroup();
    actionGroup.add(new DumbAwareAction(
      YeomanBundle.messagePointer("action.DumbAware.YeomanAvailableGeneratorsMain.text.reload.list.of.generators"),
      YeomanBundle.messagePointer("action.DumbAware.YeomanAvailableGeneratorsMain.description.reload.list.of.generators"),
      AllIcons.Actions.Refresh) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        myBusy = true;

        downloadAndShowGeneratorList();
      }

      @Override
      public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
      }

      @Override
      public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(!myBusy);
      }
    });

    return actionGroup;
  }

  public void downloadAndShowGeneratorList() {
    final YeomanGeneratorInfo generator = getSelectedObject();

    final Ref<List<YeomanGeneratorFullInfo>> generators = new Ref<>(null);

    final Ref<Boolean> hasErrors = Ref.create(false);

    ProgressManager.getInstance().runProcessWithProgressSynchronously((() -> {
      try {
        try {
          List<YeomanGeneratorFullInfo> providedGenerators = myProvider.getAvailableGenerators(true);
          generators.set(providedGenerators);
        }
        catch (RuntimeException e) {
          hasErrors.set(true);
        }
      }
      finally {
        myBusy = false;
      }
    }), YeomanBundle.message("yeoman.generators.dialog.available.list.download.title"), true, null, main);

    if (myFilter != null) {
      myFilter.setFilter("");
    }
    //still edt
    final List<YeomanGeneratorFullInfo> list = ContainerUtil.notNullize(generators.get());
    YeomanGeneratorFullInfo newSelected = null;
    for (YeomanGeneratorFullInfo info : list) {
      if (info.equals(generator)) {
        newSelected = info;
      }
    }
    setAvailableGenerators(list);
    select(newSelected);
    handleUpdate();

    if (hasErrors.get()) {
      Messages.showErrorDialog(getMainPanel(), YeomanBundle.message("yeoman.generators.dialog.download.error"));
    }
  }

  public void setAvailableGenerators(List<YeomanGeneratorFullInfo> availableGenerators) {
    myModel.setAllViews(availableGenerators);
  }

  @Override
  @Nullable
  public YeomanInstalledGeneratorInfo getInstalledGeneratorInfo(@Nullable YeomanGeneratorInfo info) {
    if (info == null) return null;

    final YeomanInstalledGeneratorInfo installedGeneratorInfo = myInstalledGenerators.get(info.getName());
    return installedGeneratorInfo != null ?
           installedGeneratorInfo :
           super.getInstalledGeneratorInfo(info);
  }
}
