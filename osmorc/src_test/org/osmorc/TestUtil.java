/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.facet.FacetManager;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.JavaTestFixtureFactory;
import com.intellij.testFramework.fixtures.TestFixtureBuilder;
import com.intellij.util.io.ZipUtil;
import org.jdom.JDOMException;
import org.osmorc.facet.OsmorcFacet;
import org.osmorc.facet.OsmorcFacetType;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class TestUtil {
    public static IdeaProjectTestFixture createTestFixture() {
        TestFixtureBuilder<IdeaProjectTestFixture> fixtureBuilder = JavaTestFixtureFactory.createFixtureBuilder();

        return fixtureBuilder.getFixture();
    }

    public static void loadModules(final String projectName, final Project project, final String projectDirPath) throws Exception {
        final File projectZIP = new File(getTestDataDir(), projectName + ".zip");
        assert projectZIP.exists() : projectZIP.getAbsoluteFile() + " not found";
        assert !projectZIP.isDirectory() : projectZIP.getAbsolutePath() + " is a directory";

        final File projectDir = new File(projectDirPath);
        ZipUtil.extract(projectZIP, projectDir, null);

        final ModifiableModuleModel moduleModel = ModuleManager.getInstance(project).getModifiableModel();

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                try {
                    List<ModifiableRootModel> rootModels = new ArrayList<ModifiableRootModel>();
                    for (File moduleDir : projectDir.listFiles(new FileFilter() {
                        public boolean accept(File pathname) {
                            return pathname.isDirectory() && !pathname.getName().startsWith(".");
                        }
                    })) {
                        String moduleDirPath = moduleDir.getPath().replace(File.separatorChar, '/') + "/";
                        final String moduleFileName = moduleDirPath + moduleDir.getName() + ".iml";
                        if ( new File(moduleFileName).exists()) {
                        Module module = moduleModel.loadModule(moduleFileName);
                        ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
                        VirtualFile file = VirtualFileManager.getInstance().getFileSystem("file").findFileByPath(moduleDirPath);
                        ContentEntry contentEntry = rootModel.addContentEntry(file);
                        contentEntry.addSourceFolder(file.findChild("src"), false);
                        rootModels.add(rootModel);
                        }
                    }

                    ProjectRootManager.getInstance(project).multiCommit(moduleModel,
                            rootModels.toArray(new ModifiableRootModel[rootModels.size()]));
                }
                catch (InvalidDataException e) {
                    throw new RuntimeException(e);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                catch (JDOMException e) {
                    throw new RuntimeException(e);
                }
                catch (ModuleWithNameAlreadyExists e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static void createOsmorcFacetForAllModules(final Project project) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                final Module[] modules = ModuleManager.getInstance(project).getModules();
                for (Module module : modules) {
                    final ModifiableFacetModel modifiableFacetModel = FacetManager.getInstance(module).createModifiableModel();
                    final OsmorcFacet facet = new OsmorcFacet(module);
                    facet.getConfiguration().setUseProjectDefaultManifestFileLocation(false);
                    facet.getConfiguration().setManifestLocation("META-INF/MANIFEST.MF");
                    facet.getConfiguration().setOsmorcControlsManifest(false);
                    modifiableFacetModel.addFacet(facet);
                    modifiableFacetModel.commit();
                }
            }
        });
    }

    public static void createOsmorcFacetForModule(final Project project, final String moduleName, boolean isManifestGenerated) {
        final Module module = ModuleManager.getInstance(project).findModuleByName(moduleName);
        createOsmorcFacetForModule(module, isManifestGenerated);
    }

    public static void createOsmorcFacetForModule(final Module module, final boolean isManifestGenerated) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                final ModifiableFacetModel modifiableFacetModel = FacetManager.getInstance(module).createModifiableModel();
                final OsmorcFacet facet = new OsmorcFacet(module);
                facet.getConfiguration().setUseProjectDefaultManifestFileLocation(false);
                facet.getConfiguration().setManifestLocation("META-INF/MANIFEST.MF");
                facet.getConfiguration().setOsmorcControlsManifest(isManifestGenerated);
                modifiableFacetModel.addFacet(facet);
                modifiableFacetModel.commit();
            }
        });
    }

    public static void removeOsmorcFacetOfModule(final Module module) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                final ModifiableFacetModel modifiableFacetModel = FacetManager.getInstance(module).createModifiableModel();
                OsmorcFacet facet = modifiableFacetModel.getFacetByType(OsmorcFacetType.ID);
                modifiableFacetModel.removeFacet(facet);
                modifiableFacetModel.commit();
            }
        });
    }

    public static void createModuleDependency(final Project project, final String fromModuleName, final String toModuleName) {
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                final Module fromModule = ModuleManager.getInstance(project).findModuleByName(fromModuleName);
                final Module toModule = ModuleManager.getInstance(project).findModuleByName(toModuleName);

                ModifiableRootModel rootModel = ModuleRootManager.getInstance(fromModule).getModifiableModel();
                rootModel.addModuleOrderEntry(toModule);
                rootModel.commit();
            }
        });
    }

    public static PsiFile loadPsiFile(Project project, String moduleName, String filePathInSource) {
        final ModuleRootManager rootManager = getModuleRootManager(project, moduleName);
        final VirtualFile root = rootManager.getSourceRoots()[0];
        VirtualFile file = root.findFileByRelativePath(filePathInSource);

        return PsiManager.getInstance(project).findFile(file);
    }

    public static PsiFile loadPsiFileUnderContent(Project project, String moduleName, String filePathInContent) {
        final ModuleRootManager rootManager = getModuleRootManager(project, moduleName);
        VirtualFile root = rootManager.getContentRoots()[0];
        VirtualFile file = root.findFileByRelativePath(filePathInContent);

        return PsiManager.getInstance(project).findFile(file);
    }

    public static ModuleRootManager getModuleRootManager(Project project, String moduleName) {
        ModuleManager moduleManager = ModuleManager.getInstance(project);

        Module module = moduleManager.findModuleByName(moduleName);

        return ModuleRootManager.getInstance(module);
    }

    public static List<ProblemDescriptor> runInspection(LocalInspectionTool inspection, PsiFile psiFile, Project project) {
        ProblemsHolder holder = new ProblemsHolder(InspectionManager.getInstance(project), psiFile);
        final PsiElementVisitor elementVisitor = inspection.buildVisitor(holder, true);

        psiFile.accept(new PsiRecursiveElementVisitor() {

            public void visitElement(PsiElement psielement) {
                psielement.accept(elementVisitor);
                super.visitElement(psielement);
            }
        });


        return holder.getResults();
    }

    private static File getTestDataDir() {
        if (TEST_DATA_DIR == null) {
            TEST_DATA_DIR = new File(TestUtil.class.getResource("/").getFile(), "../../../testdata");
            if (!TEST_DATA_DIR.exists()) {
                TEST_DATA_DIR = new File(TestUtil.class.getResource("").getFile(), "../../../../../testdata");
            }
            if (!TEST_DATA_DIR.exists()) {
                TEST_DATA_DIR = new File(PathManager.getHomePath(), "contrib/osmorc/testdata");
            }
            assert TEST_DATA_DIR.exists();
            assert TEST_DATA_DIR.isDirectory();
        }

        return TEST_DATA_DIR;
    }

    public static ModuleOrderEntry getOrderEntry(Module forModule, Module inModule) {
        ModuleOrderEntry result = null;
        ModifiableRootModel model = ModuleRootManager.getInstance(inModule).getModifiableModel();
        OrderEntry[] orderEntries = model.getOrderEntries();
        model.dispose();
        for (OrderEntry orderEntry : orderEntries) {
            if (orderEntry instanceof ModuleOrderEntry && ((ModuleOrderEntry) orderEntry).getModule() == forModule) {
                result = (ModuleOrderEntry) orderEntry;
                break;
            }
        }
        return result;
    }

    private static File TEST_DATA_DIR;
}
