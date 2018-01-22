package org.intellij.errorProne;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.compiler.JpsJavaCompilerOptions;

/**
 * @author nik
 */
@State(name = "ErrorProneCompilerSettings", storages = @Storage("compiler.xml"))
public class ErrorProneCompilerConfiguration implements PersistentStateComponent<JpsJavaCompilerOptions> {
  private final JpsJavaCompilerOptions mySettings = new JpsJavaCompilerOptions();

  @NotNull
  public JpsJavaCompilerOptions getState() {
    return mySettings;
  }

  public void loadState(@NotNull JpsJavaCompilerOptions state) {
    XmlSerializerUtil.copyBean(state, mySettings);
  }

  public static JpsJavaCompilerOptions getOptions(Project project) {
    return ServiceManager.getService(project, ErrorProneCompilerConfiguration.class).getState();
  }}
