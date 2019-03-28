package tanvd.grazi;

import com.intellij.openapi.options.*;
import org.jetbrains.annotations.*;

public class GraziToolConfigurable extends ConfigurableBase<GraziSettingsPanel, GraziApplicationSettings> {
    public GraziToolConfigurable() {
        super("reference.settingsdialog.project.grazi", "Grazi", null);
    }

    @NotNull
    @Override
    protected GraziApplicationSettings getSettings() {
        return GraziApplicationSettings.getInstance();
    }

    @Override
    protected GraziSettingsPanel createUi() {
        return new GraziSettingsPanel();
    }
}
