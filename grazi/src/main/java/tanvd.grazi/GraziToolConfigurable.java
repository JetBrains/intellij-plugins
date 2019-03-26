package tanvd.grazi;

import com.intellij.openapi.options.*;
import com.intellij.openapi.project.*;
import org.jetbrains.annotations.*;

public class GraziToolConfigurable extends ConfigurableBase<GraziSettingsPanel, GraziToolProjectSettings> {
    private final Project myProject;

    public GraziToolConfigurable(Project project) {
        super("reference.settingsdialog.project.grazi", "Grazi", null);
        myProject = project;
    }

    @NotNull
    @Override
    protected GraziToolProjectSettings getSettings() {
        return GraziToolProjectSettings.getInstance(myProject);
    }

    @Override
    protected GraziSettingsPanel createUi() {
        return new GraziSettingsPanel();
    }
}
