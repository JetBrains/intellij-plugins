package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import com.intellij.ui.navigation.Place;
import com.intellij.util.EventDispatcher;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class AirPackagingConfigurableBase<T extends ModifiableAirPackagingOptions> extends NamedConfigurable<T>
  implements Place.Navigator {

  public enum Location {
    CustomDescriptor("custom-descriptor-path"),
    PackageFileName("package-file-name"),
    FilesToPackage("files-to-package"),
    ProvisioningProfile("provisioning-profile"),
    Keystore("keystore"),
    IosSdkPath("ios-sdk-path");

    public final String errorId;

    Location(final String errorId) {
      this.errorId = errorId;
    }
  }

  public interface AirDescriptorInfoProvider {
    String getMainClass();

    String getAirVersion();

    String[] getExtensionIDs();

    boolean isAndroidPackagingEnabled();

    boolean isIOSPackagingEnabled();

    void setCustomDescriptorForAndroidAndIOS(String descriptorPath);
  }

  private JPanel myMainPanel;

  private JCheckBox myEnabledCheckBox;
  private AirDescriptorForm myAirDescriptorForm;
  private JTextField myPackageFileNameTextField;
  private FilesToPackageForm myFilesToPackageForm;
  private SigningOptionsForm mySigningOptionsForm;

  private final Module myModule;
  private final T myModel;

  private final boolean isAndroid;
  private final boolean isIOS;

  private final AirDescriptorInfoProvider myAirDescriptorInfoProvider;

  private final Disposable myDisposable;
  private final EventDispatcher<UserActivityListener> myUserActivityDispatcher;
  private boolean myFreeze;

  public AirPackagingConfigurableBase(final Module module, final T model, final AirDescriptorInfoProvider airDescriptorInfoProvider) {
    myModule = module;
    myModel = model;
    myAirDescriptorInfoProvider = airDescriptorInfoProvider;

    isAndroid = model instanceof ModifiableAndroidPackagingOptions;
    isIOS = model instanceof ModifiableIosPackagingOptions;

    myEnabledCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateControls();
      }
    });

    myDisposable = Disposer.newDisposable();
    UserActivityWatcher watcher = new UserActivityWatcher();
    watcher.register(myMainPanel);
    myUserActivityDispatcher = EventDispatcher.create(UserActivityListener.class);
    watcher.addUserActivityListener(new UserActivityListener() {
      @Override
      public void stateChanged() {
        if (myFreeze) {
          return;
        }
        myUserActivityDispatcher.getMulticaster().stateChanged();
      }
    }, myDisposable);
  }

  public void addUserActivityListener(final UserActivityListener listener, final Disposable disposable) {
    myUserActivityDispatcher.addListener(listener, disposable);
  }

  public void removeUserActivityListeners() {
    for (UserActivityListener listener : myUserActivityDispatcher.getListeners()) {
      myUserActivityDispatcher.removeListener(listener);
    }
  }

  private void updateControls() {
    final boolean enabled = isPackagingEnabled();
    UIUtil.setEnabled(myMainPanel, enabled, true);
    myEnabledCheckBox.setEnabled(true);
    myAirDescriptorForm.updateControls();
    mySigningOptionsForm.setEnabled(enabled);
  }

  @Override
  public T getEditableObject() {
    return myModel;
  }

  @Override
  public void setDisplayName(final String name) {
  }

  @Override
  public String getBannerSlogan() {
    return getDisplayName();
  }

  @Override
  public JComponent createOptionsPanel() {
    return myMainPanel;
  }

  @Override
  public void reset() {
    myFreeze = true;
    try {
      myEnabledCheckBox.setVisible(isAndroid || isIOS);

      if (isAndroid) myEnabledCheckBox.setSelected(((AndroidPackagingOptions)myModel).isEnabled());
      if (isIOS) myEnabledCheckBox.setSelected(((IosPackagingOptions)myModel).isEnabled());

      myAirDescriptorForm.resetFrom(myModel);
      myPackageFileNameTextField.setText(myModel.getPackageFileName());
      myFilesToPackageForm.resetFrom(myModel.getFilesToPackage());
      mySigningOptionsForm.resetFrom(myModel.getSigningOptions());

      updateControls();
    }
    finally {
      myFreeze = false;
    }
  }

  @Override
  public boolean isModified() {
    if (isAndroid && myEnabledCheckBox.isSelected() != ((AndroidPackagingOptions)myModel).isEnabled()) return true;
    if (isIOS && myEnabledCheckBox.isSelected() != ((IosPackagingOptions)myModel).isEnabled()) return true;

    if (myAirDescriptorForm.isModified(myModel)) return true;
    if (!myModel.getPackageFileName().equals(myPackageFileNameTextField.getText().trim())) return true;
    if (myFilesToPackageForm.isModified(myModel.getFilesToPackage())) return true;
    if (mySigningOptionsForm.isModified(myModel.getSigningOptions())) return true;

    return false;
  }

  @Override
  public void apply() {
    applyTo(myModel);
  }

  public void applyTo(final ModifiableAirPackagingOptions model) {
    if (isAndroid) ((ModifiableAndroidPackagingOptions)model).setEnabled(myEnabledCheckBox.isSelected());
    if (isIOS) ((ModifiableIosPackagingOptions)model).setEnabled(myEnabledCheckBox.isSelected());

    myAirDescriptorForm.applyTo(model);
    model.setPackageFileName(myPackageFileNameTextField.getText().trim());
    model.setFilesToPackage(myFilesToPackageForm.getFilesToPackage());
    mySigningOptionsForm.applyTo(model.getSigningOptions());
  }

  @Override
  public void disposeUIResources() {
    Disposer.dispose(myDisposable);
  }

  private void createUIComponents() {
    final Runnable descriptorCreator = () -> {
      final String folderPath = FlexUtils.getContentOrModuleFolderPath(myModule);
      final String mainClass = myAirDescriptorInfoProvider.getMainClass();
      final String airVersion = myAirDescriptorInfoProvider.getAirVersion();
      final String[] extensions = myAirDescriptorInfoProvider.getExtensionIDs();
      final boolean androidEnabled = myAirDescriptorInfoProvider.isAndroidPackagingEnabled();
      final boolean iosEnabled = myAirDescriptorInfoProvider.isIOSPackagingEnabled();

      final CreateAirDescriptorTemplateDialog dialog =
        new CreateAirDescriptorTemplateDialog(myModule.getProject(), folderPath, mainClass, airVersion, extensions,
                                              androidEnabled, iosEnabled);

      if (dialog.showAndGet()) {
        final String descriptorPath = dialog.getDescriptorPath();
        setUseCustomDescriptor(descriptorPath);

        if (androidEnabled && iosEnabled && dialog.isBothAndroidAndIosSelected()) {
          final int choice =
            Messages.showYesNoDialog(myModule.getProject(), FlexBundle.message("use.same.descriptor.for.android.and.ios"),
                                     CreateAirDescriptorTemplateDialog.getTitleText(), Messages.getQuestionIcon());
          if (choice == Messages.YES) {
            myAirDescriptorInfoProvider.setCustomDescriptorForAndroidAndIOS(descriptorPath);
          }
        }
      }
    };

    myAirDescriptorForm = new AirDescriptorForm(myModule.getProject(), descriptorCreator);
    myFilesToPackageForm = new FilesToPackageForm(myModule.getProject());

    final SigningOptionsForm.Mode mode = isIOS ? SigningOptionsForm.Mode.iOS
                                               : isAndroid ? SigningOptionsForm.Mode.Android
                                                           : SigningOptionsForm.Mode.Desktop;
    mySigningOptionsForm = new SigningOptionsForm(myModule.getProject(), mode);
  }

  public void setUseCustomDescriptor(final String descriptorPath) {
    myAirDescriptorForm.setUseCustomDescriptor(descriptorPath);
  }

  public boolean isPackagingEnabled() {
    return !myEnabledCheckBox.isVisible() || myEnabledCheckBox.isSelected();
  }

  @Override
  public ActionCallback navigateTo(@Nullable final Place place, final boolean requestFocus) {
    if (place != null && place.getPath(FlexBCConfigurable.LOCATION_ON_TAB) instanceof Location loc) {
      return switch (loc) {
        case CustomDescriptor -> myAirDescriptorForm.navigateTo(loc);
        case PackageFileName -> IdeFocusManager.findInstance().requestFocus(myPackageFileNameTextField, true);
        case FilesToPackage -> myFilesToPackageForm.navigateTo(loc);
        case ProvisioningProfile, Keystore, IosSdkPath -> mySigningOptionsForm.navigateTo(loc);
      };
    }
    return ActionCallback.DONE;
  }
}
