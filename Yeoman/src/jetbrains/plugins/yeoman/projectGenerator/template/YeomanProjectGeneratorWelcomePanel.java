package jetbrains.plugins.yeoman.projectGenerator.template;

import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.options.newEditor.SettingsDialog;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.UIUtil;
import jetbrains.plugins.yeoman.YeomanBundle;
import jetbrains.plugins.yeoman.generators.YeomanGeneratorInfo;
import jetbrains.plugins.yeoman.generators.YeomanInstalledGeneratorInfo;
import jetbrains.plugins.yeoman.projectGenerator.ui.list.YeomanInstalledGeneratorsMain;
import jetbrains.plugins.yeoman.settings.YeomanConfigurable;
import jetbrains.plugins.yeoman.settings.YeomanGlobalSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

public class YeomanProjectGeneratorWelcomePanel implements YeomanProjectGeneratorSubPanel {

  private final YeomanProjectGeneratorOwnerPanel myOwner;
  private final YeomanProjectGenerator.Settings mySettings;
  private final YeomanGlobalSettings myYeomanGlobalSettings;
  private YeomanInstalledGeneratorsMain myGeneratorsMain;
  private HyperlinkLabel myNodeAndYeomanLink;
  private JBTextField myOptionsTextField;


  public YeomanProjectGeneratorWelcomePanel(YeomanProjectGeneratorOwnerPanel parent, YeomanProjectGenerator.Settings settings) {
    myOwner = parent;
    mySettings = settings;
    myYeomanGlobalSettings = YeomanGlobalSettings.getInstance();
  }

