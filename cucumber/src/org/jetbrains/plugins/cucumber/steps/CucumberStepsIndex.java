package org.jetbrains.plugins.cucumber.steps;

import com.intellij.ProjectTopics;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.containers.HashMap;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberJvmExtensionPoint;
import org.jetbrains.plugins.cucumber.CucumberUtil;

import java.util.*;

/**
 * @author yole
 */
public class CucumberStepsIndex {
  private static final Logger LOG = Logger.getInstance(CucumberStepsIndex.class.getName());

  public static CucumberStepsIndex getInstance(Project project) {
    return ServiceManager.getService(project, CucumberStepsIndex.class);
  }

  private Project myProject;
  private final List<AbstractStepDefinition> myStepDefinitions = new ArrayList<AbstractStepDefinition>();
  private final Set<String> myProcessedStepDirectories = new HashSet<String>();

  private final MergingUpdateQueue myUpdateQueue = new MergingUpdateQueue("Steps reparse", 500, true, null);

  private final CucumberJvmExtensionPoint[] myExtensionList;

  private final Map<FileType, CucumberJvmExtensionPoint> myExtensionMap;

  private final CucumberPsiTreeListener myCucumberPsiTreeListener;

  public CucumberStepsIndex(final Project project) {
    myUpdateQueue.setPassThrough(false);
    myProject = project;

    myExtensionList = Extensions.getExtensions(CucumberJvmExtensionPoint.EP_NAME);
    myExtensionMap = new HashMap<FileType, CucumberJvmExtensionPoint>();

    myCucumberPsiTreeListener = new CucumberPsiTreeListener();
    PsiManager.getInstance(project).addPsiTreeChangeListener(myCucumberPsiTreeListener);

    for (CucumberJvmExtensionPoint e : myExtensionList) {
      myExtensionMap.put(e.getStepFileType(), e);
    }

    // Register steps files change watcher
    PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {
      @Override
      public void childAdded(@NotNull PsiTreeChangeEvent event) {
        final PsiElement parent = event.getParent();
        PsiElement child = event.getChild();
        if (isStepLikeFile(child, parent)) {
          final PsiFile file = (PsiFile)child;
          myUpdateQueue.queue(new Update(parent) {
            public void run() {
              if (file.isValid()) {
                reloadAbstractStepDefinitions(file);
                createWatcher(file);
              }
            }
          });
        }
      }

      @Override
      public void childRemoved(@NotNull PsiTreeChangeEvent event) {
        final PsiElement parent = event.getParent();
        final PsiElement child = event.getChild();
        if (isStepLikeFile(child, parent)) {
          myUpdateQueue.queue(new Update(parent) {
            public void run() {
              removeAbstractStepDefinitionsRelatedTo((PsiFile)child);
            }
          });
        }
      }
    });

    // clear caches after modules roots were changed
    final MessageBusConnection connection = project.getMessageBus().connect();
    connection.subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
      final List<VirtualFile> myPreviousStepDefsProviders = new ArrayList<VirtualFile>();

      public void beforeRootsChange(ModuleRootEvent event) {
        myPreviousStepDefsProviders.clear();

        collectAllStepDefsProviders(myPreviousStepDefsProviders);
      }

      private void collectAllStepDefsProviders(final List<VirtualFile> providers) {
        for (CucumberJvmExtensionPoint extension : myExtensionList) {
          extension.collectAllStepDefsProviders(providers, myProject);
        }
      }

      public void rootsChanged(ModuleRootEvent event) {
        // compare new and previous content roots
        final List<VirtualFile> newStepDefsProviders = new ArrayList<VirtualFile>();
        collectAllStepDefsProviders(newStepDefsProviders);

        if (!compareRoots(newStepDefsProviders)) {
          // clear caches on roots changed
          reset();
        }
      }

