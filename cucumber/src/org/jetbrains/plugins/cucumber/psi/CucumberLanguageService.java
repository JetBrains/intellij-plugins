package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.plugins.cucumber.psi.i18n.JsonGherkinKeywordProvider;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author yole
 */
public class CucumberLanguageService {
  private GherkinKeywordProvider myKeywordProvider;

  public static CucumberLanguageService getInstance(Project project) {
    return ServiceManager.getService(project, CucumberLanguageService.class);
  }

  @SuppressWarnings("UnusedParameters")
  public CucumberLanguageService(Project project) {
  }

  public GherkinKeywordProvider getKeywordProvider() {
    if (myKeywordProvider == null) {
      final ClassLoader classLoader = CucumberLanguageService.class.getClassLoader();
      if (classLoader != null) {
        @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
        final InputStream inputStream = classLoader.getResourceAsStream("i18n.json");
        if (inputStream != null) {
          myKeywordProvider = new JsonGherkinKeywordProvider(inputStream);
        }
      }

      if (myKeywordProvider == null) {
        myKeywordProvider = new PlainGherkinKeywordProvider();
      }
    }
    return myKeywordProvider;
  }
}
