package jetbrains.plugins.yeoman.projectGenerator.ui.list;

import com.intellij.icons.AllIcons;
import com.intellij.ide.ui.RoundedActionButton;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.HorizontalLayout;
import com.intellij.ui.components.panels.VerticalLayout;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.StartupUiUtil;
import com.intellij.util.ui.UIUtil;
import jetbrains.plugins.yeoman.YeomanBundle;
import jetbrains.plugins.yeoman.generators.YeomanGeneratorFullInfo;
import jetbrains.plugins.yeoman.generators.YeomanGeneratorInfo;
import jetbrains.plugins.yeoman.generators.YeomanGeneratorInstaller;
import jetbrains.plugins.yeoman.generators.YeomanInstalledGeneratorInfo;
import jetbrains.plugins.yeoman.settings.YeomanGlobalSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.intellij.openapi.ui.Messages.showYesNoDialog;

public class YeomanGeneratorInfoPanelHeader {

  private final JBPanel myRootPanel;
  private final JBLabel myGeneratorHeaderLabel;
  private final YeomanGeneratorsMain myParent;
  private final boolean myHasInstallButton;
  private YeomanGeneratorInfo myInfo;
  private final JButton myActionButton;
  private final JPanel myStartsHolder;

  enum State {
    AVAILABLE,
    LOCAL_INSTALLED,
    GLOBAL_INSTALLED,
    INCORRECT
  }

  private State myState = State.AVAILABLE;

  public YeomanGeneratorInfoPanelHeader(final YeomanGeneratorsMain parent, boolean allAvailable) {
    myParent = parent;
    myHasInstallButton = allAvailable;
    myRootPanel = new JBPanel(new VerticalLayout(5, SwingConstants.LEFT));
    myRootPanel.setBackground(UIUtil.getTextFieldBackground());
    myRootPanel.setBorder(JBUI.Borders.empty());
    myGeneratorHeaderLabel = new JBLabel();
    myGeneratorHeaderLabel.setFont(StartupUiUtil.getLabelFont());
    UIUtil.addBorder(myGeneratorHeaderLabel, JBUI.Borders.empty(12, 0, 0, 0));
    myRootPanel.add(myGeneratorHeaderLabel);
    myActionButton = createActionButton();

    RelativeFont.BOLD.install(myGeneratorHeaderLabel);
    myRootPanel.add(myActionButton);
    myActionButton.setVisible(false);

    myStartsHolder = new JPanel(new BorderLayout());
    myStartsHolder.setBackground(UIUtil.getTextFieldBackground());
    myRootPanel.add(myStartsHolder);
  }

