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

    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
        loadLanguages();
    }

    public String getGraziHome() {
        return myState.graziHome;
    }

    public void loadLanguages() {
        GrammarEngineService.Companion.getInstance().setEnabledLangs(new ArrayList<>(myState.languages));
    }

    public void setGraziHome(String graziHome) {
        myState.graziHome = graziHome;
    }

    public static class State {
        public String graziHome = getDefaultGraziHome();
        public final Set<String> languages = new HashSet<>();

        {
            languages.add("en");
        }
    }

    public static String getDefaultGraziHome() {
        File directory = new File(System.getProperty("user.home"), ".grazi");
        if (!directory.exists()) {
            directory.mkdir();
        }

        return directory.getAbsolutePath();
    }
}
