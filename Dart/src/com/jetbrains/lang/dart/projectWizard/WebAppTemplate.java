package com.jetbrains.lang.dart.projectWizard;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

class WebAppTemplate extends DartProjectTemplate {
  WebAppTemplate() {
    super(DartBundle.message("dart.web.app.title"), "");
  }

  @Override
  public Collection<VirtualFile> generateProject(@NotNull final String sdkRoot,
                                                 @NotNull final Module module,
                                                 @NotNull final VirtualFile baseDir) throws IOException {
    final String projectTitle = StringUtil.toTitleCase(module.getName());
    final String lowercaseName = module.getName().toLowerCase(Locale.US);

    final VirtualFile pubspecFile = baseDir.createChildData(this, PubspecYamlUtil.PUBSPEC_YAML);
    pubspecFile.setBinaryContent(("name: " + module.getName() + "\n" +
                                  "version: 0.0.1\n" +
                                  "description: A sample web application\n" +
                                  "dependencies:\n" +
                                  "  browser: any\n" +
                                  "dev_dependencies:\n" +
                                  "#  unittest: any\n").getBytes(Charset.forName("UTF-8")));

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
                               "}\n").getBytes(Charset.forName("UTF-8")));

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
                               "    <script type=\"" + DartLanguage.DART_MIME_TYPE + "\" src=\"" + lowercaseName + ".dart\"></script>\n" +
                               "    <script src=\"packages/browser/dart.js\"></script>\n" +
                               "  </body>\n" +
                               "</html>\n").getBytes(Charset.forName("UTF-8")));

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
                              "}\n").getBytes(Charset.forName("UTF-8")));

    createWebRunConfiguration(module, htmlFile);

    return Arrays.asList(pubspecFile, htmlFile, cssFile, dartFile);
  }
}
