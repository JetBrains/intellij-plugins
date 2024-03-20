// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.analyzer;

import com.google.common.collect.Sets;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.EventDispatcher;
import com.intellij.util.SmartList;
import org.dartlang.analysis.server.protocol.*;
import org.jetbrains.annotations.*;

import java.util.*;

public final class DartServerData {
  public interface OutlineListener extends EventListener {
    void outlineUpdated(@NotNull DartFileInfo fileInfo);
  }

  private final DartAnalysisServerService myService;

  private final EventDispatcher<OutlineListener> myEventDispatcher = EventDispatcher.create(OutlineListener.class);

  private final Map<DartFileInfo, List<DartError>> myErrorData = Collections.synchronizedMap(new HashMap<>());
  private final Map<DartFileInfo, List<DartHighlightRegion>> myHighlightData = Collections.synchronizedMap(new HashMap<>());
  private final Map<DartFileInfo, List<DartNavigationRegion>> myNavigationData = Collections.synchronizedMap(new HashMap<>());
  private final Map<DartFileInfo, List<DartOverrideMember>> myOverrideData = Collections.synchronizedMap(new HashMap<>());
  private final Map<DartFileInfo, List<DartRegion>> myImplementedClassData = Collections.synchronizedMap(new HashMap<>());
  private final Map<DartFileInfo, List<DartRegion>> myImplementedMemberData = Collections.synchronizedMap(new HashMap<>());
  private final Map<DartFileInfo, Outline> myOutlineData = Collections.synchronizedMap(new HashMap<>());

  private final Map<Integer, AvailableSuggestionSet> myAvailableSuggestionSetMap = Collections.synchronizedMap(new HashMap<>());
  private final Map<String, Map<String, Map<String, Set<String>>>> myExistingImports = Collections.synchronizedMap(new HashMap<>());

  private final Set<DartLocalFileInfo> myLocalFilesWithUnsentChanges = Sets.newConcurrentHashSet();

  // keeps track of files in which error regions have been updated by DocumentListener
  private final Set<DartLocalFileInfo> myLocalFilesWithInaccurateErrorInfo = Sets.newConcurrentHashSet();

  DartServerData(@NotNull DartAnalysisServerService service) {
    myService = service;
  }

  boolean isErrorInfoInaccurate(@NotNull DartLocalFileInfo fileInfo) {
    return myLocalFilesWithInaccurateErrorInfo.contains(fileInfo);
  }

  /**
   * @return {@code true} if {@code errors} were processes, {@code false} if ignored;
   * errors are ignored if the file has been edited and new contents has not yet been sent to the server.
   */
  boolean computedErrors(@NotNull DartFileInfo fileInfo, @NotNull List<? extends AnalysisError> errors, boolean restartHighlighting) {
    if (myLocalFilesWithUnsentChanges.contains(fileInfo)) return false;

    List<DartError> newErrors = new ArrayList<>(errors.size());
    VirtualFile file = fileInfo instanceof DartLocalFileInfo localFileInfo ? localFileInfo.findFile() : null;

    for (AnalysisError error : errors) {
      final int offset = myService.getConvertedOffset(file, error.getLocation().getOffset());
      final int length = myService.getConvertedOffset(file, error.getLocation().getOffset() + error.getLocation().getLength()) - offset;
      newErrors.add(new DartError(error, offset, length));
    }

    if (fileInfo instanceof DartLocalFileInfo) {
      myLocalFilesWithInaccurateErrorInfo.remove(fileInfo);
    }
    myErrorData.put(fileInfo, newErrors);

    if (restartHighlighting) {
      forceFileAnnotation(file, false);
    }

    return true;
  }

  void computedClosingLabels(@NotNull DartFileInfo fileInfo, @NotNull List<ClosingLabel> labels) {
    if (myLocalFilesWithUnsentChanges.contains(fileInfo)) return;

    DartClosingLabelManager.getInstance().computedClosingLabels(myService.getProject(), fileInfo, labels);
  }

