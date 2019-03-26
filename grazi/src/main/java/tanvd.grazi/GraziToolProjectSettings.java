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

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@State(
        name = "GraziToolProjectSettings",
        storages = @Storage("grazi.xml")
)
public class GraziToolProjectSettings implements PersistentStateComponent<GraziToolProjectSettings.State> {
    private State myState = new State();

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
        myState.graziHome = state.graziHome;
        myState.uaEnabled = state.uaEnabled;
    }

    public String getGraziHome() {
        return myState.graziHome;
    }

    public void setGraziHome(String graziHome) {
        myState.graziHome = graziHome;
    }

    public static class State {
        public String graziHome = getDefaultGraziHome();
        public boolean uaEnabled = false;
    }

    public static String getDefaultGraziHome() {
        File directory = new File(System.getProperty("user.home"), ".grazi");
        if (!directory.exists())
            directory.mkdir();

        return directory.getAbsolutePath();
    }

}
