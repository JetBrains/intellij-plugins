package org.intellij.plugins.markdown.ui.preview.javafx;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import org.intellij.plugins.markdown.lang.MarkdownFileType;
import org.intellij.plugins.markdown.settings.MarkdownApplicationSettings;
import org.intellij.plugins.markdown.settings.MarkdownPreviewSettings;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanelProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaFxCouldBeEnabledNotificationProvider extends EditorNotifications.Provider<EditorNotificationPanel> implements DumbAware {
  private static final Key<EditorNotificationPanel> KEY = Key.create("Markdown JavaFX Preview Could Be Enabled");

  private static final String DONT_ASK_TO_CHANGE_PROVIDER_TYPE_KEY = "markdown.do.not.ask.to.change.preview.provider";

  @NotNull
  @Override
  public Key<EditorNotificationPanel> getKey() {
    return KEY;
  }

  @Nullable
  @Override
  public EditorNotificationPanel createNotificationPanel(@NotNull VirtualFile file, @NotNull final FileEditor fileEditor) {
    if (file.getFileType() != MarkdownFileType.INSTANCE) {
      return null;
    }
    if (PropertiesComponent.getInstance().getBoolean(DONT_ASK_TO_CHANGE_PROVIDER_TYPE_KEY)) {
      return null;
    }
    final MarkdownApplicationSettings markdownApplicationSettings = MarkdownApplicationSettings.getInstance();
    final MarkdownPreviewSettings oldPreviewSettings = markdownApplicationSettings.getMarkdownPreviewSettings();
    if (oldPreviewSettings.getHtmlPanelProviderInfo().getClassName().equals(JavaFxHtmlPanelProvider.class.getName())) {
      return null;
    }
    final MarkdownHtmlPanelProvider.AvailabilityInfo availabilityInfo = new JavaFxHtmlPanelProvider().isAvailable();
    if (availabilityInfo == MarkdownHtmlPanelProvider.AvailabilityInfo.UNAVAILABLE) {
      return null;
    }

    final EditorNotificationPanel panel = new EditorNotificationPanel();
    panel.setText("JavaFX WebKit-based preview renderer is available.");
    panel.createActionLabel("Change preview browser to JavaFX", () -> {
      final boolean isSuccess = availabilityInfo.checkAvailability(panel);
      if (isSuccess) {
        markdownApplicationSettings.setMarkdownPreviewSettings(new MarkdownPreviewSettings(
          oldPreviewSettings.getSplitEditorLayout(),
          new JavaFxHtmlPanelProvider().getProviderInfo(),
          oldPreviewSettings.isUseGrayscaleRendering()
        ));
        EditorNotifications.updateAll();
      }
      else {
        Logger.getInstance(JavaFxCouldBeEnabledNotificationProvider.class).warn("Could not install and apply OpenJFX");
      }
    });
    panel.createActionLabel("Do not show again", () -> {
      PropertiesComponent.getInstance().setValue(DONT_ASK_TO_CHANGE_PROVIDER_TYPE_KEY, true);
      EditorNotifications.updateAll();
    });
    return panel;
  }
}
