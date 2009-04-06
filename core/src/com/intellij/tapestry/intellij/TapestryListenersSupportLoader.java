package com.intellij.tapestry.intellij;

import com.intellij.ProjectTopics;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.intellij.core.resource.IntellijResource;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Registers all event listeners into IntelliJ IDEA API.
 */
public class TapestryListenersSupportLoader implements ProjectComponent {

    private static final Logger _logger = Logger.getLogger(TapestryListenersSupportLoader.class);

    private Project _project;
    private MessageBusConnection _messageBusConnection;
  private VirtualFileAdapter myListener;

  public TapestryListenersSupportLoader(Project project) {
        _project = project;
    }

    /**
     * {@inheritDoc}
     */
    public void initComponent() {
    }

    /**
     * {@inheritDoc}
     */
    public void disposeComponent() {
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public String getComponentName() {
        return TapestryListenersSupportLoader.class.getName();
    }

    public void projectOpened() {
        if (!TapestryApplicationSupportLoader.getInstance().hasValidLicense()) {
            return;
        }

        _messageBusConnection = _project.getMessageBus().connect();

        // Listener for file deletion
        PsiManager.getInstance(_project).addPsiTreeChangeListener(
                new PsiTreeChangeAdapter() {
                    public void childRemoved(PsiTreeChangeEvent event) {
                        Module module = getModuleFromEvent(event);
                        if (module == null) {
                            return;
                        }

                        TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);

                        if (!TapestryUtils.isTapestryModule(module)) {
                            return;
                        }

                        if (event.getChild() instanceof PsiJavaFile && IdeaUtils.findPublicClass(((PsiJavaFile) event.getChild()).getClasses()) != null) {
                            tapestryProject.getEventsManager().classDeleted(IdeaUtils.findPublicClass(((PsiJavaFile) event.getChild()).getClasses()).getQualifiedName());
                        }

                        if (event.getChild() instanceof PsiFile) {
                            tapestryProject.getEventsManager().fileDeleted(((PsiFile) event.getChild()).getVirtualFile().getPath());
                        }

                        if (event.getChild() instanceof PsiDirectory) {
                            tapestryProject.getEventsManager().fileDeleted(((PsiDirectory) event.getChild()).getVirtualFile().getPath());
                        }
                    }
                }
        );

        // Listener for file creation
        PsiManager.getInstance(_project).addPsiTreeChangeListener(
                new PsiTreeChangeAdapter() {
                    public void childAdded(PsiTreeChangeEvent event) {
                        Module module = getModuleFromEvent(event);
                        if (module == null) {
                            return;
                        }

                        TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);

                        if (!TapestryUtils.isTapestryModule(module)) {
                            return;
                        }

                        if (event.getFile() != null) {
                            tapestryProject.getEventsManager().fileContentsChanged(new IntellijResource(event.getFile()));
                        }

                        if (!(event.getChild() instanceof PsiFile)) {
                            return;
                        }

                        PsiFile psiFile = (PsiFile) event.getChild();

                        if (psiFile instanceof PsiJavaFile) {
                            tapestryProject.getEventsManager().classCreated(null);

                            return;
                        }

                        tapestryProject.getEventsManager().fileCreated(psiFile.getVirtualFile().getPath());
                    }

                    public void childRemoved(PsiTreeChangeEvent event) {
                        Module module = getModuleFromEvent(event);
                        if (module == null) {
                            return;
                        }

                        if (!TapestryUtils.isTapestryModule(module)) {
                            return;
                        }

                        TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);

                        if (event.getFile() != null) {
                            tapestryProject.getEventsManager().fileContentsChanged(new IntellijResource(event.getFile()));
                        }
                    }
                }
        );

        myListener = new VirtualFileAdapter() {
            public void contentsChanged(VirtualFileEvent event) {
                Module module = null;
                try {
                    module = ProjectRootManager.getInstance(_project).getFileIndex().getModuleForFile(event.getParent());
                } catch (Throwable e) {
                    //ignore
                }
                if (module == null) {
                    return;
                }

                TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);

                if (!TapestryUtils.isTapestryModule(module)) {
                    return;
                }

                try {
                    if (event.getFile().getPath().equals(tapestryProject.getWebXmlPath())) {
                        tapestryProject.getEventsManager().modelChanged();
                    }
                } catch (NullPointerException ex) {
                    _logger.error("Error finding web.xml file", ex);
                }
            }
        };
        VirtualFileManager.getInstance().getFileSystem("file").addVirtualFileListener(myListener);

        _messageBusConnection.subscribe(ProjectTopics.PROJECT_ROOTS,
                new ModuleRootListener() {
                    public void beforeRootsChange(ModuleRootEvent event) {
                    }

                    public void rootsChanged(ModuleRootEvent event) {
                        for (Module module : ModuleManager.getInstance((Project) event.getSource()).getModules()) {
                            if (!TapestryUtils.isTapestryModule(module)) {
                                return;
                            }

                            TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);
                            tapestryProject.getEventsManager().modelChanged();
                        }
                    }
                }
        );
    }

    public void projectClosed() {
        VirtualFileManager.getInstance().getFileSystem("file").removeVirtualFileListener(myListener);
        _messageBusConnection.disconnect();
    }

    private Module getModuleFromEvent(PsiTreeChangeEvent event) {
        PsiElement parent = event.getParent();
        while (parent != null && !(parent instanceof PsiDirectory)) {
            parent = parent.getParent();
        }

        if (parent != null) {
            return ProjectRootManager.getInstance(_project).getFileIndex().getModuleForFile(((PsiDirectory) parent).getVirtualFile());
        } else {
            return null;
        }
    }
}
