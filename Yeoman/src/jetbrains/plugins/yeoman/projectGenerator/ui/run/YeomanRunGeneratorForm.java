package jetbrains.plugins.yeoman.projectGenerator.ui.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.javascript.nodejs.NodePackageVersion;
import com.intellij.javascript.nodejs.NodePackageVersionUtil;
import com.intellij.javascript.nodejs.interpreter.local.NodeJsLocalInterpreter;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DefaultProjectFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.HideableDecorator;
import com.intellij.ui.RelativeFont;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.util.text.SemVer;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.StartupUiUtil;
import com.intellij.util.ui.UIUtil;
import jetbrains.plugins.yeoman.YeomanBundle;
import jetbrains.plugins.yeoman.generators.YeomanInstalledGeneratorInfo;
import jetbrains.plugins.yeoman.generators.YeomanInstalledGeneratorListProvider;
import jetbrains.plugins.yeoman.projectGenerator.ui.run.controls.YeomanGeneratorControl;
import jetbrains.plugins.yeoman.settings.YeomanGlobalSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class YeomanRunGeneratorForm implements Disposable {
  public static final Logger LOGGER = Logger.getInstance(YeomanRunGeneratorForm.class);

  private final Project myProject;
  private final YeomanGlobalSettings mySettings;
  private final AsyncProcessIcon myAsyncProcessIcon;
  private JTextField myGeneratorOptions;

  public enum EventTypes {
    RENDERED,
    TERMINATED_OK,
    TERMINATED_ERROR,
    STARTING_ERROR
  }

  public interface EventHandler {
    void handleEvent(EventTypes event);
  }


  @Nullable
  private YeomanInstalledGeneratorInfo myInfo;
  private final String myDirectoryForCreate;
  private final EventHandler myGlobalEventHandler;
  private JScrollPane myScrollPane;
  private JPanel myMainPanel;
  private JPanel myFormContainerPanel;
  private JLabel myGeneratorTitle;
  private JPanel myConsolePanel;
  private JPanel myTopRunningLabelPanel;
  private KillableColoredProcessHandler myProcessHandler;
  private final YeomanStepFormRenderer myRenderer;
  private YeomanGeneratorControl.YeomanGeneratorControlUI myCurrentControl;
  private OutputStream myInput;

  private int mySteps = 0;
  private ConsoleViewImpl myConsoleView;
  private JTextField myGeneratorNameField;


  public YeomanRunGeneratorForm(String directoryForCreate,
                                @Nullable Project project,
                                YeomanGlobalSettings settings,
                                @Nullable YeomanInstalledGeneratorInfo info,
                                EventHandler handler,
                                @Nullable String options) {
    myDirectoryForCreate = directoryForCreate;
    myGlobalEventHandler = handler;
    myConsolePanel.setVisible(false);
    GuiUtils.replaceJSplitPaneWithIDEASplitter(myMainPanel);
    myProject = project == null ? DefaultProjectFactory.getInstance().getDefaultProject() : project;
    mySettings = settings;
    myInfo = info;
    myRenderer = new YeomanStepFormRenderer();


    myAsyncProcessIcon = new AsyncProcessIcon("");
    myTopRunningLabelPanel.add(myAsyncProcessIcon);

    renderWelcomeForm();
    myGeneratorOptions.setText(options);
    if (!hasWelcome()) {
      next();
    }
  }

  protected boolean hasWelcome() {
    return false;
  }

  public void renderWelcomeForm() {
    FormBuilder builder = FormBuilder.createFormBuilder();
    myGeneratorNameField = createGeneratorNameField();
    builder.addLabeledComponent(YeomanBundle.message("label.text.generator.name"), myGeneratorNameField);
    myGeneratorOptions = new JTextField();
    builder.addLabeledComponent(YeomanBundle.message("label.text.generator.options"), myGeneratorOptions);
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(builder.getPanel(), BorderLayout.NORTH);
    myScrollPane = ScrollPaneFactory.createScrollPane(wrapper, true);
    myScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    myFormContainerPanel.add(myScrollPane, BorderLayout.CENTER);


    myGeneratorTitle.setFont(StartupUiUtil.getLabelFont());
    RelativeFont.ITALIC.install(myGeneratorTitle);

    myGeneratorTitle.setText(YeomanBundle.message("label.text.generator.settings"));
  }

  public void startGeneratorApp() {
    final GeneralCommandLine commandLine = createCommandLine();
    try {
      myProcessHandler = new KillableColoredProcessHandler(commandLine);
      myProcessHandler.addProcessListener(new ProcessAdapter() {
        @Override
        public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
          LOGGER.debug(event.getText());
          if (outputType == ProcessOutputTypes.STDOUT) {
            final String text = StringUtil.notNullize(event.getText()).trim();
            if (text.contains("{") && text.endsWith("}") && !text.startsWith("--debug")) {
              renderStepForm(text.substring(text.indexOf("{")));
            }
          }
        }

        @Override
        public void processTerminated(@NotNull final ProcessEvent event) {
          UIUtil.invokeLaterIfNeeded(() -> {
            done();

            myGlobalEventHandler.handleEvent(event.getExitCode() == 0 ?
                                             EventTypes.TERMINATED_OK :
                                             EventTypes.TERMINATED_ERROR);
          });
        }
      });
      myProcessHandler.startNotify();
      myInput = myProcessHandler.getProcessInput();
      myConsoleView = new ConsoleViewImpl(myProject, true) {
        @Override
        public void print(@NotNull String s, @NotNull ConsoleViewContentType contentType) {
          if (contentType == ConsoleViewContentType.ERROR_OUTPUT || LOGGER.isDebugEnabled()) {
            super.print(s, contentType);
          }
        }
      };
      myConsoleView.attachToProcess(myProcessHandler);
      myConsoleView.setPreferredSize(new Dimension(550, 150));
    }
    catch (ExecutionException e) {
      myGlobalEventHandler.handleEvent(EventTypes.STARTING_ERROR);
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public void renderStepForm(final String text) {
    UIUtil.invokeLaterIfNeeded(() -> {
      ApplicationManager.getApplication().assertIsDispatchThread();
      myCurrentControl = myRenderer.render(text);
      assert myInfo != null;

      myGeneratorTitle.setText(YeomanBundle.message("label.text.running.0.generator.step.1", myInfo.getYoName(), ++mySteps));

      final JComponent component = myCurrentControl.getComponent();
      final JPanel wrapper = new JPanel(new BorderLayout());
      wrapper.add(component, BorderLayout.NORTH);
      UIUtil.setEnabled(myScrollPane, true, true);
      myScrollPane.setViewportView(wrapper);

      myAsyncProcessIcon.setVisible(false);
      myGlobalEventHandler.handleEvent(EventTypes.RENDERED);
    });
  }

  private void showConsole() {
    final JComponent consoleComponent = myConsoleView.getComponent();

    final HideableDecorator decorator = new HideableDecorator(myConsolePanel, YeomanBundle.message("separator.yeoman.console.output"), false);
    decorator.setContentComponent(consoleComponent);
    decorator.setOn(false);
    myConsolePanel.setVisible(true);
  }

  private GeneralCommandLine createCommandLine() {
    NodeJsLocalInterpreter interpreter = mySettings.getInterpreter();
    assert interpreter != null;

    final GeneralCommandLine line = new GeneralCommandLine(interpreter.getInterpreterSystemIndependentPath());
    assert myInfo != null;

    line.withWorkDirectory(myDirectoryForCreate);
    line.setCharset(StandardCharsets.UTF_8);
    line.addParameter(mySettings.getCLIHelperPath());
    line.addParameter("--generatorName");
    line.addParameter(myInfo.getYoName());
    line.addParameter("--generatorPath");
    line.addParameter(myInfo.getFilePath());
    line.addParameter("--yeoman");
    line.addParameter(mySettings.getYoPackagePath());

    final String options = myGeneratorOptions.getText();
    if (!StringUtil.isEmpty(options)) {
      line.addParameter("--arguments");
      line.addParameter(options);
    }

    addNewYoVersionParameter(line);

    if (LOGGER.isDebugEnabled()) {
      line.addParameter("--debug");
    }


    return line;
  }

  private void addNewYoVersionParameter(@NotNull GeneralCommandLine line) {
    assert myInfo != null;
    NodePackageVersion version = NodePackageVersionUtil.getPackageVersion(myInfo.getFilePath());

    SemVer ver = version == null ? null : version.getSemVer();
    if (ver == null || ver.isGreaterOrEqualThan(1, 7, 1)) {
      line.addParameter("--newYoVersion");
    }
  }

  @NotNull
  private JTextField createGeneratorNameField() {
    final JTextField field = new JTextField();
    if (myInfo != null) {
      field.setText(myInfo.getYoName());
      field.setEnabled(false);
    }
    return field;
  }

  @NotNull
  public JScrollPane getHolderPanel() {
    return myScrollPane;
  }

  public void next() {
    ApplicationManager.getApplication().assertIsDispatchThread();
    myAsyncProcessIcon.setVisible(true);
    if (myCurrentControl != null) {
      send(myCurrentControl.getSelectedValue());
      UIUtil.setEnabled(myScrollPane, false, true);
    }

    if (myProcessHandler == null) {
      myInfo = new YeomanInstalledGeneratorListProvider().getGeneratorByYoName(myGeneratorNameField.getText());
      if (myInfo != null) {
        myGeneratorTitle.setText(YeomanBundle.message("label.text.running.0.generator", myInfo.getYoName()));
        startGeneratorApp();
        UIUtil.setEnabled(myScrollPane, true, true);
        myScrollPane.setViewportView(new JPanel(new BorderLayout()));
        showConsole();
      }
    }
  }

  private void done() {
    ApplicationManager.getApplication().assertIsDispatchThread();
    myGeneratorTitle.setText(YeomanBundle.message("label.text.complete.0.generator", myInfo.getYoName()));
    myAsyncProcessIcon.setVisible(false);
    myScrollPane.setViewportView(new JPanel(new BorderLayout()));
  }

  @NotNull
  public JComponent getMainPanel() {
    return myMainPanel;
  }

  @Override
  public void dispose() {
    if (myProcessHandler != null) {
      if (!myProcessHandler.isProcessTerminated() && !myProcessHandler.isProcessTerminating()) {
        LOGGER.debug("Dispose process " + myProcessHandler.getCommandLine());

        myProcessHandler.destroyProcess();
        //process doesn't completed -> should remove folder
        try {
          final File file = new File(myDirectoryForCreate);
          if (file.exists()) {
            FileUtil.delete(file);
          }
        }
        catch (Exception e) {
          LOGGER.warn(e.getMessage(), e);
        }

        if (SystemInfo.isWindows) {
          myProcessHandler.killProcess();
        }
      }
    }

    if (myConsoleView != null) {
      Disposer.dispose(myConsoleView);
    }
  }

  private void send(String message) {
    if (myInput != null) {
      try {
        if (!message.endsWith("\n")) {
          message += "\n";
        }
        myInput.write(message.getBytes(StandardCharsets.UTF_8));
        myInput.flush();
      }
      catch (IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
  }
}
