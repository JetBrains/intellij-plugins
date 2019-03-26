/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tanvd.grazi;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.Language;
import tanvd.grazi.model.GrammarEngine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@State(
        name = "GraziToolProjectSettings",
        storages = @Storage("grazi.xml")
)
public class GraziToolProjectSettings implements PersistentStateComponent<GraziToolProjectSettings.State> {
    private State myState = new State();
    private Set<String> loadingLanguages = new HashSet<>();

    public static GraziToolProjectSettings getInstance(@NotNull final Project project) {
        return ServiceManager.getService(project, GraziToolProjectSettings.class);
    }

    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public String getGraziHome() {
        return myState.graziHome;
    }

    public boolean getUaEnabled() {
        return myState.languages.contains("uk");
    }

    public void setUaEnabled(boolean uaEnabled) {
        if (uaEnabled) {
            myState.languages.add("uk");
        } else
            myState.languages.remove("uk");
    }

    public void loadLanguages() {
        final ArrayList<String> list = new ArrayList<>();
        if (!myState.languages.contains("uk"))
            list.add("uk");
        GrammarEngine.INSTANCE.setNoopLangs(list);
    }

    public void setGraziHome(String graziHome) {
        myState.graziHome = graziHome;
    }

    public static class State {
        public String graziHome = getDefaultGraziHome();
        public final Set<String> languages = new HashSet<>();
    }

    public static String getDefaultGraziHome() {
        File directory = new File(System.getProperty("user.home"), ".grazi");
        if (!directory.exists())
            directory.mkdir();

        return directory.getAbsolutePath();
    }

}
