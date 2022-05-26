package jetbrains.plugins.yeoman.projectGenerator.template;

import com.intellij.ide.util.projectWizard.WebProjectTemplate;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;
import jetbrains.plugins.yeoman.YeomanBundle;
import jetbrains.plugins.yeoman.settings.YeomanGlobalSettings;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class YeomanProjectGeneratorPanel implements Disposable, YeomanProjectGeneratorOwnerPanel {


  private final YeomanProjectGenerator.Settings mySettings;

  JLabel myErrorLabel;

  JPanel myLinkAndErrorPanel;
  JButton myCreateButton;
  JPanel myMainPanel;
  LabeledComponent<TextFieldWithBrowseButton> myLocation;
  ActionListener myCloseActionListener;
  JBScrollPane myScrollPane;
  final YeomanGlobalSettings myYeomanGlobalSettings;
  ValidateHandler myValidateHandler;

  @Nullable
  YeomanProjectGeneratorSubPanel mySubPanel;

  public boolean isEnable() {
    return mySubPanel == null || mySubPanel.isCreateButtonEnabled();
  }

  public YeomanProjectGenerator.Settings getSettings() {
    return mySettings;
  }

  public YeomanProjectGeneratorPanel(YeomanProjectGenerator.Settings path) {
    mySettings = path;
    myYeomanGlobalSettings = YeomanGlobalSettings.getInstance();
  }

  @Nls
  @Nullable
  public String validate() {
    if (mySubPanel != null) {
      return mySubPanel.validate();
    }
    return null;
  }

  public void init(@NotNull LabeledComponent<TextFieldWithBrowseButton> location,
                   @NotNull JLabel errorLabel,
                   @NotNull JButton createButton,
                   ActionListener closeActionListener) {
    myCloseActionListener = closeActionListener;
    myMainPanel = new JPanel(new BorderLayout());
    myLocation = location;
    myErrorLabel = errorLabel;
    myScrollPane = createScrollPane();

    myMainPanel.add(myScrollPane, BorderLayout.CENTER);

    final JPanel bottomPanel = new JPanel(new BorderLayout());

    myCreateButton = createButton;
    myCreateButton.setText(YeomanBundle.message("yeoman.generator.next"));

    myCreateButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (mySubPanel != null) {
          mySubPanel = mySubPanel.next(e);
          mySubPanel.render();
        }
      }
    });


    myLinkAndErrorPanel = new JPanel(new BorderLayout(10, 0));
    myLinkAndErrorPanel.add(myErrorLabel, BorderLayout.WEST);

    bottomPanel.add(myLinkAndErrorPanel, BorderLayout.WEST);
    bottomPanel.add(myCreateButton, BorderLayout.EAST);
    myMainPanel.add(bottomPanel, BorderLayout.SOUTH);

    final JPanel titlePanel = WebProjectTemplate.createTitlePanel();
    myMainPanel.add(titlePanel, BorderLayout.NORTH);
  }

  public static JBScrollPane createScrollPane() {
    JBScrollPane scrollPane = new JBScrollPane();
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setBorder(null);

    return scrollPane;
  }

  public void showFirstStep() {
    mySubPanel = new YeomanProjectGeneratorWelcomePanel(this, mySettings);
    mySubPanel.render();
  }


  @Override
  @NotNull
  public JPanel getMainPanel() {
    return myMainPanel;
  }

  public void setValidateHandler(ValidateHandler handler) {
    myValidateHandler = handler;
  }

  @Override
  public void dispose() {
    if (mySubPanel != null) {
      Disposer.dispose(mySubPanel);

      UIUtil.invokeLaterIfNeeded(() -> showFirstStep());
    }
  }

  @Nullable
  @Override
  public ValidateHandler getValidateHandler() {
    return myValidateHandler;
  }

  @Override
  public void setCentralComponent(@NotNull JComponent component) {
    myScrollPane.setViewportView(component);
  }

  @Override
  public void setBottomComponent(JComponent component) {
    myLinkAndErrorPanel.removeAll();
    myLinkAndErrorPanel.add(component, BorderLayout.CENTER);
  }

  @Override
  public void setMainButtonEnable(boolean isEnable) {
    myCreateButton.setEnabled(isEnable);
  }

  @Override
  @NotNull
  public String getLocationTitle() {
    return myLocation.getComponent().getText();
  }

  @Override
  @Nullable
  public LabeledComponent<TextFieldWithBrowseButton> getLocationComponent() {
    return  myLocation;
  }

  @Override
  public void close(ActionEvent e) {
    myCloseActionListener.actionPerformed(e);
  }

  @Override
  public void setMainButtonName(@NotNull String newName) {
    myCreateButton.setName(newName);
  }
}
