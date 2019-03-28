package tanvd.grazi;

import com.intellij.openapi.components.*;
import org.jetbrains.annotations.*;
import tanvd.grazi.grammar.*;

import java.io.*;
import java.util.*;

@State(
        name = "GraziApplicationSettings",
        storages = @Storage("grazi.xml")
)
public class GraziApplicationSettings implements PersistentStateComponent<GraziApplicationSettings.State> {
    private State myState = new State();

    public static GraziApplicationSettings getInstance() {
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

    public void loadLanguages() {
        GrammarEngineService.Companion.getInstance().setEnabledLangs(new ArrayList<>(myState.languages));
    }

    public static class State {
        public final Set<String> languages = new HashSet<>();

        {
            languages.add("en");
        }
    }
}
