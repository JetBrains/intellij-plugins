package com.github.masahirosuzuka.PhoneGapIntelliJPlugin.runner.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public abstract class PanelWithAsyncLoad<T> {
  private void createUIComponents() {
    myComboBox = createValuesComboBox();
  }

  protected JComboBox createValuesComboBox() {
    return new ComboBox();
  }

  public JPanel getMainPanel() {
    return myMainPanel;
  }

  public interface DataProvider<T> {

    Set<T> getCachedValues();

    /**
     * After getting values method must call #onUpdateValues or #onTagsUpdateError
     */
    void updateValuesAsynchronously();
  }

  private static final String CONTROL_PLACE = "UI.Configuration.Component.Async.Loader";

  private enum UpdateStatus {
    UPDATING, IDLE
  }

  @Nullable
  private volatile DataProvider<T> myDataProvider;

  private UpdateStatus myUpdateStatus;
  private final JLabel myErrorMessage = new JLabel();
  private final AsyncProcessIcon myLoadingVersionIcon = new AsyncProcessIcon("Getting possible values");
  protected JComboBox myComboBox;
  protected JPanel myActionPanel;
  protected JPanel myMainPanel;


  public PanelWithAsyncLoad() {
    myErrorMessage.setForeground(JBColor.RED);
    fillActionPanel();
  }

  public void setDataProvider(@NotNull DataProvider<T> dataProvider) {
    myDataProvider = dataProvider;

    Set<T> cachedValues = dataProvider.getCachedValues();
    if (cachedValues != null) {
      onUpdateValues(cachedValues);
    }
  }

  protected abstract void doUpdateValues(Set<T> values);

  protected abstract boolean shouldUpdate(Set<T> values);

  protected abstract T getSelectedValue();

  public JLabel getErrorComponent() {
    return myErrorMessage;
  }

  public boolean isBackgroundJobRunning() {
    return myUpdateStatus == UpdateStatus.UPDATING;
  }

  public void onUpdateValues(@NotNull Set<T> values) {
    changeUpdateStatus(UpdateStatus.IDLE);
    if (!shouldUpdate(values)) {
      return;
    }

    doUpdateValues(values);
  }


  protected void reloadValuesInBackground() {
    if (myUpdateStatus == UpdateStatus.UPDATING) return;
    changeUpdateStatus(UpdateStatus.UPDATING);
    myErrorMessage.setText(null);
    DataProvider<T> provider = myDataProvider;
    assert provider != null;
    provider.updateValuesAsynchronously();
  }

  private void changeUpdateStatus(@NotNull UpdateStatus status) {
    CardLayout cardLayout = (CardLayout)myActionPanel.getLayout();
    cardLayout.show(myActionPanel, status.name());
    if (status == UpdateStatus.UPDATING) {
      myLoadingVersionIcon.resume();
    }
    else {
      myLoadingVersionIcon.suspend();
    }
    myUpdateStatus = status;
  }

  private void fillActionPanel() {
    myActionPanel.add(createReloadButtonPanel(), UpdateStatus.IDLE.name());
    myActionPanel.add(createReloadInProgressPanel(), UpdateStatus.UPDATING.name());
    changeUpdateStatus(UpdateStatus.IDLE);
  }

  public void onValuesUpdateError(@NotNull final String errorMessage) {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        if (getSelectedValue() == null) {
          myErrorMessage.setText(errorMessage);
        }
        changeUpdateStatus(UpdateStatus.IDLE);
      }
    });
  }

  @NotNull
  private JPanel createReloadButtonPanel() {
    ReloadAction reloadAction = new ReloadAction();
    ActionButton reloadButton = new ActionButton(
      reloadAction,
      reloadAction.getTemplatePresentation().clone(),
      CONTROL_PLACE,
      ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE
    );
    JPanel panel = new JPanel(new BorderLayout(0, 0));
    panel.add(reloadButton, BorderLayout.WEST);
    return panel;
  }

  @NotNull
  private JPanel createReloadInProgressPanel() {
    JPanel panel = new JPanel();
    panel.add(myLoadingVersionIcon);
    return panel;
  }

  private class ReloadAction extends AnAction {

    private ReloadAction() {
      super("Reload list", null, AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
      reloadValuesInBackground();
    }

    @Override
    public void update(AnActionEvent e) {
      e.getPresentation().setEnabled(true);
    }
  }
}
