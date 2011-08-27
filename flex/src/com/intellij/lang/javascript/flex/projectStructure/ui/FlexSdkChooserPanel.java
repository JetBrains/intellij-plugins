package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.FlexIdeUtils;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkModel;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.projectRoots.impl.SdkConfigurationUtil;
import com.intellij.openapi.projectRoots.ui.SdkEditor;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.JdkConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.JdkListConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.navigation.History;
import com.intellij.ui.navigation.Place;
import com.intellij.util.EventDispatcher;
import com.intellij.util.PlatformUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author ksafonov
 */
public class FlexSdkChooserPanel implements Disposable {

  private final Project myProject;

  private JLabel mySdkLabel;
  private SdkPathCombo mySdkPathCombo;
  private JButton myEditButton;
  private JLabel myInfoLabel;
  @SuppressWarnings("UnusedDeclaration") private JPanel myContentPane;

  private final SdkModel.Listener myListener;
  private final ProjectSdksModel mySdksModel;
  private String myInitialSdkHome;
  private final EventDispatcher<ChangeListener> myEventDispatcher;
  private @Nullable String mySdkVersion;
  private final ModifiableRootModel myRootModel;
  private boolean mySdkRootsModified;
  @Nullable
  private ProjectJdkImpl myWorkingSdk;

  private static final Place.Navigator EMPTY_NAVIGATOR = new Place.Navigator() {
    @Override
    public void setHistory(History history) {
    }

    @Override
    public ActionCallback navigateTo(@Nullable Place place, boolean requestFocus) {
      return new ActionCallback.Done();
    }

    @Override
    public void queryPlace(@NotNull Place place) {
    }
  };

