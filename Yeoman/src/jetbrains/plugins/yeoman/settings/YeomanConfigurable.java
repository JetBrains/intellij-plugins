package jetbrains.plugins.yeoman.settings;

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.SwingHelper;
import jetbrains.plugins.yeoman.YeomanBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class YeomanConfigurable implements Configurable, Configurable.NoScroll {

  public static final String ID = "settings.javascript.yeoman";
  private final YeomanGlobalSettings myYeomanGlobalSettings;
  private NodeJsInterpreterField myNodeTextField;
  private TextFieldWithHistoryWithBrowseButton myYeomanPackageField;

  public YeomanConfigurable() {
    myYeomanGlobalSettings = YeomanGlobalSettings.getInstance();
  }

  @Nls
  @Override
  public String getDisplayName() {
    return YeomanBundle.message("settings.yeoman.name");
  }

  @Nullable
  @Override
  public String getHelpTopic() {
    return "reference.settings.yeoman";
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    Project project = ProjectManager.getInstance().getDefaultProject();
    myNodeTextField = new NodeJsInterpreterField(project, false);


    myYeomanPackageField = SwingHelper.createTextFieldWithHistoryWithBrowseButton(null, YeomanBundle.message("yeoman.configurable.package.dialog.title"),
                                                                                  FileChooserDescriptorFactory
                                                                                    .createSingleFolderDescriptor(), null);
    FormBuilder builder = FormBuilder.createFormBuilder();
    builder.addLabeledComponent(NodeJsInterpreterField.getLabelTextForComponent(), myNodeTextField);
    builder.addLabeledComponent(YeomanBundle.message("yeoman.configurable.package.dialog.label"), myYeomanPackageField);
    JPanel panel = builder.getPanel();
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(panel, BorderLayout.NORTH);
    wrapper.setPreferredSize(new Dimension(400, 200));
    return wrapper;
  }

  @Override
  public boolean isModified() {
    return !StringUtil.equals(StringUtil.notNullize(myYeomanGlobalSettings.getNodeInterpreterRefName()),
                              StringUtil.notNullize(getNodeInterpreterRefName())) ||
           !StringUtil.equals(StringUtil.notNullize(myYeomanGlobalSettings.getYoPackagePath()),
                              StringUtil.notNullize(myYeomanPackageField.getText()));
  }

  @Override
  public void apply() {
    myYeomanGlobalSettings.setNodePath(getNodeInterpreterRefName());
    myYeomanGlobalSettings.setYoPackage(myYeomanPackageField.getText());
  }

  @Nullable
  private String getNodeInterpreterRefName() {
    NodeJsInterpreterRef interpreterRef = myNodeTextField.getInterpreterRef();
    return interpreterRef.isProjectRef() ? null : interpreterRef.getReferenceName();
  }

  @Override
  public void reset() {
    String interpreter = myYeomanGlobalSettings.getNodeInterpreterRefName();
    myNodeTextField.setInterpreterRef(NodeJsInterpreterRef.create(interpreter));
    myYeomanPackageField.setTextAndAddToHistory(StringUtil.notNullize(myYeomanGlobalSettings.getYoPackagePath()));
  }
}
