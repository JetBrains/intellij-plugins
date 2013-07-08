package org.jetbrains.plugins.cucumber.steps;

import com.intellij.ProjectTopics;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;

import java.util.*;

/**
 * User: Andrey.Vokin
 * Date: 6/26/13
 */
public abstract class NotIndexedCucumberExtension extends AbstractCucumberExtension {
  protected final List<AbstractStepDefinition> myStepDefinitions = new ArrayList<AbstractStepDefinition>();

  private final Set<String> myProcessedStepDirectories = new HashSet<String>();

  private final MergingUpdateQueue myUpdateQueue = new MergingUpdateQueue("Steps reparse", 500, true, null);

  private final CucumberPsiTreeListener myCucumberPsiTreeListener = new CucumberPsiTreeListener();

  public void init(@NotNull final Project project) {
    myUpdateQueue.setPassThrough(false);

    PsiManager.getInstance(project).addPsiTreeChangeListener(myCucumberPsiTreeListener);

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

        collectAllStepDefsProviders(myPreviousStepDefsProviders, project);
      }

      public void rootsChanged(ModuleRootEvent event) {
        // compare new and previous content roots
        final List<VirtualFile> newStepDefsProviders = new ArrayList<VirtualFile>();
        collectAllStepDefsProviders(newStepDefsProviders, project);

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

  @Override
  public Collection<? extends PsiFile> getStepDefinitionContainers(@NotNull final GherkinFile featureFile) {
    final Set<PsiDirectory> stepDefRoots = findStepDefsRoots(featureFile);

    final Set<PsiFile> stepDefs = ContainerUtil.newHashSet();
    for (PsiDirectory root : stepDefRoots) {
      stepDefs.addAll(gatherStepDefinitionsFilesFromDirectory(root, true));
    }
    return stepDefs.isEmpty() ? Collections.<PsiFile>emptySet() : stepDefs;
  }

  protected Set<PsiDirectory> findStepDefsRoots(@NotNull final GherkinFile featureFile) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(featureFile);

    final VirtualFile file = featureFile.getVirtualFile();
    if (file == null || module == null) {
      return Collections.emptySet();
    }

    final List<PsiDirectory> result = new ArrayList<PsiDirectory>();
    findRelatedStepDefsRoots(module, featureFile, result, new HashSet<String>());

    return new HashSet<PsiDirectory>(result);
  }


  private void createWatcher(final PsiFile file) {
    myCucumberPsiTreeListener.addChangesWatcher(file, new CucumberPsiTreeListener.ChangesWatcher() {
      public void onChange(PsiElement parentPsiElement) {
        myUpdateQueue.queue(new Update(file) {
          public void run() {
            if (!file.getProject().isDisposed()) {
              reloadAbstractStepDefinitions(file);
            }
          }
        });
      }
    });
  }

  private void reloadAbstractStepDefinitions(final PsiFile file) {
    // Do not commit document if file was deleted
    final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(file.getProject());
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

  @NotNull
  private List<PsiFile> gatherStepDefinitionsFilesFromDirectory(@NotNull final PsiDirectory dir, final boolean writableOnly) {
    final List<PsiFile> result = new ArrayList<PsiFile>();

    // find step definitions in current folder
    for (PsiFile file : dir.getFiles()) {
      final VirtualFile virtualFile = file.getVirtualFile();

      final PsiDirectory parent = file.getParent();
      if (parent != null) {
        boolean isStepFile = writableOnly ? isWritableStepLikeFile(file, parent) : isStepLikeFile(file, parent);
        if (isStepFile && virtualFile != null) {
          result.add(file);
        }
      }
    }
    // process subfolders
    for (PsiDirectory subDir : dir.getSubdirectories()) {
      result.addAll(gatherStepDefinitionsFilesFromDirectory(subDir, writableOnly));
    }

    return result;
  }

  public List<AbstractStepDefinition> loadStepsFor(@Nullable final PsiFile featureFile, @NotNull final Module module) {
    // New step definitions folders roots
    final List<PsiDirectory> notLoadedStepDefinitionsRoots = new ArrayList<PsiDirectory>();
    try {
      if (featureFile != null) {
        findRelatedStepDefsRoots(module, featureFile, notLoadedStepDefinitionsRoots, myProcessedStepDirectories);
      }
      loadStepDefinitionRootsFromLibraries(module, notLoadedStepDefinitionsRoots, myProcessedStepDirectories);
    }
    catch (ProcessCanceledException e) {
      // just stop items gathering
      return Collections.emptyList();
    }

    synchronized (myStepDefinitions) {
      // Parse new folders
      final List<AbstractStepDefinition> stepDefinitions = new ArrayList<AbstractStepDefinition>();
      for (PsiDirectory root : notLoadedStepDefinitionsRoots) {
        stepDefinitions.clear();
        // let's process each folder separately
        try {
          myProcessedStepDirectories.add(root.getVirtualFile().getPath());
          final List<PsiFile> files = gatherStepDefinitionsFilesFromDirectory(root, false);
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

    synchronized (myStepDefinitions) {
      return new ArrayList<AbstractStepDefinition>(myStepDefinitions);
    }
  }

  protected static void addStepDefsRootIfNecessary(final VirtualFile root,
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

  protected abstract void loadStepDefinitionRootsFromLibraries(Module module, List<PsiDirectory> roots, Set<String> directories);

  protected abstract Collection<AbstractStepDefinition> getStepDefinitions(@NotNull final PsiFile file);

  protected abstract void collectAllStepDefsProviders(@NotNull final List<VirtualFile> providers, @NotNull final Project project);

  public abstract void findRelatedStepDefsRoots(@NotNull final Module module, @NotNull final PsiFile featureFile,
                                                final List<PsiDirectory> newStepDefinitionsRoots,
                                                final Set<String> processedStepDirectories);

    public void reset() {
    myUpdateQueue.cancelAllUpdates();
    synchronized (myStepDefinitions) {
      myStepDefinitions.clear();
    }
    myProcessedStepDirectories.clear();
  }

  public void flush() {
    myUpdateQueue.flush();
  }
}
