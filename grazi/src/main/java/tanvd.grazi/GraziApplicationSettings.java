package tanvd.grazi;

import com.intellij.openapi.components.*;
import org.jetbrains.annotations.*;
import tanvd.grazi.grammar.*;
import tanvd.grazi.language.*;
import tanvd.grazi.spellcheck.*;

import java.io.*;
import java.util.*;

@State(name = "GraziApplicationSettings", storages = @Storage("grazi.xml"))
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

        init();
    }

    void init() {
        LangChecker.INSTANCE.clear();
        SpellDictionary.Companion.setGraziFolder(myState.graziFolder);
        GrammarEngine.Companion.getInstance().setEnabledLangs(new ArrayList<>(myState.languages));
    }

    public static class State {
        final Set<String> languages = new HashSet<>();
        public File graziFolder = new File(System.getProperty("user.home"), ".grazi");

        {
            languages.add("en");
        }
    }
}
