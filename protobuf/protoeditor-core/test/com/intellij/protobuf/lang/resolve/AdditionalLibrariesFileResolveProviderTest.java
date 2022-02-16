package com.intellij.protobuf.lang.resolve;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;
import com.intellij.protobuf.lang.resolve.FileResolveProvider.ChildEntry;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link AdditionalLibrariesFileResolveProvider}.
 */
public class AdditionalLibrariesFileResolveProviderTest extends PbCodeInsightFixtureTestCase {

    private File tempDir = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        tempDir = FileUtil.createTempDirectory(getName(), UUID.randomUUID().toString(), false);
        WriteAction.run(() -> ProjectRootManagerEx.getInstanceEx(getProject()).makeRootsChange(
                () -> AdditionalLibraryRootsProvider.EP_NAME.getPoint().registerExtension(new AdditionalLibraryRootsProvider() {
                    @NotNull
                    @Override
                    public Collection<SyntheticLibrary> getAdditionalProjectLibraries(@NotNull Project project) {
                        List<VirtualFile> roots = Collections.singletonList(VfsUtil.findFile(tempDir.toPath(), true));
                        return Collections.singletonList(SyntheticLibrary.newImmutableLibrary(roots));
                    }
                }, getTestRootDisposable()), false, true));
    }

    @Override
    public void tearDown() throws Exception {
        FileUtil.delete(tempDir);
        super.tearDown();
    }

    public void testFindFilePrefersFirstListedPath() throws Exception {
        FileUtil.writeToFile(new File(tempDir, "com/foo/dir/foo.proto"), "// foo");


        FileResolveProvider resolver = new AdditionalLibrariesFileResolveProvider();
        VirtualFile foo = resolver.findFile("com/foo/dir/foo.proto", getProject());

        assertNotNull(foo);

        assertEquals("// foo", VfsUtil.loadText(foo));
    }

    public void testGetChildEntries() throws Exception {
        FileUtil.writeToFile(new File(tempDir, "com/foo/dir/foo.proto"), "// foo");
        FileUtil.writeToFile(new File(tempDir, "com/foo/dir/bar.proto"), "// bar");


        FileResolveProvider resolver = new AdditionalLibrariesFileResolveProvider();
        assertContainsElements(
                resolver.getChildEntries("", getProject()),
                ChildEntry.directory("com"));
        assertContainsElements(
                resolver.getChildEntries("com", getProject()), ChildEntry.directory("foo"));
        assertContainsElements(
                resolver.getChildEntries("com/", getProject()), ChildEntry.directory("foo"));
        assertContainsElements(
                resolver.getChildEntries("com/foo", getProject()), ChildEntry.directory("dir"));
        assertContainsElements(
                resolver.getChildEntries("com/foo/dir", getProject()),
                ChildEntry.file("foo.proto"),
                ChildEntry.file("bar.proto"));
        assertContainsElements(
                resolver.getChildEntries("com/foo/dir/", getProject()),
                ChildEntry.file("foo.proto"),
                ChildEntry.file("bar.proto"));
    }
}
