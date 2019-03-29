package tanvd.grazi;

import com.intellij.openapi.components.*;
import org.jetbrains.annotations.*;
import tanvd.grazi.grammar.*;

import java.util.*;

@State(name = "GraziApplicationSettings", storages = @Storage("grazi.xml"))
public class GraziApplicationSettings implements PersistentStateComponent<GraziApplicationSettings.State> {
    private State myState = new State();

    static GraziApplicationSettings getInstance() {
        return ServiceManager.getService(GraziApplicationSettings.class);
    }

    @NotNull
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
        loadLanguages();
    }

    void loadLanguages() {
        GrammarEngineService.Companion.getInstance().setEnabledLangs(new ArrayList<>(myState.languages));
    }

    static class State {
        final Set<String> languages = new HashSet<>();

        {
            languages.add("en");
        }
    }
}