      private boolean compareRoots(final List<VirtualFile> newStepDefsProviders) {
        if (myPreviousStepDefsProviders.size() != newStepDefsProviders.size()) {
          return false;
        }
        for (VirtualFile root : myPreviousStepDefsProviders) {
          if (!newStepDefsProviders.contains(root)) {
            return false;
          }
        }
        return true;
      }
    });

    Disposer.register(project, connection);
  }

  public int getExtensionCount() {
    return myExtensionList.length;
  }

  /**
   * Creates file that will contain step definitions
   *
   * @param dir                      container for created file
   * @param fileNameWithoutExtension name of the file with out "." and extension
   * @param fileType                 type of file to create
   */
  public PsiFile createStepDefinitionFile(@NotNull final PsiDirectory dir, @NotNull final String fileNameWithoutExtension, @NotNull final FileType fileType) {
    CucumberJvmExtensionPoint extension = myExtensionMap.get(fileType);
    if (extension == null) {
      LOG.error(String.format("Unsupported step definition file type %s", fileType.getName()));
      return null;
    }

    return extension.getStepDefinitionCreator().createStepDefinitionContainer(dir, fileNameWithoutExtension);
  }

  // ToDo: move to q-fix
  public boolean validateNewStepDefinitionFileName(@NotNull final PsiDirectory directory, @NotNull final String fileName, @NotNull final FileType fileType) {
    CucumberJvmExtensionPoint ep = myExtensionMap.get(fileType);
    assert ep != null;
    return ep.getStepDefinitionCreator().validateNewStepDefinitionFileName(directory.getProject(), fileName);
  }

  private boolean isStepLikeFile(PsiElement child, PsiElement parent) {
    for (CucumberJvmExtensionPoint ep : myExtensionList) {
      if (ep.isStepLikeFile(child, parent)) {
        return true;
      }
    }

    return false;
  }

  public void reset() {
    myUpdateQueue.cancelAllUpdates();
    synchronized (myStepDefinitions) {
      myStepDefinitions.clear();
    }
    myProcessedStepDirectories.clear();
  }

  @Nullable
  public AbstractStepDefinition findStepDefinition(final @NotNull PsiFile featureFile, final String stepName) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(featureFile);
    if (module == null) return null;

    loadStepsFor(featureFile, module);
    synchronized (myStepDefinitions) {
      for (AbstractStepDefinition stepDefinition : myStepDefinitions) {
        if (stepDefinition.matches(stepName)) {
          return stepDefinition;
        }
      }
    }
    return null;
  }

  @NotNull
  public Set<AbstractStepDefinition> findStepDefinitionsByPartOfName(@NotNull final String word) {
    final Set<AbstractStepDefinition> result = new HashSet<AbstractStepDefinition>();
    synchronized (myStepDefinitions) {
      for (AbstractStepDefinition stepDefinition : myStepDefinitions) {
        if (CucumberUtil.isPatternRelatedToPartOfName(stepDefinition.getPattern(), word)) {
          if (!result.contains(stepDefinition)) {
            result.add(stepDefinition);
          }
        }
      }
    }
    return result;
  }

  public List<AbstractStepDefinition> findStepDefinitionsByPattern(@NotNull final String pattern, @NotNull final Module module) {
    loadStepsFor(null, module);
    final List<AbstractStepDefinition> result = new ArrayList<AbstractStepDefinition>();
    synchronized (myStepDefinitions) {
      for (AbstractStepDefinition stepDefinition : myStepDefinitions) {
        final String elementText = stepDefinition.getElementText();
        if (elementText != null && elementText.equals(pattern)) {
          result.add(stepDefinition);
        }
      }
    }
    return result;
  }

  @Nullable
  public PsiElement findStep(PsiFile featureFile, String stepName) {
    final AbstractStepDefinition definition = findStepDefinition(featureFile, stepName);
    return definition != null ? definition.getElement() : null;
  }

  public List<AbstractStepDefinition> getAllStepDefinitions(@NotNull final PsiFile featureFile) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(featureFile);
    if (module == null) return Collections.emptyList();
    loadStepsFor(featureFile, module);
    synchronized (myStepDefinitions) {
      return new ArrayList<AbstractStepDefinition>(myStepDefinitions);
    }
  }

  public List<PsiFile> gatherStepDefinitionsFilesFromDirectory(@NotNull final PsiDirectory dir) {
    List<PsiFile> result = new ArrayList<PsiFile>();
    addAllStepsFiles(dir, result);
    return result;
  }

  private void addAllStepsFiles(@NotNull final PsiDirectory dir, final List<PsiFile> result) {
    // find step definitions in current folder
    for (PsiFile file : dir.getFiles()) {
      final VirtualFile virtualFile = file.getVirtualFile();
      boolean isStepFile = isStepLikeFile(file, file.getParent());
      if (isStepFile && virtualFile != null) {
        result.add(file);
      }
    }

    // process subfolders
    for (PsiDirectory subDir : dir.getSubdirectories()) {
      addAllStepsFiles(subDir, result);
    }
  }

  private void loadStepsFor(@Nullable final PsiFile featureFile, @NotNull final Module module) {
    // New step definitions folders roots
    final List<PsiDirectory> notLoadedStepDefinitionsRoots = new ArrayList<PsiDirectory>();
    try {
      findRelatedStepDefsRoots(featureFile, module, notLoadedStepDefinitionsRoots, myProcessedStepDirectories);
    }
    catch (ProcessCanceledException e) {
      // just stop items gathering
      return;
    }

    synchronized (myStepDefinitions) {
      // Parse new folders
      final List<AbstractStepDefinition> stepDefinitions = new ArrayList<AbstractStepDefinition>();
      for (PsiDirectory root : notLoadedStepDefinitionsRoots) {
        stepDefinitions.clear();
        // let's process each folder separately
        try {
          myProcessedStepDirectories.add(root.getVirtualFile().getPath());
          List<PsiFile> files = gatherStepDefinitionsFilesFromDirectory(root);
          for (final PsiFile file : files) {
            removeAbstractStepDefinitionsRelatedTo(file);
            stepDefinitions.addAll(getStepDefinitions(file));
            createWatcher(file);
          }

          myStepDefinitions.addAll(stepDefinitions);
        }
        catch (ProcessCanceledException e) {
          // remove from processed
          myProcessedStepDirectories.remove(root.getVirtualFile().getPath());
          // remove new step definitions
          if (!stepDefinitions.isEmpty()) {
            myStepDefinitions.removeAll(stepDefinitions);
          }
          throw e;
        }
      }
    }
  }

  public void findRelatedStepDefsRoots(@Nullable final PsiFile featureFile, @NotNull final Module module,
                                       final List<PsiDirectory> newStepDefinitionsRoots,
                                       final Set<String> processedStepDirectories) {

    for (CucumberJvmExtensionPoint extension : myExtensionList) {
      if (featureFile != null) {
        // get local steps_definitions from the same content root
        extension.findRelatedStepDefsRoots(module, featureFile, newStepDefinitionsRoots, processedStepDirectories);
      }

      extension.loadStepDefinitionRootsFromLibraries(module, newStepDefinitionsRoots, processedStepDirectories);
    }
  }

  public static void addStepDefsRootIfNecessary(final VirtualFile root,
                                                @NotNull final List<PsiDirectory> newStepDefinitionsRoots,
                                                @NotNull final Set<String> processedStepDirectories,
                                                @NotNull final Project project) {
    if (root == null || !root.isValid()) {
      return;
    }
    final String path = root.getPath();
    if (processedStepDirectories.contains(path)) {
      return;
    }

    final PsiDirectory rootPathDir = PsiManager.getInstance(project).findDirectory(root);
    if (rootPathDir != null && rootPathDir.isValid()) {
      if (!newStepDefinitionsRoots.contains(rootPathDir)) {
        newStepDefinitionsRoots.add(rootPathDir);
      }
    }
  }

  private void createWatcher(final PsiFile file) {
    myCucumberPsiTreeListener.addChangesWatcher(file, new CucumberPsiTreeListener.ChangesWatcher() {
      public void onChange(PsiElement parentPsiElement) {
        myUpdateQueue.queue(new Update(file) {
          public void run() {
            if (!myProject.isDisposed()) {
              reloadAbstractStepDefinitions(file);
            }
          }
        });
      }
    });
  }

  private void reloadAbstractStepDefinitions(final PsiFile file) {
    // Do not commit document if file was deleted
    final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(myProject);
    final Document document = psiDocumentManager.getDocument(file);
    if (document != null) {
      psiDocumentManager.commitDocument(document);
    }

    // remove old definitions related to current file
    removeAbstractStepDefinitionsRelatedTo(file);

    // read definitions from file
    if (file.isValid()) {
      synchronized (myStepDefinitions) {
        myStepDefinitions.addAll(getStepDefinitions(file));
      }
    }
  }

  private void removeAbstractStepDefinitionsRelatedTo(final PsiFile file) {
    // file may be invalid !!!!
    synchronized (myStepDefinitions) {
      for (Iterator<AbstractStepDefinition> iterator = myStepDefinitions.iterator(); iterator.hasNext(); ) {
        AbstractStepDefinition definition = iterator.next();
        final PsiElement element = definition.getElement();
        if (element == null || element.getContainingFile().equals(file)) {
          iterator.remove();
        }
      }
    }
  }

  private List<AbstractStepDefinition> getStepDefinitions(final PsiFile psiFile) {
    final List<AbstractStepDefinition> newDefs = new ArrayList<AbstractStepDefinition>();
    for (CucumberJvmExtensionPoint ep : myExtensionList) {
      newDefs.addAll(ep.getStepDefinitions(psiFile));
    }
    return newDefs;
  }

  public void flush() {
    myUpdateQueue.flush();
  }

  public Map<FileType, CucumberJvmExtensionPoint> getExtensionMap() {
    return myExtensionMap;
  }
}
