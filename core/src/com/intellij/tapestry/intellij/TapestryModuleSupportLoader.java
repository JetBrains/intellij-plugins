package com.intellij.tapestry.intellij;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.module.Module;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.intellij.core.java.IntellijJavaTypeCreator;
import com.intellij.tapestry.intellij.core.java.IntellijJavaTypeFinder;
import com.intellij.tapestry.intellij.core.resource.IntellijResourceFinder;
import org.jetbrains.annotations.NotNull;

@State(
        name = "Loomy",
        storages = {
        @Storage(
                id = "Loomy",
                file = "$MODULE_FILE$"
        )}
)
public class TapestryModuleSupportLoader extends AbstractModuleComponent implements PersistentStateComponent<TapestryModuleSupportLoader.ModuleConfiguration> {

    private final TapestryProject _tapestryProject;
    private ModuleConfiguration _configuration;

    public TapestryModuleSupportLoader(Module module) {
        super(module);

        _configuration = new ModuleConfiguration();

        _tapestryProject = new TapestryProject(new IntellijResourceFinder(module), new IntellijJavaTypeFinder(module), new IntellijJavaTypeCreator(module));
    }

    public static TapestryModuleSupportLoader getInstance(Module module) {
        return module.getComponent(TapestryModuleSupportLoader.class);
    }

    /**
     * Finds the Tapestry project instance associated with a module.
     *
     * @param module the module to look for the Tapestry project.
     * @return the Tapestry project instance associated withthe given module.
     */
    public static TapestryProject getTapestryProject(Module module) {
        if (module == null) {
            return null;
        }

        return module.getComponent(TapestryModuleSupportLoader.class).getTapestryProject();
    }

    public TapestryProject getTapestryProject() {
        return _tapestryProject;
    }

    @NotNull
    public String getComponentName() {
        return TapestryModuleSupportLoader.class.getName();
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
