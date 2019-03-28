package tanvd.grazi;

import com.intellij.openapi.components.BaseComponent;

public class GraziSettingsComponent implements BaseComponent {
    @Override
    public void initComponent() {
        GraziApplicationSettings.getInstance().loadLanguages();
    }
}
