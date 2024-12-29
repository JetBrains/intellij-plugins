package jetbrains.plugins.yeoman.settings;

import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterField;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
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

public final class YeomanConfigurable implements Configurable, Configurable.NoScroll {
  private final YeomanGlobalSettings myYeomanGlobalSettings;
  private NodeJsInterpreterField myNodeTextField;
  private TextFieldWithHistoryWithBrowseButton myYeomanPackageField;

  public YeomanConfigurable() {
    myYeomanGlobalSettings = YeomanGlobalSettings.getInstance();
  }

  @Override
  public @Nls String getDisplayName() {
    return YeomanBundle.message("settings.yeoman.name");
  }

  @Override
  public @Nullable String getHelpTopic() {
    return "reference.settings.yeoman";
  }

  @Override
  public @Nullable JComponent createComponent() {
    var project = ProjectManager.getInstance().getDefaultProject();
    myNodeTextField = new NodeJsInterpreterField(project, false);
    var descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle(YeomanBundle.message("yeoman.configurable.package.dialog.title"));
    myYeomanPackageField = SwingHelper.createTextFieldWithHistoryWithBrowseButton(null, descriptor, null);
    var builder = FormBuilder.createFormBuilder()
      .addLabeledComponent(NodeJsInterpreterField.getLabelTextForComponent(), myNodeTextField)
      .addLabeledComponent(YeomanBundle.message("yeoman.configurable.package.dialog.label"), myYeomanPackageField);
    var panel = builder.getPanel();
    var wrapper = new JPanel(new BorderLayout());
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

  private @Nullable String getNodeInterpreterRefName() {
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