  public FlexSdkChooserPanel(Project project, ModifiableRootModel rootModel) {
    myProject = project;
    myRootModel = rootModel;
    myEventDispatcher = EventDispatcher.create(ChangeListener.class);
    if (!PlatformUtils.isFlexIde()) {
      throw new UnsupportedOperationException("Should not be visible in IDEA");
    }
    mySdksModel = ProjectStructureConfigurable.getInstance(myProject).getProjectJdksModel();
    mySdkLabel.setLabelFor(mySdkPathCombo.getChildComponent());

    myListener = new SdkModel.Listener() {
      @Override
      public void sdkAdded(Sdk sdk) {
      }

      @Override
      public void beforeSdkRemove(Sdk sdk) {
        if (!isModified() && findExistingSdk(getSdkPath(), FlexIdeUtils.getSdkType()) == null) {
          mySdkPathCombo.setText("");
          myInitialSdkHome = "";
          myWorkingSdk = null;
        }
      }

      @Override
      public void sdkChanged(Sdk sdk, String previousName) {
      }

      @Override
      public void sdkHomeSelected(Sdk sdk, String newSdkHome) {
      }
    };
    mySdksModel.addListener(myListener);
    mySdkPathCombo.addListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        analyzeSdk();
        myEventDispatcher.getMulticaster().stateChanged(new ChangeEvent(FlexSdkChooserPanel.this));
        myWorkingSdk = null;
      }
    });

    // TODO edit button text
    myEditButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (PlatformUtils.isFlexIde()) {
          editSdk();
        }
        else {
          navigateToEditSdk();
        }
      }
    });
  }

  private void navigateToEditSdk() {
    Sdk sdk = findExistingSdk(getSdkPath(), FlexIdeUtils.getSdkType());
    if (sdk == null) {
      sdk = createSdk(getSdkPath());
      JdkListConfigurable.getInstance(myProject).addJdkNode(sdk, false);
      mySdksModel.doAdd((ProjectJdkImpl)sdk, null);
    }
    ProjectStructureConfigurable.getInstance(myProject).select(sdk, true);
  }

  private void editSdk() {
    if (myWorkingSdk == null) {
      Sdk existingSdk = findExistingSdk(getSdkPath(), FlexIdeUtils.getSdkType());
      if (existingSdk != null) {
        try {
          myWorkingSdk = (ProjectJdkImpl)existingSdk.clone();
        }
        catch (CloneNotSupportedException ignored) {
        }
      }
      else {
        myWorkingSdk = createSdk(getSdkPath());
      }
    }

    JdkConfigurable c = new JdkConfigurable(myWorkingSdk, mySdksModel, null, new History(EMPTY_NAVIGATOR), myProject) {
      @Override
      protected SdkEditor createSdkEditor(ProjectSdksModel sdksModel, History history, ProjectJdkImpl projectJdk) {
        return new SdkEditor(sdksModel, history, projectJdk) {
          @Override
          protected boolean showTabForType(OrderRootType type) {
            return type == OrderRootType.SOURCES || type == JavadocOrderRootType.getInstance();
          }

          @Override
          protected TextFieldWithBrowseButton createHomeComponent() {
            TextFieldWithBrowseButton c = new TextFieldWithBrowseButton();
            c.getButton().setVisible(false);
            return c;
          }
        };
      }

      @Override
      public String getDisplayName() {
        return FlexIdeUtils.getSdkType().getPresentableName();
      }

      @Override
      public void updateName() {
        // ignore
      }
    };
    c.setNameFieldShown(false);
    boolean modified = ShowSettingsUtil.getInstance().editConfigurable(myProject, c);
    if (modified) {
      mySdkRootsModified = true;
    }
  }

  private ProjectJdkImpl createSdk(String sdkPath) {
    String newSdkName =
      SdkConfigurationUtil.createUniqueSdkName(FlexIdeUtils.getSdkType(), FileUtil.toSystemIndependentName(sdkPath),
                                               mySdksModel.getProjectSdks().values());
    ProjectJdkImpl result = new ProjectJdkImpl(newSdkName, FlexIdeUtils.getSdkType());
    result.setHomePath(sdkPath);
    FlexIdeUtils.getSdkType().setupSdkPaths(result);
    return result;
  }

  private void analyzeSdk() {
    String sdkPath = getSdkPath();
    if (StringUtil.isEmpty(sdkPath)) {
      mySdkVersion = null;
      myEditButton.setEnabled(false);
      myInfoLabel.setText("");
    }
    else {
      if (!FlexIdeUtils.getSdkType().isValidSdkHome(sdkPath)) {
        mySdkVersion = null;
        myEditButton.setEnabled(false);
        myInfoLabel.setText("SDK not found");
      }
      else {
        String sdkVersion = FlexSdkUtils.readFlexSdkVersion(LocalFileSystem.getInstance().findFileByPath(sdkPath));
        mySdkVersion = sdkVersion;
        myEditButton.setEnabled(true);
        myInfoLabel.setText("Flex SDK version: " + sdkVersion);
        // TODO AIR version
      }
    }
  }

  public boolean isModified() {
    return !pathsEqual(myInitialSdkHome, getSdkPath()) || mySdkRootsModified;
  }

  @Nullable
  public String getSdkPath() {
    return mySdkPathCombo.getText();
  }

  @Nullable
  public String getSdkVersion() {
    return mySdkVersion;
  }

  public void addListener(ChangeListener listener) {
    myEventDispatcher.addListener(listener);
  }

  public void removeListener(ChangeListener listener) {
    myEventDispatcher.removeListener(listener);
  }

  private static boolean pathsEqual(@Nullable String path1, @Nullable String path2) {
    path1 = StringUtil.trimEnd(FileUtil.toSystemIndependentName(StringUtil.notNullize(path1)), "/");
    path2 = StringUtil.trimEnd(FileUtil.toSystemIndependentName(StringUtil.notNullize(path2)), "/");
    return Comparing.equal(path1, path2, SystemInfo.isFileSystemCaseSensitive);
  }

  public void reset() {
    final Sdk sdk = myRootModel.getSdk();
    String sdkHome = sdk != null ? FileUtil.toSystemDependentName(StringUtil.notNullize(sdk.getHomePath())) : "";
    mySdkPathCombo.setText(sdkHome);
    myInitialSdkHome = sdkHome;
    analyzeSdk();
    mySdkRootsModified = false;
    myWorkingSdk = null;
  }

  public void apply() throws ConfigurationException {
    if (myWorkingSdk != null) {
      Sdk existingSdk = findExistingSdk(getSdkPath(), FlexIdeUtils.getSdkType());
      if (existingSdk != null) {
        copy(myWorkingSdk, (ProjectJdkImpl)existingSdk);
        mySdksModel.apply(); // happens only in Flex IDE, in IDEA we should have jumped to SDKs list
      }
      else {
        JdkListConfigurable.getInstance(myProject).addJdkNode(myWorkingSdk, false);
        mySdksModel.doAdd(myWorkingSdk, null);
        myRootModel.setSdk(myWorkingSdk);
      }
      myWorkingSdk = null;
    }
    else if (!pathsEqual(myInitialSdkHome, getSdkPath())) {
      String newSdkHome = getSdkPath();
      final SdkType sdkType = FlexIdeUtils.getSdkType();
      if (StringUtil.isEmpty(newSdkHome) || !sdkType.isValidSdkHome(newSdkHome)) {
      }
      else {
        Sdk sdk = findExistingSdk(newSdkHome, sdkType);
        if (sdk == null) {
          sdk = createSdk(newSdkHome);
          JdkListConfigurable.getInstance(myProject).addJdkNode(sdk, false);
          mySdksModel.doAdd((ProjectJdkImpl)sdk, null);
        }
      }
    }
    mySdkPathCombo.saveHistory();
  }

  private static void copy(ProjectJdkImpl source, ProjectJdkImpl target) {
    final SdkModificator m = target.getSdkModificator();
    final String name = source.getName();
    m.setName(name);
    m.setHomePath(source.getHomePath());
    m.setVersionString(source.getVersionString());
    m.setSdkAdditionalData(source.getSdkAdditionalData());
    m.removeAllRoots();
    for (OrderRootType rootType : OrderRootType.getAllTypes()) {
      for (VirtualFile file : source.getRoots(rootType)) {
        m.addRoot(file, rootType);
      }
    }
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        m.commitChanges();
      }
    });
  }

  private Sdk findExistingSdk(String newSdkHome, SdkType sdkType) {
    Sdk existingSdk = null;
    for (Sdk sdk : mySdksModel.getSdks()) {
      if (sdk.getSdkType() == sdkType && pathsEqual(sdk.getHomePath(), newSdkHome)) {
        existingSdk = sdk;
        break;
      }
    }
    return existingSdk;
  }

  public void dispose() {
    ProjectStructureConfigurable.getInstance(myProject).getProjectJdksModel().removeListener(myListener);
  }

  private void createUIComponents() {
    final FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
    descriptor.setTitle(FlexBundle.message("select.flex.sdk"));
    descriptor.setDescription(FlexBundle.message("select.sdk.description"));
    mySdkPathCombo = new SdkPathCombo(myProject, FlexIdeUtils.getSdkType(), "flex.sdk.combo");
  }
}
