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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

class WebAppTemplate extends DartProjectTemplate {
  WebAppTemplate() {
    super(DartBundle.message("dart.web.app.title"), "");
  }

  @Override
  public Collection<VirtualFile> generateProject(@NotNull final String sdkRoot,
                                                 @NotNull final Module module,
                                                 @NotNull final VirtualFile baseDir) throws IOException {
    final String projectTitle = StringUtil.toTitleCase(module.getName());
    final String lowercaseName = StringUtil.toLowerCase(module.getName());

    final VirtualFile pubspecFile = baseDir.createChildData(this, PubspecYamlUtil.PUBSPEC_YAML);
    pubspecFile.setBinaryContent(("name: " + module.getName() + "\n" +
                                  "version: 0.0.1\n" +
                                  "description: A sample web application\n" +
                                  "dependencies:\n" +
                                  "  browser: any\n" +
                                  "dev_dependencies:\n" +
                                  "#  unittest: any\n").getBytes(StandardCharsets.UTF_8));

    final VirtualFile webDir = VfsUtil.createDirectoryIfMissing(baseDir, "web");

    final VirtualFile dartFile = webDir.createChildData(this, lowercaseName + ".dart");
    dartFile.setBinaryContent(("""
                                 import 'dart:html';

                                 void main() {
                                   querySelector('#sample_text_id')
                                     ..text = 'Click me!'
                                     ..onClick.listen(reverseText);
                                 }

                                 void reverseText(MouseEvent event) {
                                   var text = querySelector('#sample_text_id').text;
                                   var buffer = new StringBuffer();
                                   for (int i = text.length - 1; i >= 0; i--) {
                                     buffer.write(text[i]);
                                   }
                                   querySelector('#sample_text_id').text = buffer.toString();
                                 }
                                 """).getBytes(StandardCharsets.UTF_8));

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
                               "</html>\n").getBytes(StandardCharsets.UTF_8));

    final VirtualFile cssFile = webDir.createChildData(this, lowercaseName + ".css");
    cssFile.setBinaryContent(("""
                                body {
                                  background-color: #F8F8F8;
                                  font-family: 'Open Sans', sans-serif;
                                  font-size: 14px;
                                  font-weight: normal;
                                  line-height: 1.2em;
                                  margin: 15px;
                                }

                                h1, p {
                                  color: #333;
                                }

                                #sample_container_id {
                                  width: 100%;
                                  height: 400px;
                                  position: relative;
                                  border: 1px solid #ccc;
                                  background-color: #fff;
                                }

                                #sample_text_id {
                                  font-size: 24pt;
                                  text-align: center;
                                  margin-top: 140px;
                                  -webkit-user-select: none;
                                  user-select: none;
                                }
                                """).getBytes(StandardCharsets.UTF_8));

    createWebRunConfiguration(module, htmlFile);

    return Arrays.asList(pubspecFile, htmlFile, cssFile, dartFile);
  }
}
