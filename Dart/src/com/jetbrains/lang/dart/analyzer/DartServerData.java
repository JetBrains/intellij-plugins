// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.analyzer;

import com.google.common.collect.Sets;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiModificationTrackerImpl;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.EventDispatcher;
import com.intellij.util.SmartList;
import gnu.trove.THashMap;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DartServerData {

  public interface OutlineListener extends EventListener {
    void outlineUpdated(@NotNull final String filePath);
  }

  private final DartAnalysisServerService myService;

  private final EventDispatcher<OutlineListener> myEventDispatcher = EventDispatcher.create(OutlineListener.class);

  private final Map<String, List<DartError>> myErrorData = Collections.synchronizedMap(new THashMap<>());
  private final Map<String, List<DartHighlightRegion>> myHighlightData = Collections.synchronizedMap(new THashMap<>());
  private final Map<String, List<DartNavigationRegion>> myNavigationData = Collections.synchronizedMap(new THashMap<>());
  private final Map<String, List<DartOverrideMember>> myOverrideData = Collections.synchronizedMap(new THashMap<>());
  private final Map<String, List<DartRegion>> myImplementedClassData = Collections.synchronizedMap(new THashMap<>());
  private final Map<String, List<DartRegion>> myImplementedMemberData = Collections.synchronizedMap(new THashMap<>());
  private final Map<String, Outline> myOutlineData = Collections.synchronizedMap(new THashMap<>());
  private final Map<Integer, AvailableSuggestionSet> myAvailableSuggestionSetMap = Collections.synchronizedMap(new THashMap<>());

  private final Set<String> myFilePathsWithUnsentChanges = Sets.newConcurrentHashSet();

  // keeps track of files in which error regions have been deleted by DocumentListener (typing inside an error region)
  private final Set<String> myFilePathsWithLostErrorInfo = Sets.newConcurrentHashSet();

  DartServerData(@NotNull final DartAnalysisServerService service) {
    myService = service;
  }

  boolean isErrorInfoLost(@NotNull final String filePath) {
    return myFilePathsWithLostErrorInfo.contains(filePath);
  }

  /**
   * @return {@code true} if {@code errors} were processes, {@code false} if ignored;
   * errors are ignored if the file has been edited and new contents has not yet been sent to the server.
   */
  boolean computedErrors(@NotNull final String filePath, @NotNull final List<AnalysisError> errors, final boolean restartHighlighting) {
    if (myFilePathsWithUnsentChanges.contains(filePath)) return false;

    final List<DartError> newErrors = new ArrayList<>(errors.size());
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);

    for (AnalysisError error : errors) {
      final int offset = myService.getConvertedOffset(file, error.getLocation().getOffset());
      final int length = myService.getConvertedOffset(file, error.getLocation().getOffset() + error.getLocation().getLength()) - offset;
      newErrors.add(new DartError(error, offset, length));
    }

    myFilePathsWithLostErrorInfo.remove(filePath);
    myErrorData.put(filePath, newErrors);

    if (restartHighlighting) {
      forceFileAnnotation(file, false);
    }

    return true;
  }

  void computedClosingLabels(@NotNull final String filePath, @NotNull final List<ClosingLabel> labels) {
    if (myFilePathsWithUnsentChanges.contains(filePath)) return;

    DartClosingLabelManager.getInstance().computedClosingLabels(myService.getProject(), FileUtil.toSystemIndependentName(filePath), labels);
  }

  void computedHighlights(@NotNull final String filePath, @NotNull final List<HighlightRegion> regions) {
    if (myFilePathsWithUnsentChanges.contains(filePath)) return;

    final List<DartHighlightRegion> newRegions = new ArrayList<>(regions.size());
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);

    for (HighlightRegion region : regions) {
      if (region.getLength() > 0) {
        final int offset = myService.getConvertedOffset(file, region.getOffset());
        final int length = myService.getConvertedOffset(file, region.getOffset() + region.getLength()) - offset;
        newRegions.add(new DartHighlightRegion(offset, length, region.getType()));
      }
    }

    myHighlightData.put(filePath, newRegions);
    forceFileAnnotation(file, false);
  }

  void computedNavigation(@NotNull final String filePath, @NotNull final List<NavigationRegion> regions) {
    if (myFilePathsWithUnsentChanges.contains(filePath)) return;

    final List<DartNavigationRegion> newRegions = new ArrayList<>(regions.size());
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);

    for (NavigationRegion region : regions) {
      if (region.getLength() > 0) {
        final DartNavigationRegion dartNavigationRegion = createDartNavigationRegion(myService, file, region);
        newRegions.add(dartNavigationRegion);
      }
    }

    myNavigationData.put(filePath, newRegions);
    forceFileAnnotation(file, true);
  }

  void computedOutline(@NotNull final String filePath, @NotNull final Outline outline) {
    if (myFilePathsWithUnsentChanges.contains(filePath)) return;

    myOutlineData.put(filePath, outline);
    ApplicationManager.getApplication().invokeLater(() -> myEventDispatcher.getMulticaster().outlineUpdated(filePath),
                                                    ModalityState.NON_MODAL,
                                                    myService.getProject().getDisposed());
  }

  void computedAvailableSuggestions(@NotNull final List<AvailableSuggestionSet> changed, @NotNull final int[] removed) {
    for (int id : removed) {
      myAvailableSuggestionSetMap.remove(id);
    }
    for (AvailableSuggestionSet suggestionSet : changed) {
      myAvailableSuggestionSetMap.put(suggestionSet.getId(), suggestionSet);
    }
  }

  @NotNull
  static DartNavigationRegion createDartNavigationRegion(@NotNull final DartAnalysisServerService service,
                                                         @Nullable final VirtualFile file,
                                                         @NotNull final NavigationRegion region) {
    final int offset = service.getConvertedOffset(file, region.getOffset());
    final int length = service.getConvertedOffset(file, region.getOffset() + region.getLength()) - offset;
    final SmartList<DartNavigationTarget> targets = new SmartList<>();
    for (NavigationTarget target : region.getTargetObjects()) {
      targets.add(new DartNavigationTarget(target));
    }
    return new DartNavigationRegion(offset, length, targets);
  }

  void computedOverrides(@NotNull final String filePath, @NotNull final List<OverrideMember> overrides) {
    if (myFilePathsWithUnsentChanges.contains(filePath)) return;

    final List<DartOverrideMember> newOverrides = new ArrayList<>(overrides.size());
    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);

    for (OverrideMember override : overrides) {
      if (override.getLength() > 0) {
        final int offset = myService.getConvertedOffset(file, override.getOffset());
        final int length = myService.getConvertedOffset(file, override.getOffset() + override.getLength()) - offset;
        newOverrides.add(new DartOverrideMember(offset, length, override.getSuperclassMember(), override.getInterfaceMembers()));
      }
    }

    myOverrideData.put(filePath, newOverrides);
    forceFileAnnotation(file, false);
  }

  void computedImplemented(@NotNull final String filePath,
                           @NotNull final List<ImplementedClass> implementedClasses,
                           @NotNull final List<ImplementedMember> implementedMembers) {
    if (myFilePathsWithUnsentChanges.contains(filePath)) return;

    final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);

    final List<DartRegion> newImplementedClasses = new ArrayList<>(implementedClasses.size());
    for (ImplementedClass implementedClass : implementedClasses) {
      final int offset = myService.getConvertedOffset(file, implementedClass.getOffset());
      final int length = myService.getConvertedOffset(file, implementedClass.getOffset() + implementedClass.getLength()) - offset;
      newImplementedClasses.add(new DartRegion(offset, length));
    }

    final List<DartRegion> newImplementedMembers = new ArrayList<>(implementedMembers.size());
    for (ImplementedMember implementedMember : implementedMembers) {
      final int offset = myService.getConvertedOffset(file, implementedMember.getOffset());
      final int length = myService.getConvertedOffset(file, implementedMember.getOffset() + implementedMember.getLength()) - offset;
      newImplementedMembers.add(new DartRegion(offset, length));
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
      forceFileAnnotation(file, false);
    }
  }

  @NotNull
  List<DartError> getErrors(@NotNull final SearchScope scope) {
    final List<DartError> errors = new ArrayList<>();

    synchronized (myErrorData) {
      for (Map.Entry<String, List<DartError>> entry : myErrorData.entrySet()) {
        final VirtualFile file = LocalFileSystem.getInstance().findFileByPath(entry.getKey());
        if (file != null && scope.contains(file)) {
          errors.addAll(entry.getValue());
        }
      }
    }

    return errors;
  }

  @NotNull
  List<DartError> getErrors(@NotNull final VirtualFile file) {
    final List<DartError> errors = myErrorData.get(file.getPath());
    return errors != null ? errors : Collections.emptyList();
  }

  @NotNull
  List<DartHighlightRegion> getHighlight(@NotNull final VirtualFile file) {
    final List<DartHighlightRegion> regions = myHighlightData.get(file.getPath());
    return regions != null ? regions : Collections.emptyList();
  }

  @NotNull
  List<DartNavigationRegion> getNavigation(@NotNull final VirtualFile file) {
    final List<DartNavigationRegion> regions = myNavigationData.get(file.getPath());
    return regions != null ? regions : Collections.emptyList();
  }

  @NotNull
  List<DartOverrideMember> getOverrideMembers(@NotNull final VirtualFile file) {
    final List<DartOverrideMember> regions = myOverrideData.get(file.getPath());
    return regions != null ? regions : Collections.emptyList();
  }

  @NotNull
  List<DartRegion> getImplementedClasses(@NotNull final VirtualFile file) {
    final List<DartRegion> classes = myImplementedClassData.get(file.getPath());
    return classes != null ? classes : Collections.emptyList();
  }

  @NotNull
  List<DartRegion> getImplementedMembers(@NotNull final VirtualFile file) {
    final List<DartRegion> classes = myImplementedMemberData.get(file.getPath());
    return classes != null ? classes : Collections.emptyList();
  }

  @Nullable
  Outline getOutline(@NotNull final VirtualFile file) {
    return myOutlineData.get(file.getPath());
  }

  void addOutlineListener(@NotNull final OutlineListener listener) {
    myEventDispatcher.addListener(listener);
  }

  void removeOutlineListener(@NotNull final OutlineListener listener) {
    myEventDispatcher.removeListener(listener);
  }

  @Nullable
  AvailableSuggestionSet getAvailableSuggestionSet(int id) {
    return myAvailableSuggestionSetMap.get(id);
  }

  private void forceFileAnnotation(@Nullable final VirtualFile file, final boolean clearCache) {
    if (file != null) {
      final Project project = myService.getProject();

      // It's ok to call DaemonCodeAnalyzer.restart() right in this thread, without invokeLater(),
      // but it will cache RemoteAnalysisServerImpl$ServerResponseReaderThread in FileStatusMap.threads and as a result,
      // DartAnalysisServerService.myProject will be leaked in tests
      ApplicationManager.getApplication()
        .invokeLater(() -> {
                       if (clearCache) {
                         ResolveCache.getInstance(project).clearCache(true);
                         ((PsiModificationTrackerImpl)PsiManager.getInstance(project).getModificationTracker()).incCounter();
                       }
                       DaemonCodeAnalyzer.getInstance(project).restart();
                     },
                     ModalityState.NON_MODAL,
                     project.getDisposed());
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
    myOutlineData.remove(file.getPath());
  }

  void onFlushedResults(@NotNull final List<String> filePaths) {
    if (!myErrorData.isEmpty()) {
      for (String path : filePaths) {
        myErrorData.remove(path);
      }
    }
    if (!myHighlightData.isEmpty()) {
      for (String path : filePaths) {
        myHighlightData.remove(path);
      }
    }
    if (!myNavigationData.isEmpty()) {
      for (String path : filePaths) {
        myNavigationData.remove(path);
      }
    }
    if (!myOverrideData.isEmpty()) {
      for (String path : filePaths) {
        myOverrideData.remove(path);
      }
    }
    if (!myImplementedClassData.isEmpty()) {
      for (String path : filePaths) {
        myImplementedClassData.remove(path);
      }
    }
    if (!myImplementedMemberData.isEmpty()) {
      for (String path : filePaths) {
        myImplementedMemberData.remove(path);
      }
    }
    if (!myOutlineData.isEmpty()) {
      for (String path : filePaths) {
        myOutlineData.remove(path);
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
    myOutlineData.clear();
    myAvailableSuggestionSetMap.clear();
  }

  void onDocumentChanged(@NotNull final DocumentEvent e) {
    final VirtualFile file = FileDocumentManager.getInstance().getFile(e.getDocument());
    if (!DartAnalysisServerService.isLocalAnalyzableFile(file)) return;

    final String filePath = file.getPath();
    myFilePathsWithUnsentChanges.add(filePath);

    boolean someRegionDeleted = updateRegionsDeletingTouched(filePath, myErrorData.get(filePath), e);
    if (someRegionDeleted) {
      myFilePathsWithLostErrorInfo.add(filePath);
    }
    updateRegionsUpdatingTouched(myHighlightData.get(filePath), e);
    updateRegionsDeletingTouched(filePath, myNavigationData.get(filePath), e);
    updateRegionsDeletingTouched(filePath, myOverrideData.get(filePath), e);
    updateRegionsDeletingTouched(filePath, myImplementedClassData.get(filePath), e);
    updateRegionsDeletingTouched(filePath, myImplementedMemberData.get(filePath), e);
    // A bit outdated outline data is not a big problem, updated data will come shortly
  }

  /**
   * @return {@code true} if at least one region has been deleted, {@code false} if updated only or nothing done at all
   */
  private static boolean updateRegionsDeletingTouched(@NotNull final String filePath,
                                                      @Nullable final List<? extends DartRegion> regions,
                                                      @NotNull final DocumentEvent e) {
    if (regions == null) return false;

    boolean regionDeleted = false;

    // delete touched regions, shift untouched
    final int eventOffset = e.getOffset();
    final int deltaLength = e.getNewLength() - e.getOldLength();

    final Iterator<? extends DartRegion> iterator = regions.iterator();
    while (iterator.hasNext()) {
      final DartRegion region = iterator.next();

      if (region instanceof DartNavigationRegion) {
        // may be we'd better delete target touched by editing?
        for (DartNavigationTarget target : ((DartNavigationRegion)region).getTargets()) {
          if (target.myFile.equals(filePath) && target.myConvertedOffset >= eventOffset) {
            target.myConvertedOffset += deltaLength;
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
          regionDeleted = true;
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
          regionDeleted = true;
        }
      }
    }

    return regionDeleted;
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

    private DartHighlightRegion(final int offset, final int length, @NotNull final String type) {
      super(offset, length);
      this.type = type.intern();
    }

    public String getType() {
      return type;
    }
  }

  public static class DartError extends DartRegion {
    @NotNull private final String myAnalysisErrorFileSD;
    @NotNull private final String mySeverity;
    @Nullable private final String myCode;
    @NotNull private final String myMessage;
    @Nullable private final String myCorrection;
    @Nullable private final String myUrl;

    private DartError(@NotNull final AnalysisError error, final int correctedOffset, final int correctedLength) {
      super(correctedOffset, correctedLength);
      myAnalysisErrorFileSD = error.getLocation().getFile().intern();
      mySeverity = error.getSeverity().intern();
      myCode = error.getCode() == null ? null : error.getCode().intern();
      myMessage = error.getMessage();
      myCorrection = error.getCorrection();
      myUrl = error.getUrl();
    }

    @NotNull
    public String getAnalysisErrorFileSD() {
      return myAnalysisErrorFileSD;
    }

    @NotNull
    public String getSeverity() {
      return mySeverity;
    }

    public boolean isError() {
      return mySeverity.equals(AnalysisErrorSeverity.ERROR);
    }

    @Nullable
    public String getCode() {
      return myCode;
    }

    @NotNull
    public String getMessage() {
      return myMessage;
    }

    @Nullable
    public String getCorrection() {
      return myCorrection;
    }

    @Nullable
    public String getUrl() {
      return myUrl;
    }
  }

  public static class DartNavigationRegion extends DartRegion {
    private final List<DartNavigationTarget> myTargets;

    DartNavigationRegion(final int offset, final int length, @NotNull final List<DartNavigationTarget> targets) {
      super(offset, length);
      myTargets = targets;
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
    private final int myOriginalOffset;
    private final String myKind;

    private int myConvertedOffset = -1;

    private DartNavigationTarget(@NotNull final NavigationTarget target) {
      myFile = FileUtil.toSystemIndependentName(target.getFile().trim()).intern();
      myOriginalOffset = target.getOffset();
      myKind = target.getKind().intern();
    }

    public String getFile() {
      return myFile;
    }

    public int getOffset(@NotNull final Project project, @Nullable final VirtualFile file) {
      if (myConvertedOffset == -1) {
        myConvertedOffset = DartAnalysisServerService.getInstance(project).getConvertedOffset(file, myOriginalOffset);
      }
      return myConvertedOffset;
    }

    public String getKind() {
      return myKind;
    }
  }

  public static class DartOverrideMember extends DartRegion {
    @Nullable private final OverriddenMember mySuperclassMember;
    @Nullable private final List<OverriddenMember> myInterfaceMembers;

    private DartOverrideMember(final int offset,
                               final int length,
                               @Nullable final OverriddenMember superclassMember,
                               @Nullable final List<OverriddenMember> interfaceMembers) {
      super(offset, length);
      mySuperclassMember = superclassMember;
      myInterfaceMembers = interfaceMembers;
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
