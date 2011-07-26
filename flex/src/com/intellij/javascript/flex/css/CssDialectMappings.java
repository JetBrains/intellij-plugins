package com.intellij.javascript.flex.css;

import com.intellij.lang.LanguagePerFileMappings;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;

import java.util.Arrays;
import java.util.List;

/**
 * @author Eugene.Kudelevsky
 */
@State(
    name = "CssDialectMappings",
    storages = {
        @Storage( file = "$PROJECT_FILE$"),
        @Storage( file = "$PROJECT_CONFIG_DIR$/cssdialects.xml", scheme = StorageScheme.DIRECTORY_BASED)
})
public class CssDialectMappings extends LanguagePerFileMappings<CssDialect> {
  public static CssDialectMappings getInstance(final Project project) {
    return ServiceManager.getService(project, CssDialectMappings.class);
  }

  public CssDialectMappings(final Project project) {
    super(project);
  }

  protected String serialize(final CssDialect dialect) {
    return dialect.name();
  }

  public List<CssDialect> getAvailableValues() {
    return Arrays.asList(CssDialect.values());
  }
}
