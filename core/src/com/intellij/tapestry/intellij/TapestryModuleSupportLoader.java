package com.intellij.tapestry.intellij;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlElement;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.intellij.core.java.IntellijJavaTypeCreator;
import com.intellij.tapestry.intellij.core.java.IntellijJavaTypeFinder;
import com.intellij.tapestry.intellij.core.resource.IntellijResourceFinder;
import com.intellij.tapestry.lang.TmlFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
  name = "Loomy",
  storages = @Storage(file = StoragePathMacros.MODULE_FILE)
)
public class TapestryModuleSupportLoader
    implements PersistentStateComponent<TapestryModuleSupportLoader.ModuleConfiguration> {

  private final TapestryProject _tapestryProject;
  private ModuleConfiguration _configuration;

  public TapestryModuleSupportLoader(Module module) {

    _configuration = new ModuleConfiguration();
    _tapestryProject = new TapestryProject(module, new IntellijResourceFinder(module), new IntellijJavaTypeFinder(module),
                                           new IntellijJavaTypeCreator(module));
  }

  public static TapestryModuleSupportLoader getInstance(Module module) {
    return ModuleServiceManager.getService(module, TapestryModuleSupportLoader.class);
  }

  @Nullable
  public static TapestryModuleSupportLoader getInstance(@NotNull PsiElement element) {
    final PsiFile file = element.getContainingFile();
    if (file == null || !(file.getFileType() instanceof TmlFileType)) return null;
    Module module = ModuleUtil.findModuleForPsiElement(element);
    return module != null ? getInstance(module) : null;
  }

  /**
   * Finds the Tapestry project instance associated with a module.
   *
   * @param module the module to look for the Tapestry project.
   * @return the Tapestry project instance associated withthe given module.
   */
  @Nullable
  public static TapestryProject getTapestryProject(@Nullable Module module) {
    return module == null ? null : getInstance(module).getTapestryProject();
  }

  @Nullable
  public static TapestryProject getTapestryProject(@NotNull XmlElement element) {
    TapestryModuleSupportLoader instance = getInstance(element);
    return instance == null ? null : instance.getTapestryProject();
  }

  public TapestryProject getTapestryProject() {
    return _tapestryProject;
  }

  /**
   * {@inheritDoc}
   */
  public ModuleConfiguration getState() {
    return _configuration;
  }

  /**
   * {@inheritDoc}
   */
  public void loadState(ModuleConfiguration state) {
    _configuration = state;
  }

  public static class ModuleConfiguration {

    private String _newPagesTemplatesSourceDirectory;
    private String _newPagesClassesSourceDirectory;
    private String _newComponentsTemplatesSourceDirectory;
    private String _newComponentsClassesSourceDirectory;
    private String _newMixinsClassesSourceDirectory;

    public String getNewPagesTemplatesSourceDirectory() {
      return _newPagesTemplatesSourceDirectory;
    }

    public void setNewPagesTemplatesSourceDirectory(String newPagesTemplatesSourceDirectory) {
      _newPagesTemplatesSourceDirectory = newPagesTemplatesSourceDirectory;
    }

    public String getNewPagesClassesSourceDirectory() {
      return _newPagesClassesSourceDirectory;
    }

    public void setNewPagesClassesSourceDirectory(String newPagesClassesSourceDirectory) {
      _newPagesClassesSourceDirectory = newPagesClassesSourceDirectory;
    }

    public String getNewComponentsTemplatesSourceDirectory() {
      return _newComponentsTemplatesSourceDirectory;
    }

    public void setNewComponentsTemplatesSourceDirectory(String newComponentsTemplatesSourceDirectory) {
      _newComponentsTemplatesSourceDirectory = newComponentsTemplatesSourceDirectory;
    }

    public String getNewComponentsClassesSourceDirectory() {
      return _newComponentsClassesSourceDirectory;
    }

    public void setNewComponentsClassesSourceDirectory(String newComponentsClassesSourceDirectory) {
      _newComponentsClassesSourceDirectory = newComponentsClassesSourceDirectory;
    }

    public String getNewMixinsClassesSourceDirectory() {
      return _newMixinsClassesSourceDirectory;
    }

    public void setNewMixinsClassesSourceDirectory(String newMixinsClassesSourceDirectory) {
      _newMixinsClassesSourceDirectory = newMixinsClassesSourceDirectory;
    }
  }
}
