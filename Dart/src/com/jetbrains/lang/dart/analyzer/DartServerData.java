package com.jetbrains.lang.dart.analyzer;

import com.google.common.collect.Sets;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.util.SmartList;
import gnu.trove.THashMap;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DartServerData {

  private DartServerRootsHandler myRootsHandler;

  private final Map<String, List<DartError>> myErrorData =
    Collections.synchronizedMap(new THashMap<String, List<DartError>>());
  private final Map<String, List<DartHighlightRegion>> myHighlightData =
    Collections.synchronizedMap(new THashMap<String, List<DartHighlightRegion>>());
  private final Map<String, List<DartNavigationRegion>> myNavigationData =
    Collections.synchronizedMap(new THashMap<String, List<DartNavigationRegion>>());
  private final Map<String, List<DartOverrideMember>> myOverrideData =
    Collections.synchronizedMap(new THashMap<String, List<DartOverrideMember>>());
  private final Map<String, List<DartRegion>> myImplementedClassData =
    Collections.synchronizedMap(new THashMap<String, List<DartRegion>>());
  private final Map<String, List<DartRegion>> myImplementedMemberData =
    Collections.synchronizedMap(new THashMap<String, List<DartRegion>>());

  private final Set<String> myFilePathsWithUnsentChanges = Sets.newConcurrentHashSet();

  DartServerData(@NotNull final DartServerRootsHandler rootsHandler) {
    myRootsHandler = rootsHandler;
  }

  void computedErrors(@NotNull final String filePath, @NotNull final List<AnalysisError> errors, final boolean restartHighlighting) {
    if (myFilePathsWithUnsentChanges.contains(filePath)) return;

    final List<DartError> newErrors = new ArrayList<DartError>(errors.size());
    for (AnalysisError error : errors) {
      newErrors.add(new DartError(error));
    }

    myErrorData.put(filePath, newErrors);

    if (restartHighlighting) {
      forceFileAnnotation(filePath, false);
    }
  }

  void computedHighlights(@NotNull final String filePath, @NotNull final List<HighlightRegion> regions) {
    if (myFilePathsWithUnsentChanges.contains(filePath)) return;

    final List<DartHighlightRegion> newRegions = new ArrayList<DartHighlightRegion>(regions.size());
    for (HighlightRegion region : regions) {
      if (region.getLength() > 0) {
        newRegions.add(new DartHighlightRegion(region));
      }
    }

    myHighlightData.put(filePath, newRegions);
    forceFileAnnotation(filePath, false);
  }

  void computedNavigation(@NotNull final String filePath, @NotNull final List<NavigationRegion> regions) {
    if (myFilePathsWithUnsentChanges.contains(filePath)) return;

    final List<DartNavigationRegion> newRegions = new ArrayList<DartNavigationRegion>(regions.size());
    for (NavigationRegion region : regions) {
      if (region.getLength() > 0) {
        newRegions.add(new DartNavigationRegion(region));
      }
    }

    myNavigationData.put(filePath, newRegions);
    forceFileAnnotation(filePath, true);
  }

  void computedOverrides(@NotNull final String filePath, @NotNull final List<OverrideMember> overrides) {
    if (myFilePathsWithUnsentChanges.contains(filePath)) return;

    final List<DartOverrideMember> newOverrides = new ArrayList<DartOverrideMember>(overrides.size());
    for (OverrideMember override : overrides) {
      if (override.getLength() > 0) {
        newOverrides.add(new DartOverrideMember(override));
      }
    }

    myOverrideData.put(filePath, newOverrides);
    forceFileAnnotation(filePath, false);
  }

  void computedImplemented(@NotNull final String filePath,
                           @NotNull final List<ImplementedClass> implementedClasses,
                           @NotNull final List<ImplementedMember> implementedMembers) {
    if (myFilePathsWithUnsentChanges.contains(filePath)) return;

    final List<DartRegion> newImplementedClasses = new ArrayList<DartRegion>(implementedClasses.size());
    for (ImplementedClass implementedClass : implementedClasses) {
      newImplementedClasses.add(new DartRegion(implementedClass.getOffset(), implementedClass.getLength()));
    }

    final List<DartRegion> newImplementedMembers = new ArrayList<DartRegion>(implementedMembers.size());
    for (ImplementedMember implementedMember : implementedMembers) {
      newImplementedMembers.add(new DartRegion(implementedMember.getOffset(), implementedMember.getLength()));
    }

    boolean hasChanges = false;
    final List<DartRegion> oldClasses = myImplementedClassData.get(filePath);
    if (oldClasses == null || !oldClasses.equals(newImplementedClasses)) {
      hasChanges = true;
      myImplementedClassData.put(filePath, newImplementedClasses);
    }

    final List<DartRegion> oldMembers = myImplementedMemberData.get(filePath);
    if (oldMembers == null || !oldMembers.equals(newImplementedMembers)) {
      hasChanges = true;
      myImplementedMemberData.put(filePath, newImplementedMembers);
    }

    if (hasChanges) {
      forceFileAnnotation(filePath, false);
    }
  }

  @NotNull
  List<DartError> getErrors(@NotNull final VirtualFile file) {
    final List<DartError> errors = myErrorData.get(file.getPath());
    return errors != null ? errors : Collections.<DartError>emptyList();
  }

  @NotNull
  List<DartHighlightRegion> getHighlight(@NotNull final VirtualFile file) {
    final List<DartHighlightRegion> regions = myHighlightData.get(file.getPath());
    return regions != null ? regions : Collections.<DartHighlightRegion>emptyList();
  }

  @NotNull
  List<DartNavigationRegion> getNavigation(@NotNull final VirtualFile file) {
    final List<DartNavigationRegion> regions = myNavigationData.get(file.getPath());
    return regions != null ? regions : Collections.<DartNavigationRegion>emptyList();
  }

  @NotNull
  List<DartOverrideMember> getOverrideMembers(@NotNull final VirtualFile file) {
    final List<DartOverrideMember> regions = myOverrideData.get(file.getPath());
    return regions != null ? regions : Collections.<DartOverrideMember>emptyList();
  }

  @NotNull
  List<DartRegion> getImplementedClasses(@NotNull final VirtualFile file) {
    final List<DartRegion> classes = myImplementedClassData.get(file.getPath());
    return classes != null ? classes : Collections.<DartRegion>emptyList();
  }

  @NotNull
  List<DartRegion> getImplementedMembers(@NotNull final VirtualFile file) {
    final List<DartRegion> classes = myImplementedMemberData.get(file.getPath());
    return classes != null ? classes : Collections.<DartRegion>emptyList();
  }

  private void forceFileAnnotation(@NotNull final String filePath, final boolean clearCache) {
    final VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
    if (virtualFile != null) {
      Set<Project> projects = myRootsHandler.getTrackedProjects();
      for (final Project project : projects) {
        if (project.isDisposed()) continue;

        if (clearCache) {
          ResolveCache.getInstance(project).clearCache(true);
        }
        DaemonCodeAnalyzer.getInstance(project).restart();
      }
    }
  }

  void onFilesContentUpdated() {
    myFilePathsWithUnsentChanges.clear();
  }

  void onFileClosed(@NotNull final VirtualFile file) {
    // do not remove from myErrorData, this map is always kept up-to-date for all files, not only for visible
    myHighlightData.remove(file.getPath());
    myNavigationData.remove(file.getPath());
    myOverrideData.remove(file.getPath());
    myImplementedClassData.remove(file.getPath());
    myImplementedMemberData.remove(file.getPath());
  }

  void onFlushedResults(@NotNull final List<String> filePaths) {
    if (!myErrorData.isEmpty()) {
      for (String path : filePaths) {
        myErrorData.remove(FileUtil.toSystemIndependentName(path));
      }
    }
    if (!myHighlightData.isEmpty()) {
      for (String path : filePaths) {
        myHighlightData.remove(FileUtil.toSystemIndependentName(path));
      }
    }
    if (!myNavigationData.isEmpty()) {
      for (String path : filePaths) {
        myNavigationData.remove(FileUtil.toSystemIndependentName(path));
      }
    }
    if (!myOverrideData.isEmpty()) {
      for (String path : filePaths) {
        myOverrideData.remove(FileUtil.toSystemIndependentName(path));
      }
    }
    if (!myImplementedClassData.isEmpty()) {
      for (String path : filePaths) {
        myImplementedClassData.remove(FileUtil.toSystemIndependentName(path));
      }
    }
    if (!myImplementedMemberData.isEmpty()) {
      for (String path : filePaths) {
        myImplementedMemberData.remove(FileUtil.toSystemIndependentName(path));
      }
    }
  }

  void clearData() {
    myErrorData.clear();
    myHighlightData.clear();
    myNavigationData.clear();
    myOverrideData.clear();
    myImplementedClassData.clear();
    myImplementedMemberData.clear();
  }

  void onDocumentChanged(@NotNull final DocumentEvent e) {
    final VirtualFile file = FileDocumentManager.getInstance().getFile(e.getDocument());
    if (!DartAnalysisServerService.isLocalAnalyzableFile(file)) return;

    final String filePath = file.getPath();
    myFilePathsWithUnsentChanges.add(filePath);

    updateRegionsDeletingTouched(filePath, myErrorData.get(filePath), e);
    updateRegionsUpdatingTouched(myHighlightData.get(filePath), e);
    updateRegionsDeletingTouched(filePath, myNavigationData.get(filePath), e);
    updateRegionsDeletingTouched(filePath, myOverrideData.get(filePath), e);
    updateRegionsDeletingTouched(filePath, myImplementedClassData.get(filePath), e);
    updateRegionsDeletingTouched(filePath, myImplementedMemberData.get(filePath), e);
  }

  private static void updateRegionsDeletingTouched(@NotNull final String filePath,
                                                   @Nullable final List<? extends DartRegion> regions,
                                                   @NotNull final DocumentEvent e) {
    if (regions == null) return;

    // delete touched regions, shift untouched
    final int eventOffset = e.getOffset();
    final int deltaLength = e.getNewLength() - e.getOldLength();

    final Iterator<? extends DartRegion> iterator = regions.iterator();
    while (iterator.hasNext()) {
      final DartRegion region = iterator.next();

      if (region instanceof DartNavigationRegion) {
        // may be we'd better delete target touched by editing?
        for (DartNavigationTarget target : ((DartNavigationRegion)region).getTargets()) {
          if (target.myFile.equals(filePath) && target.myOffset >= eventOffset) {
            target.myOffset += deltaLength;
          }
        }
      }

      if (deltaLength > 0) {
        // Something was typed. Shift untouched regions, delete touched.
        if (eventOffset <= region.myOffset) {
          region.myOffset += deltaLength;
        }
        else if (region.myOffset < eventOffset && eventOffset < region.myOffset + region.myLength) {
          iterator.remove();
        }
      }
      else if (deltaLength < 0) {
        // Some text was deleted. Shift untouched regions, delete touched.
        final int eventRightOffset = eventOffset - deltaLength;

        if (eventRightOffset <= region.myOffset) {
          region.myOffset += deltaLength;
        }
        else if (eventOffset < region.myOffset + region.myLength) {
          iterator.remove();
        }
      }
    }
  }

  private static void updateRegionsUpdatingTouched(@Nullable final List<? extends DartRegion> regions,
                                                   @NotNull final DocumentEvent e) {
    if (regions == null) return;

    final int eventOffset = e.getOffset();
    final int deltaLength = e.getNewLength() - e.getOldLength();

    final Iterator<? extends DartRegion> iterator = regions.iterator();
    while (iterator.hasNext()) {
      final DartRegion region = iterator.next();

      if (deltaLength > 0) {
        // Something was typed. Shift untouched regions, update touched.
        if (eventOffset <= region.myOffset) {
          region.myOffset += deltaLength;
        }
        else if (region.myOffset < eventOffset && eventOffset < region.myOffset + region.myLength) {
          region.myLength += deltaLength;
        }
      }
      else if (deltaLength < 0) {
        // Some text was deleted. Shift untouched regions, delete or update touched.
        final int eventRightOffset = eventOffset - deltaLength;
        final int regionRightOffset = region.myOffset + region.myLength;

        if (eventRightOffset <= region.myOffset) {
          region.myOffset += deltaLength;
        }
        else if (region.myOffset <= eventOffset && eventRightOffset <= regionRightOffset && region.myLength != -deltaLength) {
          region.myLength += deltaLength;
        }
        else if (eventOffset < regionRightOffset) {
          iterator.remove();
        }
      }
    }
  }


  public static class DartRegion {
    protected int myOffset;
    protected int myLength;

    DartRegion(final int offset, final int length) {
      myOffset = offset;
      myLength = length;
    }

    public final int getOffset() {
      return myOffset;
    }

    public final int getLength() {
      return myLength;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof DartRegion && myOffset == ((DartRegion)o).myOffset && myLength == ((DartRegion)o).myLength;
    }

    @Override
    public int hashCode() {
      return myOffset * 31 + myLength;
    }
  }

  public static class DartHighlightRegion extends DartRegion {
    private final String type;

    private DartHighlightRegion(@NotNull final HighlightRegion region) {
      super(region.getOffset(), region.getLength());
      type = region.getType().intern();
    }

    public String getType() {
      return type;
    }
  }

  public static class DartError extends DartRegion {
    private final String myAnalysisErrorFileSD;
    private final String mySeverity;
    private final String myType;
    @Nullable private final String myCode;
    private final String myMessage;

    private DartError(@NotNull final AnalysisError error) {
      super(error.getLocation().getOffset(), error.getLocation().getLength());
      myAnalysisErrorFileSD = error.getLocation().getFile().intern();
      mySeverity = error.getSeverity().intern();
      myType = error.getType().intern();
      myCode = error.getCode() == null ? null : error.getCode().intern();
      myMessage = error.getMessage();
    }

    public String getAnalysisErrorFileSD() {
      return myAnalysisErrorFileSD;
    }

    public String getSeverity() {
      return mySeverity;
    }

    public String getType() {
      return myType;
    }

    @Nullable
    public String getCode() {
      return myCode;
    }

    public String getMessage() {
      return myMessage;
    }
  }

  public static class DartNavigationRegion extends DartRegion {
    private final List<DartNavigationTarget> myTargets = new SmartList<>();

    DartNavigationRegion(@NotNull final NavigationRegion region) {
      super(region.getOffset(), region.getLength());

      for (NavigationTarget target : region.getTargetObjects()) {
        myTargets.add(new DartNavigationTarget(target));
      }
    }

    @Override
    public String toString() {
      return "DartNavigationRegion(" + myOffset + ", " + myLength + ")";
    }

    public List<DartNavigationTarget> getTargets() {
      return myTargets;
    }
  }

  public static class DartNavigationTarget {
    private final String myFile;
    private int myOffset;
    private final String myKind;

    private DartNavigationTarget(@NotNull final NavigationTarget target) {
      myFile = FileUtil.toSystemIndependentName(target.getFile()).intern();
      myOffset = target.getOffset();
      myKind = target.getKind().intern();
    }

    public String getFile() {
      return myFile;
    }

    public int getOffset() {
      return myOffset;
    }

    public String getKind() {
      return myKind;
    }
  }

  public static class DartOverrideMember extends DartRegion {
    @Nullable private final OverriddenMember mySuperclassMember;
    @Nullable private final List<OverriddenMember> myInterfaceMembers;

    private DartOverrideMember(@NotNull final OverrideMember overrideMember) {
      super(overrideMember.getOffset(), overrideMember.getLength());

      mySuperclassMember = overrideMember.getSuperclassMember();
      myInterfaceMembers = overrideMember.getInterfaceMembers();
    }

    @Nullable
    public OverriddenMember getSuperclassMember() {
      return mySuperclassMember;
    }

    @Nullable
    public List<OverriddenMember> getInterfaceMembers() {
      return myInterfaceMembers;
    }
  }
}
