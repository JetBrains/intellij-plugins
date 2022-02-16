package com.intellij.protobuf.lang.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AdditionalLibrariesFileResolveProvider implements FileResolveProvider {
    @NotNull
    @Override
    public Collection<ChildEntry> getChildEntries(@NotNull String path, @NotNull Project project) {
        VirtualFile pathFile = findFile(path, project);
        if (pathFile == null || !pathFile.isDirectory()) {
            return Collections.emptyList();
        }
        VirtualFile[] children = pathFile.getChildren();
        if (children == null) {
            return Collections.emptyList();
        }

        Set<ChildEntry> results = new HashSet<>();

        for (VirtualFile child : children) {
            if (PROTO_AND_DIRECTORY_FILTER.accept(child)) {
                results.add(new ChildEntry(child.getName(), child.isDirectory()));
            }
        }
        return results;
    }

    @Nullable
    @Override
    public VirtualFile findFile(@NotNull String path, @NotNull Project project) {
        for (AdditionalLibraryRootsProvider provider : AdditionalLibraryRootsProvider.EP_NAME.getExtensionList()) {
            for (SyntheticLibrary library : provider.getAdditionalProjectLibraries(project)) {
                for (VirtualFile sourceRoot : library.getSourceRoots()) {
                    if (sourceRoot.isDirectory()) {
                        VirtualFile pbFile = sourceRoot.findFileByRelativePath(path);
                        if (pbFile != null) {
                            return pbFile;
                        }
                    }
                    if (sourceRoot.getPath().endsWith(path)) {
                        return sourceRoot;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public VirtualFile getDescriptorFile(@NotNull Project project) {
        return null;
    }

    @NotNull
    @Override
    public GlobalSearchScope getSearchScope(@NotNull Project project) {
        VirtualFile[] roots = AdditionalLibraryRootsProvider.EP_NAME.getExtensionList().stream()
                .map(provider -> provider.getAdditionalProjectLibraries(project))
                .flatMap(Collection::stream)
                .map(SyntheticLibrary::getSourceRoots)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .toArray(VirtualFile[]::new);
        return GlobalSearchScopesCore.directoriesScope(project, /* withSubDirectories= */ true, roots);
    }
}