  @Override
  public void render() {
    myGeneratorsMain = new YeomanInstalledGeneratorsMain();

    final JPanel bottomPanel = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 5, true, false));
    final ArrayList<PanelWithAnchor> toMergeWithAnchorList = new ArrayList<>();

    final LabeledComponent<TextFieldWithBrowseButton> location = myOwner.getLocationComponent();

    if (location != null) {
      bottomPanel.add(location);
      toMergeWithAnchorList.add(location);
    }
    myNodeAndYeomanLink = createNodeAndYeomanLink();

    JPanel generatorsPanel = createGeneratorsWithLabelPanel(toMergeWithAnchorList);

    final JPanel generatorsMainPanel = myGeneratorsMain.getMainPanel();

    generatorsPanel.add(generatorsMainPanel, BorderLayout.CENTER);

    bottomPanel.add(generatorsPanel);


    final LabeledComponent<JBTextField> optionsTextLabelComponent = createOptionsPanel(toMergeWithAnchorList);
    final LabeledComponent<?> installedGeneratorsButtonComponent = createInstallGeneratorsButtonPanel(toMergeWithAnchorList);
    bottomPanel.add(optionsTextLabelComponent);
    bottomPanel.add(installedGeneratorsButtonComponent);


    UIUtil.mergeComponentsWithAnchor(toMergeWithAnchorList);

    final JPanel wrapperForWelcome = new JPanel(new BorderLayout());
    wrapperForWelcome.add(bottomPanel, BorderLayout.NORTH);

    myOwner.setBottomComponent(myNodeAndYeomanLink);
    myOwner.setCentralComponent(wrapperForWelcome);
  }

  private @NotNull LabeledComponent<?> createInstallGeneratorsButtonPanel(ArrayList<PanelWithAnchor> toMergeWithAnchor) {
    final JButton button = myGeneratorsMain.getInstallGeneratorsButton();
    final JPanel buttonToLeftPanel = new JPanel(new BorderLayout());
    buttonToLeftPanel.add(button, BorderLayout.WEST);
    final LabeledComponent<?> installedGeneratorsButtonComponent =
      LabeledComponent.create(buttonToLeftPanel, "", BorderLayout.WEST);
    toMergeWithAnchor.add(installedGeneratorsButtonComponent);
    return installedGeneratorsButtonComponent;
  }

  private @NotNull LabeledComponent<JBTextField> createOptionsPanel(ArrayList<PanelWithAnchor> toMergeWithAnchor) {
    myOptionsTextField = new JBTextField(10);
    final LabeledComponent<JBTextField> optionsTextLabelComponent =
      LabeledComponent.create(myOptionsTextField, YeomanBundle.message("yeoman.generator.options"), BorderLayout.WEST);
    toMergeWithAnchor.add(optionsTextLabelComponent);
    return optionsTextLabelComponent;
  }

  private static @NotNull JPanel createGeneratorsWithLabelPanel(ArrayList<PanelWithAnchor> toMergeWithAnchor) {
    JPanel generatorsPanel = new JPanel(new BorderLayout(UIUtil.DEFAULT_HGAP, 2));
    final JPanel labelToTopPanel = new JPanel(new BorderLayout());
    final JBLabel labelGenerator = new JBLabel(YeomanBundle.message("yeoman.generator.generator"));
    labelToTopPanel.add(labelGenerator, BorderLayout.NORTH);
    generatorsPanel.add(labelToTopPanel, BorderLayout.WEST);
    toMergeWithAnchor.add(new PanelWithAnchor() {
      JComponent anchor = labelGenerator;

      @Override
      public JComponent getAnchor() {
        return anchor;
      }

      @Override
      public void setAnchor(@Nullable JComponent newAnchor) {
        anchor = newAnchor;
        labelGenerator.setAnchor(newAnchor);
      }
    });
    return generatorsPanel;
  }

  private HyperlinkLabel createNodeAndYeomanLink() {
    HyperlinkLabel nodeAndYeomanLink =
      new HyperlinkLabel(YeomanBundle.message("yeoman.generators.dialog.configure.node"), UIUtil.getTextFieldBackground());
    nodeAndYeomanLink.addHyperlinkListener(new HyperlinkListener() {
      @Override
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (ShowSettingsUtil.getInstance()
              .editConfigurable(myOwner.getMainPanel(), SettingsDialog.DIMENSION_KEY, new YeomanConfigurable()) &&
            myOwner.getValidateHandler() != null) {
          //send validate signal
          myOwner.getValidateHandler().validate();
        }
      }
    });

    return nodeAndYeomanLink;
  }


  @Override
  public @Nls String validate() {
    if (myYeomanGlobalSettings.getInterpreter() == null) {
      return YeomanBundle.message("yeoman.generators.dialog.configure.error.node");
    }
    if (StringUtil.isEmpty(myYeomanGlobalSettings.getYoPackagePath())) {
      return YeomanBundle.message("yeoman.generators.dialog.configure.error.yeoman");
    }

    return null;
  }

  @Override
  public @NotNull YeomanProjectGeneratorSubPanel next(ActionEvent e) {
    commitSettings();

    myNodeAndYeomanLink.setVisible(false);
    myOwner.setMainButtonEnable(false);

    return new YeomanProjectGeneratorRunPanel(myOwner, mySettings);
  }

  @Override
  public void commitSettings() {
    final String path = mySettings.tempPath;
    final File file = createTempStoreDirectory(path);

    mySettings.appPath = file.getAbsolutePath();
    mySettings.options = myOptionsTextField.getText();
    final YeomanGeneratorInfo object = myGeneratorsMain.getSelectedObject();
    mySettings.info = object instanceof YeomanInstalledGeneratorInfo ? (YeomanInstalledGeneratorInfo)object : null;
  }

  private @NotNull File createTempStoreDirectory(String path) {
    final File file = new File(path, new File(myOwner.getLocationTitle()).getName());
    if (file.exists()) {
      FileUtil.delete(file);
    }
    FileUtil.createDirectory(file);
    return file;
  }

  @Override
  public boolean isCreateButtonEnabled() {
    return true;
  }

  @Override
  public void dispose() {
    //do nothing
  }
}
