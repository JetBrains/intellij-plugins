package com.jetbrains.lang.dart.analyzer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import org.dartlang.analysis.server.protocol.HighlightRegion;
import org.dartlang.analysis.server.protocol.NavigationRegion;
import org.dartlang.analysis.server.protocol.NavigationTarget;
import org.dartlang.analysis.server.protocol.OverrideMember;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DartServerData {

  private DartServerRootsHandler myRootsHandler;

  private final Map<String, List<PluginHighlightRegion>> myHighlightData = Maps.newHashMap();
  private final Map<String, List<PluginNavigationRegion>> myNavigationData = Maps.newHashMap();
  private final Map<String, List<OverrideMember>> myOverrideData = Maps.newHashMap();

  private final Set<String> myFilePathsWithUnsentChanges = Sets.newConcurrentHashSet();

  DartServerData(@NotNull final DartServerRootsHandler rootsHandler) {
    myRootsHandler = rootsHandler;
  }

  public void computedHighlights(@NotNull final String filePath, @NotNull final List<HighlightRegion> regions) {
    if (myFilePathsWithUnsentChanges.contains(filePath)) return;

    final List<PluginHighlightRegion> pluginRegions = new ArrayList<PluginHighlightRegion>(regions.size());
    for (HighlightRegion region : regions) {
      if (region.getLength() > 0) {
        pluginRegions.add(new PluginHighlightRegion(region));
      }
    }

    synchronized (myHighlightData) {
      myHighlightData.put(filePath, pluginRegions);
    }

    forceFileAnnotation(filePath, false);
  }

  public void computedNavigation(@NotNull final String filePath, @NotNull final List<NavigationRegion> regions) {
    if (myFilePathsWithUnsentChanges.contains(filePath)) return;

    final List<PluginNavigationRegion> pluginRegions = new ArrayList<PluginNavigationRegion>(regions.size());
    for (NavigationRegion region : regions) {
      if (region.getLength() > 0) {
        pluginRegions.add(new PluginNavigationRegion(region));
      }
    }

    synchronized (myNavigationData) {
      myNavigationData.put(filePath, pluginRegions);
    }

    forceFileAnnotation(filePath, true);
  }

  public void computedOverrides(@NotNull final String filePath, @NotNull final List<OverrideMember> overrides) {
    // check myFilePathsWithUnsentChanges? update offset in documentListener?
    synchronized (myOverrideData) {
      myOverrideData.put(filePath, overrides);
    }

    forceFileAnnotation(filePath, false);
  }

  @NotNull
  public List<PluginHighlightRegion> getHighlight(@NotNull final VirtualFile file) {
    synchronized (myHighlightData) {
      final List<PluginHighlightRegion> regions = myHighlightData.get(file.getPath());
      if (regions == null) {
        return PluginHighlightRegion.EMPTY_LIST;
      }
      return regions;
    }
  }

  @NotNull
  public List<PluginNavigationRegion> getNavigation(@NotNull final VirtualFile file) {
    synchronized (myNavigationData) {
      final List<PluginNavigationRegion> regions = myNavigationData.get(file.getPath());
      if (regions == null) {
        return PluginNavigationRegion.EMPTY_LIST;
      }
      return regions;
    }
  }

  @NotNull
  public List<OverrideMember> getOverrideMembers(@NotNull final VirtualFile file) {
    synchronized (myOverrideData) {
      List<OverrideMember> regions = myOverrideData.get(file.getPath());
      if (regions == null) {
        return OverrideMember.EMPTY_LIST;
      }
      return regions;
    }
  }

  private void forceFileAnnotation(@NotNull final String filePath, final boolean clearCache) {
    final VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
    if (virtualFile != null) {
      Set<Project> projects = myRootsHandler.getTrackedProjects();
      for (final Project project : projects) {
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
    synchronized (myHighlightData) {
      myHighlightData.remove(file.getPath());
    }
    synchronized (myNavigationData) {
      myNavigationData.remove(file.getPath());
    }
    synchronized (myOverrideData) {
      myOverrideData.remove(file.getPath());
    }
  }

  public void onFlushedResults(@NotNull final List<String> filePaths) {
    if (!myHighlightData.isEmpty()) {
      synchronized (myHighlightData) {
        for (String path : filePaths) {
          myHighlightData.remove(FileUtil.toSystemIndependentName(path));
        }
      }
    }
    if (!myNavigationData.isEmpty()) {
      synchronized (myNavigationData) {
        for (String path : filePaths) {
          myNavigationData.remove(FileUtil.toSystemIndependentName(path));
        }
      }
    }
    if (!myOverrideData.isEmpty()) {
      synchronized (myOverrideData) {
        for (String path : filePaths) {
          myOverrideData.remove(FileUtil.toSystemIndependentName(path));
        }
      }
    }
  }

  void onDocumentChanged(@NotNull final DocumentEvent e) {
    final VirtualFile file = FileDocumentManager.getInstance().getFile(e.getDocument());
    if (!DartAnalysisServerService.isLocalDartOrHtmlFile(file)) return;

    final String filePath = file.getPath();
    myFilePathsWithUnsentChanges.add(filePath);

    // navigation region must be deleted if touched by editing and updated otherwise
    synchronized (myNavigationData) {
      final List<PluginNavigationRegion> regions = myNavigationData.get(filePath);
      if (regions != null) {
        final int eventOffset = e.getOffset();
        final int deltaLength = e.getNewLength() - e.getOldLength();

        final Iterator<PluginNavigationRegion> iterator = regions.iterator();
        while (iterator.hasNext()) {
          final PluginNavigationRegion region = iterator.next();

          // may be we'd better delete target touched by editing?
          for (PluginNavigationTarget target : region.getTargets()) {
            if (target.file.equals(filePath) && target.offset >= eventOffset) {
              target.offset += deltaLength;
            }
          }

          if (deltaLength > 0) {
            // Something was typed. Shift untouched regions, delete touched.
            if (eventOffset <= region.offset) {
              region.offset += deltaLength;
            }
            else if (region.offset < eventOffset && eventOffset < region.offset + region.length) {
              iterator.remove();
            }
          }
          else if (deltaLength < 0) {
            // Some text was deleted. Shift untouched regions, delete touched.
            final int eventRightOffset = eventOffset - deltaLength;

            if (eventRightOffset <= region.offset) {
              region.offset += deltaLength;
            }
            else if (eventOffset < region.offset + region.length) {
              iterator.remove();
            }
          }
        }
      }
    }

    synchronized (myHighlightData) {
      final List<PluginHighlightRegion> regions = myHighlightData.get(filePath);
      if (regions != null) {
        final int eventOffset = e.getOffset();
        final int deltaLength = e.getNewLength() - e.getOldLength();

        final Iterator<PluginHighlightRegion> iterator = regions.iterator();
        while (iterator.hasNext()) {
          final PluginHighlightRegion region = iterator.next();

          if (deltaLength > 0) {
            // Something was typed. Shift untouched regions, update touched.
            if (eventOffset <= region.offset) {
              region.offset += deltaLength;
            }
            else if (region.offset < eventOffset && eventOffset < region.offset + region.length) {
              region.length += deltaLength;
            }
          }
          else if (deltaLength < 0) {
            // Some text was deleted. Shift untouched regions, delete or update touched.
            final int eventRightOffset = eventOffset - deltaLength;
            final int regionRightOffset = region.offset + region.length;

            if (eventRightOffset <= region.offset) {
              region.offset += deltaLength;
            }
            else if (region.offset <= eventOffset && eventRightOffset <= regionRightOffset && region.length != -deltaLength) {
              region.length += deltaLength;
            }
            else if (eventOffset < regionRightOffset) {
              iterator.remove();
            }
          }
        }
      }
    }
  }


  public interface PluginRegion {
    int getOffset();

    int getLength();
  }

  public static class PluginHighlightRegion implements PluginRegion {
    public static final List<PluginHighlightRegion> EMPTY_LIST = Lists.newArrayList();

    private int offset;
    private int length;
    private final String type;

    private PluginHighlightRegion(HighlightRegion region) {
      offset = region.getOffset();
      length = region.getLength();
      type = region.getType();
    }

    public int getOffset() {
      return offset;
    }

    public int getLength() {
      return length;
    }

    public String getType() {
      return type;
    }
  }

  public static class PluginNavigationRegion implements PluginRegion {
    public static final List<PluginNavigationRegion> EMPTY_LIST = Lists.newArrayList();

    private int offset;
    private int length;
    private final List<PluginNavigationTarget> targets = Lists.newArrayList();

    private PluginNavigationRegion(NavigationRegion region) {
      offset = region.getOffset();
      length = region.getLength();
      for (NavigationTarget target : region.getTargetObjects()) {
        targets.add(new PluginNavigationTarget(target));
      }
    }

    @Override
    public String toString() {
      return "PluginNavigationRegion(" + offset + ", " + length + ")";
    }

    public int getOffset() {
      return offset;
    }

    public int getLength() {
      return length;
    }

    public List<PluginNavigationTarget> getTargets() {
      return targets;
    }
  }

  public static class PluginNavigationTarget {
    private final String file;
    private int offset;
    private final String kind;

    private PluginNavigationTarget(NavigationTarget target) {
      file = FileUtil.toSystemIndependentName(target.getFile());
      offset = target.getOffset();
      kind = target.getKind();
    }

    public String getFile() {
      return file;
    }

    public int getOffset() {
      return offset;
    }

    public String getKind() {
      return kind;
    }
  }
}