  void computedHighlights(@NotNull DartFileInfo fileInfo, @NotNull List<? extends HighlightRegion> regions) {
    if (myLocalFilesWithUnsentChanges.contains(fileInfo)) return;

    List<DartHighlightRegion> newRegions = new ArrayList<>(regions.size());
    VirtualFile file = fileInfo instanceof DartLocalFileInfo localFileInfo ? localFileInfo.findFile() : null;

    for (HighlightRegion region : regions) {
      if (region.getLength() > 0) {
        final int offset = myService.getConvertedOffset(file, region.getOffset());
        final int length = myService.getConvertedOffset(file, region.getOffset() + region.getLength()) - offset;
        newRegions.add(new DartHighlightRegion(offset, length, region.getType()));
      }
    }

    myHighlightData.put(fileInfo, newRegions);
    forceFileAnnotation(file, false);
  }

  void computedNavigation(@NotNull DartFileInfo fileInfo, @NotNull List<? extends NavigationRegion> regions) {
    if (myLocalFilesWithUnsentChanges.contains(fileInfo)) return;

    List<DartNavigationRegion> newRegions = new ArrayList<>(regions.size());
    VirtualFile file = fileInfo instanceof DartLocalFileInfo localFileInfo ? localFileInfo.findFile() : null;

    for (NavigationRegion region : regions) {
      if (region.getLength() > 0) {
        final DartNavigationRegion dartNavigationRegion = createDartNavigationRegion(myService, file, region);
        newRegions.add(dartNavigationRegion);
      }
    }

    myNavigationData.put(fileInfo, newRegions);
    forceFileAnnotation(file, true);
  }

  void computedOutline(@NotNull DartFileInfo fileInfo, @NotNull Outline outline) {
    if (myLocalFilesWithUnsentChanges.contains(fileInfo)) return;

    myOutlineData.put(fileInfo, outline);
    ApplicationManager.getApplication().invokeLater(() -> myEventDispatcher.getMulticaster().outlineUpdated(fileInfo),
                                                    ModalityState.nonModal(),
                                                    myService.getDisposedCondition());
  }

  void computedAvailableSuggestions(final @NotNull List<? extends AvailableSuggestionSet> changed, final int @NotNull [] removed) {
    for (int id : removed) {
      myAvailableSuggestionSetMap.remove(id);
    }
    for (AvailableSuggestionSet suggestionSet : changed) {
      myAvailableSuggestionSetMap.put(suggestionSet.getId(), suggestionSet);
    }
  }

