package com.intellij.tapestry.tests.mocks;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.*;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.util.graph.Graph;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModuleManagerMock extends ModuleManager {

    private final List<Module> _modules = new ArrayList<Module>();

    @NotNull
    public Module newModule(@NotNull String filePath) {
        return null;
    }

    @NotNull
    public Module newModule(@NotNull @NonNls String filePath, @NotNull ModuleType moduleType) {
        return null;
    }

    @NotNull
    public Module loadModule(@NotNull String filePath) throws InvalidDataException, IOException, JDOMException, ModuleWithNameAlreadyExists {
        return null;
    }

    public void disposeModule(@NotNull Module module) {
    }

    @NotNull
    public Module[] getModules() {
        return _modules.toArray(new Module[0]);
    }

    public ModuleManagerMock addModules(Module... modules) {
        for (Module module : modules)
            _modules.add(module);

        return this;
    }

    @Nullable
    public Module findModuleByName(@NonNls @NotNull String name) {
        return null;
    }

    @NotNull
    public Module[] getSortedModules() {
        return new Module[0];
    }

    @NotNull
    public Comparator<Module> moduleDependencyComparator() {
        return null;
    }

    @NotNull
    public List<Module> getModuleDependentModules(@NotNull Module module) {
        return null;
    }

    public boolean isModuleDependent(@NotNull Module module, @NotNull Module onModule) {
        return false;
    }

    public void addModuleListener(@NotNull ModuleListener listener) {
    }

    public void addModuleListener(@NotNull ModuleListener listener, Disposable parentDisposable) {
    }

    public void removeModuleListener(@NotNull ModuleListener listener) {
    }

    @NotNull
    public Graph<Module> moduleGraph() {
        return null;
    }

    @NotNull
    public ModifiableModuleModel getModifiableModel() {
        return null;
    }

    public void dispatchPendingEvent(@NotNull ModuleListener listener) {
    }

    @Nullable
    public String[] getModuleGroupPath(@NotNull Module module) {
        return new String[0];
    }
}