  private JButton createActionButton() {
    final JButton actionButton = new RoundedActionButton(5, 8) {

      @Override
      @NotNull
      protected Color getButtonForeground() {
        return switch (myState) {
          case AVAILABLE -> new JBColor(Gray._240, Gray._210);
          case GLOBAL_INSTALLED, LOCAL_INSTALLED -> new JBColor(Gray._0, Gray._210);
          case INCORRECT -> new JBColor(Gray._80, Gray._60);
        };
      }

      @Override
      @NotNull
      protected Paint getBackgroundPaint() {
        return switch (myState) {
          case AVAILABLE ->
            new JBGradientPaint(this,
                                new JBColor(new Color(96, 204, 105), new Color(81, 149, 87)),
                                new JBColor(new Color(50, 101, 41), new Color(40, 70, 47)));
          case GLOBAL_INSTALLED, LOCAL_INSTALLED ->
            //noinspection UnregisteredNamedColor
            StartupUiUtil.isUnderDarcula()
            ?
            ColorUtil.mix(JBColor.namedColor("Button.startBackground", JBColor.namedColor("Button.darcula.startColor", 0x4C5052)),
                          JBColor.namedColor("Button.endBackground", JBColor.namedColor("Button.darcula.endColor", 0x4C5052)), 0.5) : Gray._240;
          case INCORRECT -> Gray._238;
        };
      }

      @Override
      @NotNull
      protected Paint getBackgroundBorderPaint() {
        return switch (myState) {
          case AVAILABLE -> new JBColor(new Color(201, 223, 201), Gray._70);
          case GLOBAL_INSTALLED, LOCAL_INSTALLED -> new JBColor(Gray._220, Gray._100.withAlpha(180));
          case INCORRECT -> Gray._208;
        };
      }


      @Override
      public String getText() {
        return switch (myState) {
          case AVAILABLE -> YeomanBundle.message("yeoman.generators.dialog.install.generator");
          case GLOBAL_INSTALLED, LOCAL_INSTALLED -> YeomanBundle.message("yeoman.generators.dialog.uninstall.generator");
          case INCORRECT -> super.getText();
        };
      }

      @Override
      public Icon getIcon() {
        return switch (myState) {
          case AVAILABLE -> AllIcons.Actions.Download;
          case GLOBAL_INSTALLED, LOCAL_INSTALLED -> AllIcons.Actions.Cancel;
          case INCORRECT -> super.getIcon();
        };
      }
    };

    actionButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final YeomanGeneratorInfo yeomanGeneratorInfo = myInfo;
        if (yeomanGeneratorInfo != null) {
          boolean updated = false;
          switch (myState) {
            case AVAILABLE -> {
              try {
                updated = null != YeomanGeneratorInstaller.getInstance().install(yeomanGeneratorInfo, YeomanGlobalSettings.getInstance());
              }
              catch (RuntimeException ex) {
                Messages.showErrorDialog(myParent.getMainPanel(), YeomanBundle.message("yeoman.generators.dialog.install.error", yeomanGeneratorInfo.getName()));
              }
            }
            case GLOBAL_INSTALLED, LOCAL_INSTALLED -> {
              if (Messages.YES != showYesNoDialog(myParent.getMainPanel(),
                                                  YeomanBundle.message("yeoman.generators.prompt.uninstall", yeomanGeneratorInfo.getName()),
                                                  YeomanBundle.message("yeoman.generators.prompt.uninstall.title"),
                                                  Messages.getQuestionIcon())) {
                return;
              }

              final YeomanInstalledGeneratorInfo installedGeneratorInfo = myParent.getInstalledGeneratorInfo(myInfo);
              if (installedGeneratorInfo != null) {
                updated = YeomanGeneratorInstaller.getInstance().uninstall(installedGeneratorInfo);
              }
            }
            case INCORRECT -> {}
          }
          if (updated) {
            myParent.handleUpdate();
            myParent.select(yeomanGeneratorInfo);
          }
        }
      }
    });

    return actionButton;
  }


  public JComponent getComponent() {
    return myRootPanel;
  }

  public void updateInfo(YeomanGeneratorInfo info) {
    ApplicationManager.getApplication().assertIsDispatchThread();

    myInfo = info;
    myRootPanel.setVisible(true);

    myGeneratorHeaderLabel.setText((info == null ? YeomanBundle.message("label.text.no.info") : info.getName()));

    myActionButton.setVisible(false);
    myState = getState(info);
    if (myInfo != null) {
      myActionButton.setVisible(myHasInstallButton && myState != State.GLOBAL_INSTALLED && myState != State.INCORRECT);

      final YeomanGeneratorFullInfo fullInfo = myParent.getFullGeneratorInfo(info);
      myStartsHolder.removeAll();
      if (fullInfo == null) {
        myStartsHolder.setVisible(false);
      }
      else {
        myStartsHolder.setVisible(true);
        myStartsHolder.add(createPanelWithStartsGithubAndRemoveButton(fullInfo));
      }
    }
  }

  @NotNull
  private State getState(YeomanGeneratorInfo info) {
    if (YeomanGlobalSettings.getInstance().getInterpreter() == null) {
      return State.INCORRECT;
    }

    final YeomanInstalledGeneratorInfo installInfo = myParent.getInstalledGeneratorInfo(info);
    if (installInfo == null) return State.AVAILABLE;

    return installInfo.isGlobal() ? State.GLOBAL_INSTALLED : State.LOCAL_INSTALLED;
  }

  @Nullable
  private static JComponent createStarsLabel(@NotNull YeomanGeneratorFullInfo fullInfo) {
    final int stars = fullInfo.getStars();
    if (stars > 0) {
      final JBLabel label = new JBLabel(String.valueOf(stars), AllIcons.Ide.Rating, SwingConstants.LEFT);
      label.setFont(StartupUiUtil.getLabelFont());

      return label;
    }

    return null;
  }

  @Nullable
  private static JComponent createOnGithub(@NotNull YeomanGeneratorFullInfo fullInfo) {
    String text = fullInfo.getWebsite();
    if (!StringUtil.isEmpty(text)) {
      final JEditorPane pane = new JEditorPane(UIUtil.HTML_MIME, "");
      pane.setEditable(false);
      pane.addHyperlinkListener(new BrowserHyperlinkListener());
      final StringBuilder builder = new StringBuilder("<a href=\"" + text + "\">on GitHub</a>");
      YeomanGeneratorsMain.setTextValue(builder, null, pane);

      return pane;
    }

    return null;
  }

  private static JPanel createPanelWithStartsGithubAndRemoveButton(@NotNull YeomanGeneratorFullInfo fullInfo) {
    final JPanel result = new JPanel(new HorizontalLayout(1, SwingConstants.CENTER));
    result.setBorder(JBUI.Borders.empty());
    result.setBackground(UIUtil.getTextFieldBackground());
    final JComponent label = createStarsLabel(fullInfo);
    if (label != null) {
      result.add(label);
    }

    final JComponent github = createOnGithub(fullInfo);
    if (github != null) {
      result.add(github);
    }

    return result;
  }
}
