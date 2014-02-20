package com.jetbrains.lang.dart.util;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;

import java.io.File;
import java.io.IOException;

public class DartHtmlUtil {
  public static PsiFile createHtmlAndConfigureFixture(final CodeInsightTestFixture fixture, String... fileNames) throws IOException {
    if (fileNames.length > 1) {
      for (int i = 1; i < fileNames.length; i++) {
        fixture.copyFileToProject(fileNames[i]);
      }
    }

    final String content = FileUtil.loadFile(new File(fixture.getTestDataPath() + "/" + fileNames[0]));
    return fixture.configureByText(fileNames[0] + ".html", DartHtmlUtil.getHtmlText(content));
  }

  private static String getHtmlText(String text) {
    return "<html>\n" +
           "  <body>\n" +
           "    <script type=\"application/dart\">\n" +
           text +
           "    </script>\n" +
           "  </body>\n" +
           "</html>";
  }
}
