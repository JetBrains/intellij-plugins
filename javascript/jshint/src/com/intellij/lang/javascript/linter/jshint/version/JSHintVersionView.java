package com.intellij.lang.javascript.linter.jshint.version;

import com.google.common.collect.ImmutableList;
import com.intellij.lang.javascript.linter.jshint.JSHintBundle;
import com.intellij.lang.javascript.linter.jshint.JSHintUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.dsl.listCellRenderer.BuilderKt;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.SwingHelper;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

/**
 * @author Sergey Simonchik
 */
public class JSHintVersionView implements Disposable {

  private static final Logger LOG = Logger.getInstance(JSHintVersionView.class);

  private final Project myProject;
  private final LabeledComponent<JComboBox<JSHintVersionDescriptor>> myComponent;
  private boolean myUpdatingVersionDescriptors = false;
  // It's possible that JSHint version read from .idea/jsLinters/jshint.xml is omitted in local versions.json
  // and no network is available.
  // In that case the version should be marked as red.
  private String myPreservedVersion;
  private @Nullable Balloon myActiveErrorBalloon;

  public JSHintVersionView(@NotNull Project project) {
    myProject = project;
    JComboBox<JSHintVersionDescriptor> comboBox = createComboBox();
    myComponent = LabeledComponent.create(comboBox, JSHintBundle.message("jshint.version.label"));
    myComponent.setLabelLocation(BorderLayout.WEST);
    comboBox.addActionListener(e -> {
      if (!myUpdatingVersionDescriptors) {
        checkSelectedVersion();
      }
    });
    comboBox.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        if (myPreservedVersion != null) {
          asyncDownloadVersions(() -> fillVersions(myPreservedVersion), null);
        }
        comboBox.removeFocusListener(this);
      }
    });
  }

  private void checkSelectedVersion() {
    ApplicationManager.getApplication().invokeLater(() -> {
      hideActiveErrorBalloon();
      JSHintVersionDescriptor selected = getSelectedVersionDescriptor();
      obtainSource(selected);
    });
  }

  private void obtainSource(@NotNull JSHintVersionDescriptor descriptor) {
    if (JSHintVersionUtil.isSourceLocallyAvailable(descriptor.getVersion())) {
      return;
    }
    CompletableFuture<Unit> future = new CompletableFuture<>();
    ModalityState modalityState = ModalityState.current();
    ProgressManager.getInstance().run(new Task.Backgroundable(myProject,
                                                              JSHintBundle.message("jshint.progress.title", descriptor.getVersion()),
                                                              true) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        indicator.setText(
          JSHintBundle.message("jshint.progress.title.downloading", JSHintVersionUtil.getJSHintSourceFileBaseName(descriptor.getVersion())));
        try {
          JSHintVersionUtil.downloadSourceVersionOnce(indicator, descriptor);
          future.complete(Unit.INSTANCE);
        }
        catch (Exception ex) {
          future.completeExceptionally(ex);
        }
      }
    });
    future.whenComplete((completed, throwable) -> {
      if (throwable != null) {
        LOG.info("Can't obtain source of JSHint " + descriptor.getVersion());
        ApplicationManager.getApplication().invokeLater(() -> {
          reportDownloadError(descriptor, throwable instanceof CancellationException);
        }, modalityState);
      }
    });
  }

  private void reportDownloadError(@NotNull JSHintVersionDescriptor descriptor, boolean cancelled) {
    final JComponent errorMessageBalloonContent;
    Runnable retryAction = () -> {
      hideActiveErrorBalloon();
      checkSelectedVersion();
    };
    String title = "<b>JSHint " + descriptor.getVersion() + " is not available</b>";
    if (!cancelled) {
      errorMessageBalloonContent = JSHintUtil.createIOExceptionBalloonComponent(
        title + "<br/>" +
        "Can not download " + descriptor.getUrl(),
        retryAction,
        true
      );
    }
    else {
      errorMessageBalloonContent = JSHintUtil.createIOExceptionBalloonComponent(
        title + "<br/>" +
        descriptor.getUrl() + " download was cancelled",
        retryAction,
        false
      );
    }

    Balloon balloon = JBPopupFactory.getInstance().createBalloonBuilder(errorMessageBalloonContent)
      .setDialogMode(true)
      .setCloseButtonEnabled(true)
      .setDisposable(this)
      .setFillColor(MessageType.ERROR.getPopupBackground())
      .setContentInsets(JBUI.insets(10))
      .createBalloon();
    hideActiveErrorBalloon();
    balloon.show(findRelativePoint(), Balloon.Position.below);
    myActiveErrorBalloon = balloon;
  }

  private @NotNull RelativePoint findRelativePoint() {
    JComboBox<JSHintVersionDescriptor> comboBox = myComponent.getComponent();
    return new RelativePoint(comboBox, new Point(comboBox.getWidth() / 2, (int)(comboBox.getHeight() / 1.2)));
  }

  private void hideActiveErrorBalloon() {
    if (myActiveErrorBalloon != null) {
      myActiveErrorBalloon.hide();
      myActiveErrorBalloon = null;
    }
  }

  public @NotNull JSHintVersionDescriptor getSelectedVersionDescriptor() {
    JComboBox<JSHintVersionDescriptor> comboBox = myComponent.getComponent();
    return (JSHintVersionDescriptor)Objects.requireNonNull(comboBox.getSelectedItem());
  }

  private static @NotNull JComboBox<JSHintVersionDescriptor> createComboBox() {
    ComboBox<JSHintVersionDescriptor> comboBox = new ComboBox<>();
    comboBox.setRenderer(BuilderKt.textListCellRenderer(JSHintBundle.message("jshint.label.no.version"), value ->
      value.getVersion() + (value.isBundled() ? " " + JSHintBundle.message("jshint.label.bundled.suffix") : "")));
    return comboBox;
  }

  private void fillVersions(@Nullable String selectedVersion) {
    myUpdatingVersionDescriptors = true;
    try {
      ImmutableList<JSHintVersionDescriptor> descriptors = JSHintVersionUtil.getVersions();
      ImmutableList<JSHintVersionDescriptor> fixedDescriptors = descriptors;
      JSHintVersionDescriptor selectedDescriptor = null;
      if (selectedVersion != null) {
        fixedDescriptors = JSHintVersionUtil.addGuessableVersionDescriptorFirstIfMissing(descriptors, selectedVersion);
        selectedDescriptor = JSHintVersionUtil.find(fixedDescriptors, selectedVersion);
      }
      JComboBox<JSHintVersionDescriptor> comboBox = myComponent.getComponent();
      SwingHelper.updateItems(comboBox, fixedDescriptors, selectedDescriptor);
    }
    finally {
      myUpdatingVersionDescriptors = false;
    }
  }

  public @NotNull Component getComponent() {
    return myComponent;
  }

  public void setVersion(final @NotNull String version) {
    myPreservedVersion = version;
    fillVersions(version);
    ImmutableList<JSHintVersionDescriptor> descriptors = JSHintVersionUtil.getVersions();
    if (JSHintVersionUtil.find(descriptors, version) != null) {
      checkSelectedVersion();
    }
    else {
      asyncDownloadVersions(
        () -> {
          fillVersions(version);
          checkSelectedVersion();
        },
        this::checkSelectedVersion
      );
    }
  }

  private static void asyncDownloadVersions(final @Nullable Runnable onSuccess, final @Nullable Runnable onFailure) {
    final ModalityState modalityState = ModalityState.any();
    final Application application = ApplicationManager.getApplication();
    application.executeOnPooledThread(
      () -> {
        try {
          JSHintVersionUtil.downloadVersions(null);
          if (onSuccess != null) {
            application.invokeLater(onSuccess, modalityState);
          }
        }
        catch (Exception e) {
          LOG.warn("Can't update jshint version list", e);
          if (onFailure != null) {
            application.invokeLater(onFailure, modalityState);
          }
        }
      }
    );
  }

  public @NotNull String getVersion() {
    JSHintVersionDescriptor selected = getSelectedVersionDescriptor();
    return selected.getVersion();
  }

  @Override
  public void dispose() {
    // does nothing
  }

}