  void computedExistingImports(@NotNull String filePathSD, @NotNull Map<String, Map<String, Set<String>>> existingImports) {
    if (existingImports.isEmpty()) {
      myExistingImports.remove(filePathSD);
      return;
    }

    myExistingImports.put(filePathSD, existingImports);
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

  void computedOverrides(@NotNull DartFileInfo fileInfo, @NotNull List<? extends OverrideMember> overrides) {
    if (myLocalFilesWithUnsentChanges.contains(fileInfo)) return;

    final List<DartOverrideMember> newOverrides = new ArrayList<>(overrides.size());
    VirtualFile file = fileInfo instanceof DartLocalFileInfo localFileInfo ? localFileInfo.findFile() : null;

    for (OverrideMember override : overrides) {
      if (override.getLength() > 0) {
        final int offset = myService.getConvertedOffset(file, override.getOffset());
        final int length = myService.getConvertedOffset(file, override.getOffset() + override.getLength()) - offset;
        newOverrides.add(new DartOverrideMember(offset, length, override.getSuperclassMember(), override.getInterfaceMembers()));
      }
    }

    myOverrideData.put(fileInfo, newOverrides);
    forceFileAnnotation(file, false);
  }

  void computedImplemented(@NotNull DartFileInfo fileInfo,
                           @NotNull List<? extends ImplementedClass> implementedClasses,
                           @NotNull List<? extends ImplementedMember> implementedMembers) {
    if (myLocalFilesWithUnsentChanges.contains(fileInfo)) return;

    VirtualFile file = fileInfo instanceof DartLocalFileInfo localFileInfo ? localFileInfo.findFile() : null;

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
    List<DartRegion> oldClasses = myImplementedClassData.get(fileInfo);
    if (oldClasses == null || !oldClasses.equals(newImplementedClasses)) {
      hasChanges = true;
      myImplementedClassData.put(fileInfo, newImplementedClasses);
    }

    List<DartRegion> oldMembers = myImplementedMemberData.get(fileInfo);
    if (oldMembers == null || !oldMembers.equals(newImplementedMembers)) {
      hasChanges = true;
      myImplementedMemberData.put(fileInfo, newImplementedMembers);
    }

    if (hasChanges) {
      forceFileAnnotation(file, false);
    }
  }

  @NotNull
  List<DartError> getErrors(@NotNull final SearchScope scope) {
    final List<DartError> errors = new ArrayList<>();

    synchronized (myErrorData) {
      for (Map.Entry<DartFileInfo, List<DartError>> entry : myErrorData.entrySet()) {
        DartFileInfo fileInfo = entry.getKey();
        VirtualFile file = fileInfo instanceof DartLocalFileInfo localFileInfo ? localFileInfo.findFile() : null;
        if (file != null && scope.contains(file)) {
          errors.addAll(entry.getValue());
        }
      }
    }

    return errors;
  }

  @NotNull
  List<DartError> getErrors(@NotNull VirtualFile file) {
    DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(file);
    List<DartError> errors = myErrorData.get(fileInfo);
    return errors != null ? errors : Collections.emptyList();
  }

  @NotNull
  List<DartHighlightRegion> getHighlight(@NotNull VirtualFile file) {
    DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(file);
    List<DartHighlightRegion> regions = myHighlightData.get(fileInfo);
    return regions != null ? regions : Collections.emptyList();
  }

  @NotNull
  List<DartNavigationRegion> getNavigation(@NotNull VirtualFile file) {
    DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(file);
    List<DartNavigationRegion> regions = myNavigationData.get(fileInfo);
    return regions != null ? regions : Collections.emptyList();
  }

  @NotNull
  List<DartOverrideMember> getOverrideMembers(@NotNull VirtualFile file) {
    DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(file);
    List<DartOverrideMember> regions = myOverrideData.get(fileInfo);
    return regions != null ? regions : Collections.emptyList();
  }

  @NotNull
  List<DartRegion> getImplementedClasses(@NotNull VirtualFile file) {
    DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(file);
    List<DartRegion> classes = myImplementedClassData.get(fileInfo);
    return classes != null ? classes : Collections.emptyList();
  }

  @NotNull
  List<DartRegion> getImplementedMembers(@NotNull VirtualFile file) {
    DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(file);
    List<DartRegion> classes = myImplementedMemberData.get(fileInfo);
    return classes != null ? classes : Collections.emptyList();
  }

  @Nullable
  Outline getOutline(@NotNull VirtualFile file) {
    DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(file);
    return myOutlineData.get(fileInfo);
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

  boolean hasAllData_TESTS_ONLY(@NotNull VirtualFile file) {
    assert ApplicationManager.getApplication().isUnitTestMode();

    DartLocalFileInfo fileInfo = (DartLocalFileInfo)DartFileInfoKt.getDartFileInfo(file);
    return !isErrorInfoInaccurate(fileInfo) &&
           myHighlightData.get(fileInfo) != null &&
           myNavigationData.get(fileInfo) != null &&
           myOverrideData.get(fileInfo) != null &&
           myImplementedClassData.get(fileInfo) != null &&
           myImplementedMemberData.get(fileInfo) != null &&
           myOutlineData.get(fileInfo) != null;
  }

  @Nullable
  Map<String, Map<String, Set<String>>> getExistingImports(@Nullable String filePathSD) {
    if (filePathSD == null) return null;
    return myExistingImports.get(filePathSD);
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
                         PsiManager.getInstance(project).dropPsiCaches();
                       }
                       DaemonCodeAnalyzer.getInstance(project).restart();
                     },
                     ModalityState.nonModal(),
                     myService.getDisposedCondition());
    }
  }

  void onFilesContentUpdated() {
    myLocalFilesWithUnsentChanges.clear();
  }

  void onFileClosed(@NotNull final VirtualFile file) {
    DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(file);
    if (!(fileInfo instanceof DartLocalFileInfo localFileInfo)) return;

    // do not remove from myErrorData, this map is always kept up-to-date for all files, not only for visible
    myHighlightData.remove(localFileInfo);
    myNavigationData.remove(localFileInfo);
    myOverrideData.remove(localFileInfo);
    myImplementedClassData.remove(localFileInfo);
    myImplementedMemberData.remove(localFileInfo);
    myOutlineData.remove(localFileInfo);
  }

  void onFlushedResults(@NotNull List<DartFileInfo> fileInfos) {
    removeAllFromMap(myErrorData, fileInfos);
    removeAllFromMap(myHighlightData, fileInfos);
    removeAllFromMap(myNavigationData, fileInfos);
    removeAllFromMap(myOverrideData, fileInfos);
    removeAllFromMap(myImplementedClassData, fileInfos);
    removeAllFromMap(myImplementedMemberData, fileInfos);
    removeAllFromMap(myOutlineData, fileInfos);
  }

  private static <T> void removeAllFromMap(@NotNull Map<T, ?> map, @NotNull List<? extends T> keys) {
    if (map.isEmpty()) return;

    for (T key : keys) {
      map.remove(key);
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

  void onDocumentChanged(@NotNull DocumentEvent e) {
    VirtualFile file = FileDocumentManager.getInstance().getFile(e.getDocument());
    if (!DartAnalysisServerService.isLocalAnalyzableFile(file)) return;

    DartFileInfo fileInfo = DartFileInfoKt.getDartFileInfo(file);
    if (!(fileInfo instanceof DartLocalFileInfo localFileInfo)) return;

    myLocalFilesWithUnsentChanges.add(localFileInfo);

    boolean regionsUpdated = updateRegionsDeletingTouched(localFileInfo, myErrorData.get(localFileInfo), e);
    if (regionsUpdated) {
      myLocalFilesWithInaccurateErrorInfo.add(localFileInfo);
    }
    updateRegionsUpdatingTouched(myHighlightData.get(localFileInfo), e);
    updateRegionsDeletingTouched(localFileInfo, myNavigationData.get(localFileInfo), e);
    updateRegionsDeletingTouched(localFileInfo, myOverrideData.get(localFileInfo), e);
    updateRegionsDeletingTouched(localFileInfo, myImplementedClassData.get(localFileInfo), e);
    updateRegionsDeletingTouched(localFileInfo, myImplementedMemberData.get(localFileInfo), e);
    // A bit outdated outline data is not a big problem, updated data will come shortly
  }

  /**
   * @return {@code true} if at least one region has been updated or deleted, {@code false} if nothing done at all
   */
  private static boolean updateRegionsDeletingTouched(@NotNull final DartFileInfo fileInfo,
                                                      @Nullable final List<? extends DartRegion> regions,
                                                      @NotNull final DocumentEvent e) {
    if (regions == null) return false;

    boolean regionUpdated = false;

    // delete touched regions, shift untouched
    final int eventOffset = e.getOffset();
    final int deltaLength = e.getNewLength() - e.getOldLength();

    final Iterator<? extends DartRegion> iterator = regions.iterator();
    while (iterator.hasNext()) {
      final DartRegion region = iterator.next();

      if (region instanceof DartNavigationRegion) {
        // may be we'd better delete target touched by editing?
        for (DartNavigationTarget target : ((DartNavigationRegion)region).getTargets()) {
          if (target.myFileInfo.equals(fileInfo) && target.myConvertedOffset >= eventOffset) {
            target.myConvertedOffset += deltaLength;
          }
        }
      }

      if (deltaLength > 0) {
        // Something was typed. Shift untouched regions, delete touched.
        if (eventOffset <= region.myOffset) {
          region.myOffset += deltaLength;
          regionUpdated = true;
        }
        else if (eventOffset < region.myOffset + region.myLength) {
          iterator.remove();
          regionUpdated = true;
        }
      }
      else if (deltaLength < 0) {
        // Some text was deleted. Shift untouched regions, delete touched.
        final int eventRightOffset = eventOffset - deltaLength;

        if (eventRightOffset <= region.myOffset) {
          region.myOffset += deltaLength;
          regionUpdated = true;
        }
        else if (eventOffset < region.myOffset + region.myLength) {
          iterator.remove();
          regionUpdated = true;
        }
      }
    }

    return regionUpdated;
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
        else if (eventOffset < region.myOffset + region.myLength) {
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

  public static final class DartHighlightRegion extends DartRegion {
    private final String type;

    private DartHighlightRegion(final int offset, final int length, @NotNull final String type) {
      super(offset, length);
      this.type = type.intern();
    }

    public String getType() {
      return type;
    }
  }

  public static final class DartError extends DartRegion {
    private final @NotNull @NonNls String mySeverity;
    private final @Nullable @NonNls String myCode;
    private final @NotNull @NlsSafe String myMessage;
    private final @Nullable @NlsSafe String myCorrection;
    private final @Nullable List<DiagnosticMessage> myContextMessages;
    private final @Nullable @NonNls String myUrl;

    @Contract(pure = true)
    public @NotNull DartError asEofError(int fileLength) {
      return new DartError(fileLength > 0 ? fileLength - 1 : 0,
                           fileLength > 0 ? 1 : 0,
                           mySeverity, myCode, myMessage, myContextMessages, myCorrection, myUrl);
    }

    private DartError(@NotNull AnalysisError error, int correctedOffset, int correctedLength) {
      super(correctedOffset, correctedLength);
      mySeverity = error.getSeverity().intern();
      myCode = error.getCode() == null ? null : error.getCode().intern();
      myMessage = error.getMessage();
      myContextMessages = error.getContextMessages();
      myCorrection = error.getCorrection();
      myUrl = error.getUrl();
    }

    private DartError(int offset,
                      int length,
                      @NotNull @NonNls String severity,
                      @Nullable @NonNls String code,
                      @NotNull @Nls String message,
                      @Nullable List<DiagnosticMessage> contextMessages,
                      @Nullable @Nls String correction,
                      @Nullable @NonNls String url) {
      super(offset, length);
      mySeverity = severity;
      myCode = code;
      myMessage = message;
      myContextMessages = contextMessages;
      myCorrection = correction;
      myUrl = url;
    }

    public @NotNull @NonNls String getSeverity() {
      return mySeverity;
    }

    public boolean isError() {
      return mySeverity.equals(AnalysisErrorSeverity.ERROR);
    }

    public @Nullable @NonNls String getCode() {
      return myCode;
    }

    public @NotNull @NlsSafe String getMessage() {
      return myMessage;
    }

    public @Nullable @NlsSafe String getCorrection() {
      return myCorrection;
    }

    public @Nullable List<DiagnosticMessage> getContextMessages() {
      return myContextMessages;
    }

    public @Nullable @NonNls String getUrl() {
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

  public static final class DartNavigationTarget {
    private final DartFileInfo myFileInfo;
    private final int myOriginalOffset;
    private final String myKind;

    private int myConvertedOffset = -1;

    private DartNavigationTarget(@NotNull final NavigationTarget target) {
      String filePathOrUri = target.getFile().trim();
      myFileInfo = DartFileInfoKt.getDartFileInfo(filePathOrUri);
      myOriginalOffset = target.getOffset();
      myKind = target.getKind().intern();
    }

    public @Nullable VirtualFile findFile() {
      if (myFileInfo instanceof DartLocalFileInfo localFileInfo) {
        return localFileInfo.findFile();
      }
      return null;
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

  public static final class DartOverrideMember extends DartRegion {
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
