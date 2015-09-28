package org.intellij.plugins.markdown.settings;

import com.intellij.ide.ui.LafManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.util.messages.Topic;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
  name = "MarkdownApplicationSettings",
  storages = @Storage(file = StoragePathMacros.APP_CONFIG + "/markdown.xml")
)
public class MarkdownApplicationSettings implements PersistentStateComponent<MarkdownApplicationSettings.State>,
                                                    MarkdownCssSettings.Holder {

  private State myState = new State();

  public MarkdownApplicationSettings() {
    LafManager.getInstance().addLafManagerListener(new MarkdownLAFListener());
  }

  @NotNull
  public static MarkdownApplicationSettings getInstance() {
    return ServiceManager.getService(MarkdownApplicationSettings.class);
  }

  @Nullable
  @Override
  public State getState() {
    return myState;
  }

  @Override
  public void loadState(State state) {
    XmlSerializerUtil.copyBean(state, myState);
  }

  @Override
  public void setMarkdownCssSettings(@NotNull MarkdownCssSettings settings) {
    myState.myCssSettings = settings;

    ApplicationManager.getApplication().getMessageBus().syncPublisher(SettingsChangedListener.TOPIC).onSettingsChange(this);
  }

  @NotNull
  @Override
  public MarkdownCssSettings getMarkdownCssSettings() {
    return myState.myCssSettings;
  }

  public static class State {
    @Property(surroundWithTag = false)
    @NotNull
    private MarkdownCssSettings myCssSettings = MarkdownCssSettings.DEFAULT;
  }

  public interface SettingsChangedListener {
    Topic<SettingsChangedListener> TOPIC = Topic.create("MarkdownApplicationSettingsChanged", SettingsChangedListener.class);

    void onSettingsChange(@NotNull MarkdownApplicationSettings settings);
  }
}
