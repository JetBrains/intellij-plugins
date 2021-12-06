/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.perforce.perforce;

import com.intellij.ide.actions.RevealFileAction;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.RelativeFont;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.*;
import org.jetbrains.idea.perforce.perforce.connections.*;
import org.jetbrains.idea.perforce.perforce.login.LoginPerformerImpl;
import org.jetbrains.idea.perforce.perforce.login.LoginState;
import org.jetbrains.idea.perforce.perforce.login.LoginSupport;
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ConfigPanel {
  private JTextField m_port;
  private JTextField m_client;
  private JTextField m_user;
  private JCheckBox m_showCmds;

  private TextFieldWithBrowseButton m_pathToExec;
  private JPanel myPanel;
  private HyperlinkLabel myOutputFileLabel;
  private JComboBox myCharset;
  private JButton myTestConnectionButton;

  private final Project myProject;
  private JCheckBox myShowBranchingHistory;
  private JCheckBox myIsEnabled;
  private JCheckBox myUseLogin;
  private JTextField myServerTimeoutField;
  private TextFieldWithBrowseButton myP4VCPathField;
  private JCheckBox myUsePerforceJobs;
  private JCheckBox myShowIntegratedChangelistsInCheckBox;
  private JRadioButton myUseP4CONFIGOrDefaultRadioButton;
  private JRadioButton myUseConnectionParametersRadioButton;
  private JLabel myPOrtLabel;
  private JLabel myUserLabel;
  private JLabel myClientLabel;
  private JLabel myCharsetLabel;
  private JCheckBox mySwitchToOffline;
  private JPanel myConfigSettingsPanel;
  private JBLabel myP4ConfigWarningLabel;
  private JRadioButton myUseP4IGNOREOrDefaultRadioButton;
  private JBLabel myP4IgnoreWarningLabel;
  private JPanel myIgnoreSettingsPanel;
  private JLabel myIgnoreLabel;
  private JRadioButton myUseIgnoreSettingsRadioButton;
  private TextFieldWithBrowseButton m_pathToIgnore;

  @NlsSafe private static final String CHARSET_ISO8859_1 = "iso8859-1";
  @NlsSafe private static final String CHARSET_ISO8859_15 = "iso8859-15";
  @NlsSafe private static final String CHARSET_eucjp = "eucjp";
  @NlsSafe private static final String CHARSET_shiftjis = "shiftjis";
  @NlsSafe private static final String CHARSET_winansi = "winansi";
  @NlsSafe private static final String CHARSET_macosroman = "macosroman";
  @NlsSafe private static final String CHARSET_utf8 = "utf8";

  public ConfigPanel(final Project project) {
    myProject  = project;

    final ButtonGroup bg = new ButtonGroup();
    bg.add(myUseConnectionParametersRadioButton);
    bg.add(myUseP4CONFIGOrDefaultRadioButton);
    final ActionListener actionListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        reenableConnectPanel();
      }
    };
    myUseConnectionParametersRadioButton.addActionListener(actionListener);
    myUseP4CONFIGOrDefaultRadioButton.addActionListener(actionListener);

    final ButtonGroup ignoreBg = new ButtonGroup();
    ignoreBg.add(myUseIgnoreSettingsRadioButton);
    ignoreBg.add(myUseP4IGNOREOrDefaultRadioButton);
    final ActionListener ignoreActionListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) { reenableIgnorePanel(); }
    };
    myUseIgnoreSettingsRadioButton.addActionListener(ignoreActionListener);
    myUseP4IGNOREOrDefaultRadioButton.addActionListener(ignoreActionListener);

    myTestConnectionButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final boolean isEmpty = PerforceLoginManager.getInstance(myProject).getNotifier().isEmpty();

        final PerforceSettings settings = new PerforceSettings(myProject);
        settings.setCanGoOffline(false);
        applyImpl(settings);

        final TestPerforceConnectionManager connectionManager = new TestPerforceConnectionManager(myProject, !settings.useP4CONFIG);
        final TestLoginManager testLoginManager = new TestLoginManager(myProject, settings, connectionManager);
        final PerforceRunner runner = new PerforceRunner(connectionManager, settings, testLoginManager);
        if (settings.useP4CONFIG) {
          final ConnectionTestDataProvider connectionTestDataProvider =
            new ConnectionTestDataProvider(myProject, connectionManager, runner);
          ProgressManager.getInstance().runProcessWithProgressSynchronously(connectionTestDataProvider::refresh,
                                                                            PerforceBundle.message("connection.test"), false, myProject);
          final P4ConfigConnectionDiagnoseDialog dialog = new P4ConfigConnectionDiagnoseDialog(myProject, connectionTestDataProvider);
          dialog.show();
        } else {
          connectionManager.setSingletonConnection(new SingletonConnection(project, settings));
          final PerforceClientRootsChecker[] checker = new PerforceClientRootsChecker[1];
          ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            final Map<VirtualFile, P4Connection> allConnections = connectionManager.getAllConnections();
            ClientRootsCache cache = ClientRootsCache.getClientRootsCache(project);
            Map<P4Connection, ConnectionInfo> info = PerforceInfoAndClient.calculateInfos(allConnections.values(), runner, cache);
            checker[0] = new PerforceClientRootsChecker(info, allConnections);
          }, PerforceBundle.message("connection.test"), false, myProject);
          PerforceConnectionProblemsNotifier.showSingleConnectionState(project, checker[0]);
        }

        // +-, can do better
        if (! isEmpty) {
          refreshAuthenticationState();
        }
      }
    });

    m_pathToExec.addBrowseFolderListener(PerforceBundle.message("dialog.title.path.to.p4.exe"),
                                         PerforceBundle.message("dialog.description.path.to.p4.exe"),
                                         project,
                                         FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());

    FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor();
    myP4VCPathField.addActionListener(new ComponentWithBrowseButton.BrowseFolderActionListener<>(
      PerforceBundle.message("dialog.title.path.to.p4.exe"), PerforceBundle.message("dialog.description.path.to.p4vc.exe"), myP4VCPathField,
      project, fileChooserDescriptor, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT) {
      @Override
      protected VirtualFile getInitialFile() {
        final VirtualFile file = super.getInitialFile();
        if (file == null && SystemInfo.isMac) {
          return LocalFileSystem.getInstance().refreshAndFindFileByPath("/Applications/p4vc");
        }
        return file;
      }

      @Override
      protected void onFileChosen(@NotNull VirtualFile chosenFile) {
        super.onFileChosen(chosenFile);
      }
    });

    m_pathToIgnore.addBrowseFolderListener(PerforceBundle.message("dialog.title.path.to.p4.ignore"),
                                           PerforceBundle.message("dialog.description.path.to.p4.ignore"),
                                           project,
                                           FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());

    reenableConnectPanel();

    reenableIgnorePanel();

    if (myProject.isDefault()) {
      hideProjectSpecificControls();
    }


    String unsetEnv = P4ConfigHelper.getUnsetP4EnvironmentConfig();
    if (!unsetEnv.isEmpty()) {
      myP4ConfigWarningLabel.setText(PerforceBundle.message("radio.no.p4config.env", unsetEnv));
      myP4ConfigWarningLabel.setVisible(true);
      RelativeFont.SMALL.install(myP4ConfigWarningLabel);
    }

    myP4IgnoreWarningLabel.setVisible(!P4ConfigHelper.hasP4IgnoreSettingInEnvironment());
    RelativeFont.SMALL.install(myP4IgnoreWarningLabel);
  }

  private void hideProjectSpecificControls() {
    myIsEnabled.setVisible(false);
    myConfigSettingsPanel.setVisible(false);
    myIgnoreSettingsPanel.setVisible(false);
    myTestConnectionButton.setVisible(false);
  }

  private void refreshAuthenticationState() {
    PerforceConnectionManager.getInstance(myProject).updateConnections();
  }

  private void reenableConnectPanel() {
    boolean useP4Conf = myUseP4CONFIGOrDefaultRadioButton.isSelected();
    m_port.setEnabled(!useP4Conf);
    m_client.setEnabled(!useP4Conf);
    m_user.setEnabled(!useP4Conf);
    myCharset.setEnabled(!useP4Conf);

    myUserLabel.setEnabled(!useP4Conf);
    myPOrtLabel.setEnabled(!useP4Conf);
    myClientLabel.setEnabled(!useP4Conf);
    myCharsetLabel.setEnabled(!useP4Conf);
    myTestConnectionButton.setEnabled(! myProject.isDefault() || ! useP4Conf);

    reenableIgnorePanel();
  }

  private boolean shouldIgnorePanelBeEnabled() {
    boolean useP4CONFIG = myUseP4CONFIGOrDefaultRadioButton.isSelected();
    if (useP4CONFIG && P4ConfigHelper.hasP4ConfigSettingInEnvironment()) {
      String basePath = myProject.getBasePath();
      @Nullable String configFileName = P4ConfigHelper.getP4ConfigFileName();
      P4ConnectionParameters params = P4ConnectionCalculator.getParametersFromConfig(new File(basePath), configFileName);

      return params.getIgnoreFileName() == null;
    }

    return true;
  }

  private void setBorderForIgnorePanel(@Nls String title) {
    Border etchedBorder = BorderFactory.createEtchedBorder();
    Border etchedTitledBorder = BorderFactory.createTitledBorder(etchedBorder, title);
    myIgnoreSettingsPanel.setBorder(etchedTitledBorder);
  }

  private void reenableIgnorePanel() {
    boolean useP4Ignore = myUseP4IGNOREOrDefaultRadioButton.isSelected();

    boolean enablePanel = shouldIgnorePanelBeEnabled();

    myIgnoreSettingsPanel.setEnabled(enablePanel);
    setBorderForIgnorePanel(enablePanel ? PerforceBundle.message("border.configure.ignore.settings")
                                        : PerforceBundle.message("border.configure.ignore.settings.disabled"));

    myUseIgnoreSettingsRadioButton.setEnabled(enablePanel);
    myUseP4IGNOREOrDefaultRadioButton.setEnabled(enablePanel);
    myP4IgnoreWarningLabel.setEnabled(enablePanel);
    myIgnoreLabel.setEnabled(enablePanel && !useP4Ignore);
    m_pathToIgnore.setEnabled(enablePanel && !useP4Ignore);
  }

  public void resetFrom(PerforceSettings settings) {
    myUseP4CONFIGOrDefaultRadioButton.setSelected(settings.useP4CONFIG);
    myUseConnectionParametersRadioButton.setSelected(! settings.useP4CONFIG);
    myUseP4IGNOREOrDefaultRadioButton.setSelected(settings.useP4IGNORE);
    myUseIgnoreSettingsRadioButton.setSelected(! settings.useP4IGNORE);
    m_port.setText(settings.port);
    m_client.setText(settings.client);
    m_user.setText(settings.user);
    m_showCmds.setSelected(settings.showCmds);
    m_pathToExec.setText(settings.pathToExec);
    myP4VCPathField.setText(settings.PATH_TO_P4VC);
    m_pathToIgnore.setText(settings.pathToIgnore);

    final File dumpFile = PerforceRunner.getDumpFile();
    if (dumpFile.exists()) {
      myOutputFileLabel.setHyperlinkText("'", dumpFile.getAbsolutePath(), "'");
    } else {
      myOutputFileLabel.setText("'" + dumpFile.getAbsolutePath() + "'");
    }
    myOutputFileLabel.addHyperlinkListener(new HyperlinkListener() {
      @Override
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          RevealFileAction.openFile(dumpFile);
        }
      }
    });

    myShowBranchingHistory.setSelected(settings.SHOW_BRANCHES_HISTORY);
    myUseLogin.setSelected(settings.USE_LOGIN);
    myServerTimeoutField.setText(Integer.toString(settings.SERVER_TIMEOUT/1000));
    myUsePerforceJobs.setSelected(settings.USE_PERFORCE_JOBS);
    mySwitchToOffline.setSelected(settings.myCanGoOffline);
    myShowIntegratedChangelistsInCheckBox.setSelected(settings.SHOW_INTEGRATED_IN_COMMITTED_CHANGES);

    myCharset.removeAllItems();
    myCharset.addItem(PerforceSettings.getCharsetNone());
    myCharset.addItem(CHARSET_ISO8859_1);
    myCharset.addItem(CHARSET_ISO8859_15);
    myCharset.addItem(CHARSET_eucjp);
    myCharset.addItem(CHARSET_shiftjis);
    myCharset.addItem(CHARSET_winansi);
    myCharset.addItem(CHARSET_macosroman);
    myCharset.addItem(CHARSET_utf8);

    myIsEnabled.setSelected(settings.ENABLED);

    myCharset.setSelectedItem(settings.CHARSET);

    reenableConnectPanel();

    reenableIgnorePanel();
  }

  public void applyTo(PerforceSettings settings) {
    applyImpl(settings);
    if (settings.ENABLED != myIsEnabled.isSelected()) {
      if (myIsEnabled.isSelected()) {
        settings.enable();
      } else {
        settings.disable(true);
      }
    }
  }

  private void applyImpl(PerforceSettings settings) {
    settings.useP4CONFIG = myUseP4CONFIGOrDefaultRadioButton.isSelected();
    settings.useP4IGNORE = myUseP4IGNOREOrDefaultRadioButton.isSelected();
    settings.port = m_port.getText();
    settings.client = m_client.getText();
    settings.user = m_user.getText();
    settings.showCmds = m_showCmds.isSelected();
    settings.pathToIgnore = m_pathToIgnore.getText();
    boolean execChanged = !Objects.equals(settings.pathToExec, m_pathToExec.getText());
    settings.pathToExec = m_pathToExec.getText();
    if (execChanged) {
      PerforceManager.getInstance(myProject).resetClientVersion();
    }
    settings.PATH_TO_P4VC = myP4VCPathField.getText();
    settings.CHARSET = (String)myCharset.getSelectedItem();
    settings.SHOW_BRANCHES_HISTORY = myShowBranchingHistory.isSelected();
    settings.USE_LOGIN = myUseLogin.isSelected();
    try {
      settings.SERVER_TIMEOUT = Integer.parseInt(myServerTimeoutField.getText()) * 1000;
    }
    catch(NumberFormatException ex) {
      // ignore
    }
    settings.USE_PERFORCE_JOBS = myUsePerforceJobs.isSelected();
    settings.myCanGoOffline = mySwitchToOffline.isSelected();
    settings.SHOW_INTEGRATED_IN_COMMITTED_CHANGES = myShowIntegratedChangelistsInCheckBox.isSelected();
  }

  public boolean equalsToSettings(PerforceSettings settings) {
    if (connectionPartDiffer(settings)) return false;

    if (!Integer.toString(settings.SERVER_TIMEOUT / 1000).equals(myServerTimeoutField.getText())) return false;

    if (settings.showCmds != m_showCmds.isSelected()) return false;
    if (settings.SHOW_BRANCHES_HISTORY != myShowBranchingHistory.isSelected()) return false;
    if (!Objects.equals(settings.pathToExec, m_pathToExec.getText().trim())) return false;
    if (!Objects.equals(settings.pathToIgnore, m_pathToIgnore.getText().trim())) return false;
    if (settings.useP4IGNORE != myUseP4IGNOREOrDefaultRadioButton.isSelected()) return false;
    if (!Objects.equals(settings.PATH_TO_P4VC, myP4VCPathField.getText().trim())) return false;
    if (! Comparing.equal(settings.USE_PERFORCE_JOBS, myUsePerforceJobs.isSelected())) return false;
    if (! Comparing.equal(settings.myCanGoOffline, mySwitchToOffline.isSelected())) return false;
    if (! Comparing.equal(settings.SHOW_INTEGRATED_IN_COMMITTED_CHANGES, myShowIntegratedChangelistsInCheckBox.isSelected())) return false;
    return Comparing.equal(settings.CHARSET, myCharset.getSelectedItem());
  }

  private boolean connectionPartDiffer(PerforceSettings settings) {
    if (settings.useP4CONFIG != myUseP4CONFIGOrDefaultRadioButton.isSelected()) return true;
    if (settings.USE_LOGIN != myUseLogin.isSelected()) return true;
    if (!Objects.equals(settings.port, m_port.getText().trim())) return true;
    if (!Objects.equals(settings.client, m_client.getText().trim())) return true;
    if (!Objects.equals(settings.user, m_user.getText().trim())) return true;
    if (settings.ENABLED != myIsEnabled.isSelected()) return true;
    return false;
  }

  public JComponent getPanel() {
    return myPanel;
  }

  private static class ConnectionTestDataProvider implements ConnectionDiagnoseRefresher {
    private final Project myProject;
    private final TestPerforceConnectionManager myConnectionManager;
    private final PerforceRunner myRunner;
    private PerforceClientRootsChecker myChecker = new PerforceClientRootsChecker();
    private Map<P4Connection, ConnectionInfo> myInfo = Collections.emptyMap();
    private PerforceMultipleConnections myMc;

    ConnectionTestDataProvider(Project project,
                                      TestPerforceConnectionManager connectionManager, PerforceRunner runner) {
      myProject = project;
      myConnectionManager = connectionManager;
      myRunner = runner;
    }

    @Override
    public void refresh() {
      final P4ConnectionCalculator calculator = new P4ConnectionCalculator(myProject);
      calculator.execute();
      // !! check connectivity & authorization separately
      myMc = calculator.getMultipleConnections();
      final Map<VirtualFile, P4Connection> map = myMc.getAllConnections();
      myConnectionManager.setMc(myMc);
      myInfo = PerforceInfoAndClient.recalculateInfos(myInfo, map.values(), myRunner, ClientRootsCache.getClientRootsCache(myProject)).newInfo;
      myChecker = new PerforceClientRootsChecker(myInfo, map);
    }

    @Override
    public PerforceMultipleConnections getMultipleConnections() {
      return myMc;
    }

    @Override
    public P4RootsInformation getP4RootsInformation() {
      return myChecker;
    }
  }

  private static final class TestLoginManager implements LoginSupport {
    private final Project myProject;
    private final PerforceSettings mySettings;
    private final PerforceConnectionManagerI myConnectionManagerI;

    private TestLoginManager(final Project project, final PerforceSettings settings, final PerforceConnectionManagerI connectionManagerI) {
      myProject = project;
      mySettings = settings;
      myConnectionManagerI = connectionManagerI;
    }

    @Override
    public boolean silentLogin(final P4Connection connection) throws VcsException {
      String password = connection instanceof P4ParametersConnection
                        ? ((P4ParametersConnection) connection).getParameters().getPassword()
                        : mySettings.getPasswd();
      final LoginPerformerImpl loginPerformer = new LoginPerformerImpl(myProject, connection, myConnectionManagerI);

      if (password != null && loginPerformer.login(password).isSuccess()) {
        return true;
      }

      while (true) {
        password = mySettings.requestForPassword(mySettings.useP4CONFIG ? connection : null);
        if (password == null) return false;
        final LoginState login = loginPerformer.login(password);
        if (login.isSuccess()) {
          PerforceConnectionManager.getInstance(myProject).updateConnections();
          return true;
        }
      }
    }
    @Override
    public void notLogged(P4Connection connection) {
    }

  }

  private static class TestPerforceConnectionManager implements PerforceConnectionManagerI {
    private final Project myProject;
    private SingletonConnection mySingletonConnection;
    private final boolean mySingleton;
    private PerforceMultipleConnections myMc;

    TestPerforceConnectionManager(Project project, boolean singleton) {
      myProject = project;
      mySingleton = singleton;
    }

    public void setSingletonConnection(SingletonConnection singletonConnection) {
      mySingletonConnection = singletonConnection;
    }

    public void setMc(PerforceMultipleConnections mc) {
      myMc = mc;
    }

    @Override
    public PerforceMultipleConnections getMultipleConnectionObject() {
      return myMc;
    }
    @NotNull
    @Override
    public Map<VirtualFile, P4Connection> getAllConnections() {
      if (mySingleton) {
        Map<VirtualFile, P4Connection> result = new LinkedHashMap<>();
        for (VirtualFile root : ProjectLevelVcsManager.getInstance(myProject).getRootsUnderVcs(PerforceVcs.getInstance(myProject))) {
          result.put(root, mySingletonConnection);
        }
        return result;
      }

      return myMc.getAllConnections();
    }
    @Override
    public P4Connection getConnectionForFile(@NotNull File file) {
      if (mySingleton) {
        return mySingletonConnection;
      } else {
        final VirtualFile vf = PerforceConnectionManager.findNearestLiveParentFor(file);
        if (vf == null) return null;
        return myMc.getConnection(vf);
      }
    }
    @Override
    public P4Connection getConnectionForFile(@NotNull P4File file) {
      if (mySingleton) {
        return mySingletonConnection;
      } else {
        return getConnectionForFile(file.getLocalFile());
      }
    }
    @Override
    public P4Connection getConnectionForFile(@NotNull VirtualFile file) {
      if (mySingleton) {
        return mySingletonConnection;
      } else {
        return myMc.getConnection(file);
      }
    }
    @Override
    public boolean isSingletonConnectionUsed() {
      return mySingleton;
    }
    @Override
    public void updateConnections() {
    }

    @Override
    public boolean isUnderProjectConnections(@NotNull File file) {
      return true;
    }
  }
}
