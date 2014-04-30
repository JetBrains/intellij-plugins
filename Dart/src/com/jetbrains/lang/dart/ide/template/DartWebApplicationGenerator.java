package com.jetbrains.lang.dart.ide.template;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.module.DartProjectTemplate;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DartWebApplicationGenerator extends BaseDartApplicationGenerator {

  @Override
  @NotNull
  protected DartProjectTemplate<?> getGenerator() { return this; }

  @Override
  protected void createContents(final VirtualFile baseDir, final Module module, Project project) throws IOException {
    final VirtualFile webDir = baseDir.createChildDirectory(this, "web");
    final String moduleName = module.getName().toLowerCase();
    final String moduleTitle = toTitleCase(moduleName);
    final VirtualFile main = webDir.createChildData(DartWebApplicationGenerator.this, moduleName + ".dart");
    main.setBinaryContent(("import 'dart:html';\n\n" +
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
                          "};\n").getBytes());
    final VirtualFile html = webDir.createChildData(DartWebApplicationGenerator.this, moduleName + ".html");
    html.setBinaryContent(("<!DOCTYPE html>\n\n" +
                           "<html>\n" +
                           "  <head>\n" +
                           "    <meta charset=\"utf-8\">\n" +
                           "    <title>" + moduleTitle + "</title>\n" +
                           "    <link rel=\"stylesheet\" href=\"" + moduleName + ".css\">\n" +
                           "  </head>\n" +
                           "  <body>\n" +
                           "    <h1>" + moduleTitle + "</h1>\n\n" +
                           "    <p>Hello world from Dart!</p>\n\n" +
                           "    <div id=\"sample_container_id\">\n" +
                           "      <p id=\"sample_text_id\">Click me!</p>\n" +
                           "    </div>\n\n" +
                           "    <script type=\"application/dart\" src=\"" + moduleName + ".dart\"></script>\n" +
                           "    <script src=\"packages/browser/dart.js\"></script>\n" +
                           "  </body>\n" +
                           "</html>\n").getBytes());
    final VirtualFile css = webDir.createChildData(DartWebApplicationGenerator.this, moduleName + ".css");
    css.setBinaryContent(("body {\n" +
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

    openFile(html, project);
  }

  @Override
  protected void setPubspecContent(final VirtualFile pubspecYamlFile, final Module module) throws IOException {
    pubspecYamlFile.setBinaryContent(("name: " + module.getName() + "\n" +
                                      "dependencies:\n" +
                                      "  browser: any").getBytes());
  }

  @Override
  @NotNull
  public String getName() {
    return DartBundle.message("dart.web.application.title");
  }

  @Override
  @NotNull
  public String getDescription() {
    return DartBundle.message("dart.web.application.description");
  }

}
