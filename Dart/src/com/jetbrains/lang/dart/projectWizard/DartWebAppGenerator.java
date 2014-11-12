package com.jetbrains.lang.dart.projectWizard;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.impl.WebBrowserServiceImpl;
import com.intellij.javascript.debugger.execution.JavaScriptDebugConfiguration;
import com.intellij.javascript.debugger.execution.JavascriptDebugConfigurationType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.Url;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.client.DartiumUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Locale;

public class DartWebAppGenerator extends DartEmptyProjectGenerator {

  @SuppressWarnings("UnusedDeclaration") // invoked by com.intellij.platform.DirectoryProjectGenerator.EP_NAME
  public DartWebAppGenerator() {
    this(DartBundle.message("dart.web.app.description.webstorm"));
  }

  public DartWebAppGenerator(final @NotNull String description) {
    super(DartBundle.message("dart.web.app.title"), description);
  }

  @NotNull
  protected VirtualFile[] doGenerateProject(@NotNull final Module module, final VirtualFile baseDir) throws IOException {
    final String projectTitle = StringUtil.toTitleCase(module.getName());
    final String lowercaseName = module.getName().toLowerCase(Locale.US);

    final VirtualFile pubspecFile = baseDir.createChildData(this, PubspecYamlUtil.PUBSPEC_YAML);
    pubspecFile.setBinaryContent(("name: " + module.getName() + "\n" +
                                  "version: 0.0.1\n" +
                                  "description: A sample web application\n" +
                                  "dependencies:\n" +
                                  "  browser: any\n" +
                                  "dev_dependencies:\n" +
                                  "#  unittest: any\n").getBytes());

    final VirtualFile webDir = VfsUtil.createDirectoryIfMissing(baseDir, "web");

    final VirtualFile dartFile = webDir.createChildData(this, lowercaseName + ".dart");
    dartFile.setBinaryContent(("import 'dart:html';\n\n" +
                               "void main() {\n" +
                               "  querySelector('#sample_text_id')\n" +
                               "    ..text = 'Click me!'\n" +
                               "    ..onClick.listen(reverseText);\n" +
                               "}\n\n" +
                               "void reverseText(MouseEvent event) {\n" +
                               "  var text = querySelector('#sample_text_id').text;\n" +
                               "  var buffer = new StringBuffer();\n" +
                               "  for (int i = text.length - 1; i >= 0; i--) {\n" +
                               "    buffer.write(text[i]);\n" +
                               "  }\n" +
                               "  querySelector('#sample_text_id').text = buffer.toString();\n" +
                               "}\n").getBytes());

    final VirtualFile htmlFile = webDir.createChildData(this, lowercaseName + ".html");
    htmlFile.setBinaryContent(("<!DOCTYPE html>\n\n" +
                               "<html>\n" +
                               "  <head>\n" +
                               "    <meta charset=\"utf-8\">\n" +
                               "    <title>" + projectTitle + "</title>\n" +
                               "    <link rel=\"stylesheet\" href=\"" + lowercaseName + ".css\">\n" +
                               "  </head>\n" +
                               "  <body>\n" +
                               "    <h1>" + projectTitle + "</h1>\n\n" +
                               "    <p>Hello world from Dart!</p>\n\n" +
                               "    <div id=\"sample_container_id\">\n" +
                               "      <p id=\"sample_text_id\">Click me!</p>\n" +
                               "    </div>\n\n" +
                               "    <script type=\"application/dart\" src=\"" + lowercaseName + ".dart\"></script>\n" +
                               "    <script src=\"packages/browser/dart.js\"></script>\n" +
                               "  </body>\n" +
                               "</html>\n").getBytes());

    final VirtualFile cssFile = webDir.createChildData(this, lowercaseName + ".css");
    cssFile.setBinaryContent(("body {\n" +
                              "  background-color: #F8F8F8;\n" +
                              "  font-family: 'Open Sans', sans-serif;\n" +
                              "  font-size: 14px;\n" +
                              "  font-weight: normal;\n" +
                              "  line-height: 1.2em;\n" +
                              "  margin: 15px;\n" +
                              "}\n\n" +
                              "h1, p {\n" +
                              "  color: #333;\n" +
                              "}\n\n" +
                              "#sample_container_id {\n" +
                              "  width: 100%;\n" +
                              "  height: 400px;\n" +
                              "  position: relative;\n" +
                              "  border: 1px solid #ccc;\n" +
                              "  background-color: #fff;\n" +
                              "}\n\n" +
                              "#sample_text_id {\n" +
                              "  font-size: 24pt;\n" +
                              "  text-align: center;\n" +
                              "  margin-top: 140px;\n" +
                              "  -webkit-user-select: none;\n" +
                              "  user-select: none;\n" +
                              "}\n").getBytes());

    createRunConfiguration(module, htmlFile);

    return new VirtualFile[]{pubspecFile, htmlFile, cssFile, dartFile};
  }

  private static void createRunConfiguration(final @NotNull Module module, final @NotNull VirtualFile htmlFile) {
    StartupManager.getInstance(module.getProject()).runWhenProjectIsInitialized(new Runnable() {
      public void run() {
        final WebBrowser dartium = DartiumUtil.getDartiumBrowser();
        if (dartium == null) return;

        final PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(htmlFile);
        final Url url = psiFile == null ? null : WebBrowserServiceImpl.getUrlForContext(psiFile);
        if (url == null) return;

        final RunManager runManager = RunManager.getInstance(module.getProject());
        try {
          final RunnerAndConfigurationSettings settings =
            runManager.createRunConfiguration("", JavascriptDebugConfigurationType.getTypeInstance().getFactory());

          ((JavaScriptDebugConfiguration)settings.getConfiguration()).setUri(url.toDecodedForm());
          ((JavaScriptDebugConfiguration)settings.getConfiguration()).setEngineId(dartium.getId().toString());
          settings.setName(((JavaScriptDebugConfiguration)settings.getConfiguration()).suggestedName());

          runManager.addConfiguration(settings, false);
          runManager.setSelectedConfiguration(settings);
        }
        catch (Throwable t) {/* ClassNotFound in IDEA Community or if JS Debugger plugin disabled */}
      }
    });
  }
}
