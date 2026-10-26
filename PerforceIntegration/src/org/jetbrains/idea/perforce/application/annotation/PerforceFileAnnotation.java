/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.perforce.application.annotation;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.VcsKey;
import com.intellij.openapi.vcs.annotate.AnnotationTooltipBuilder;
import com.intellij.openapi.vcs.annotate.DefaultLineModificationDetailsProvider;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspect;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspectAdapter;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.ChangeListData;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.actions.ShowAllSubmittedFilesAction;
import org.jetbrains.idea.perforce.application.PerforceFileRevision;
import org.jetbrains.idea.perforce.application.PerforceOnlyRevisionNumber;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.application.PerforceVcsRevisionNumber;
import org.jetbrains.idea.perforce.perforce.P4Revision;
import org.jetbrains.idea.perforce.perforce.PerforceChangeCache;
import org.jetbrains.idea.perforce.perforce.PerforceChangeList;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PerforceFileAnnotation extends FileAnnotation {
  private final AnnotationInfo myAnnotationInfo;

  private final Map<Long, ChangeListData> myIntegratedChanges;
  private final VirtualFile myFile;
  private final long myRevision;

  private final Project myProject;
  private final P4Connection myConnection;
  private final List<VcsFileRevision> myPerforceRevisions;

  private final Map<Long, P4Revision> myRevisionsByNumber = new HashMap<>();
  private final Map<Long, LineChange> myChangesByNumber = new HashMap<>();


  public PerforceFileAnnotation(final AnnotationInfo annotationInfo,
                                final VirtualFile file,
                                P4Revision[] revisions,
                                @NotNull Map<Long, ChangeListData> integratedChanges,
                                final Project project, final @NotNull P4Connection connection, long revision) {
    super(project);
    myAnnotationInfo = annotationInfo;
    myIntegratedChanges = integratedChanges;
    myProject = project;
    myConnection = connection;
    myFile = file;
    myRevision = revision;

    myPerforceRevisions = new ArrayList<>();
    for (P4Revision p4Revision : revisions) {
      myPerforceRevisions.add(new PerforceFileRevision(p4Revision, connection, project));
    }
    myPerforceRevisions.sort((o1, o2) -> -1 * o1.getRevisionNumber().compareTo(o2.getRevisionNumber()));

    for (P4Revision p4Revision : revisions) {
      long number = myAnnotationInfo.isUseChangelistNumbers() ? p4Revision.getChangeNumber() : p4Revision.getRevisionNumber();
      if (myRevisionsByNumber.putIfAbsent(number, p4Revision) == null) {
        myChangesByNumber.put(number, new LineChange(p4Revision.getChangeNumber(), p4Revision.getUser(), p4Revision.getDate(),
                                                     p4Revision.getSubmitMessage()));
      }
    }
    if (myAnnotationInfo.isFollowIntegrations()) {
      for (ChangeListData data : myIntegratedChanges.values()) {
        myChangesByNumber.putIfAbsent(data.NUMBER, new LineChange(data.NUMBER, data.USER, ChangeListData.parseChangelistDate(data.DATE),
                                                                  data.DESCRIPTION));
      }
    }
  }

  private final LineAnnotationAspect REVISION =
    new PerforceAnnotationAspect(LineAnnotationAspect.REVISION, VcsBundle.message("line.annotation.aspect.revision"), false) {
      @Override
      public String getValue(int lineNumber) {
        LineChange change = findChangeForLine(lineNumber);
        if (change != null) {
          return String.valueOf(change.number());
        }
        else {
          return "";
        }
      }
    };

  private final LineAnnotationAspect CLIENT =
    new PerforceAnnotationAspect(LineAnnotationAspect.AUTHOR, VcsBundle.message("line.annotation.aspect.author"), true) {
      @Override
      public String getValue(int lineNumber) {
        LineChange change = findChangeForLine(lineNumber);
        if (change != null) {
          return change.user();
        }
        else {
          return "";
        }
      }
    };

  private final LineAnnotationAspect DATE =
    new PerforceAnnotationAspect(LineAnnotationAspect.DATE, VcsBundle.message("line.annotation.aspect.date"), true) {
      @Override
      public String getValue(int lineNumber) {
        LineChange change = findChangeForLine(lineNumber);
        if (change != null) {
          return formatDate(change.date());
        }
        else {
          return "";
        }
      }
    };

  @Override
  public LineAnnotationAspect @NotNull [] getAspects() {
    return new LineAnnotationAspect[]{REVISION, DATE, CLIENT};
  }

  @Override
  public @Nullable String getToolTip(int lineNumber) {
    return getToolTip(lineNumber, false);
  }

  @Override
  public @Nullable String getHtmlToolTip(int lineNumber) {
    return getToolTip(lineNumber, true);
  }

  private @Nullable @NlsContexts.Tooltip String getToolTip(int lineNumber, boolean asHtml) {
    LineChange change = findChangeForLine(lineNumber);
    if (change == null) return null;

    return AnnotationTooltipBuilder.buildSimpleTooltip(getProject(), asHtml, PerforceBundle.message("file.revision"),
                                                       String.valueOf(change.number()),
                                                       change.description());
  }

  @Override
  public String getAnnotatedContent() {
    return myAnnotationInfo.getContent();
  }

  private @Nullable P4Revision findRevisionForLine(final int lineNumber) {
    return myRevisionsByNumber.get(myAnnotationInfo.getRevision(lineNumber));
  }

  @VisibleForTesting
  public @Nullable LineChange findChangeForLine(int lineNumber) {
    return myChangesByNumber.get(myAnnotationInfo.getRevision(lineNumber));
  }

  private @Nullable ChangeListData findIntegratedChangeForLine(int lineNumber) {
    if (!myAnnotationInfo.isFollowIntegrations()) return null;
    return myIntegratedChanges.get(myAnnotationInfo.getRevision(lineNumber));
  }

  @VisibleForTesting
  public record LineChange(long number, @NlsSafe String user, Date date, @NlsSafe String description) {
  }

  @Override
  public VcsRevisionNumber getLineRevisionNumber(final int lineNumber) {
    P4Revision p4Revision = findRevisionForLine(lineNumber);
    if (p4Revision != null) {
      return new PerforceVcsRevisionNumber(p4Revision);
    }
    ChangeListData data = findIntegratedChangeForLine(lineNumber);
    return data == null ? null : new PerforceOnlyRevisionNumber(data.NUMBER);
  }

  @Override
  public @Nullable Date getLineDate(int lineNumber) {
    LineChange change = findChangeForLine(lineNumber);
    return change == null ? null : change.date();
  }

  @Override
  public @NotNull VcsRevisionNumber getCurrentRevision() {
    return new PerforceOnlyRevisionNumber(myRevision);
  }

  @Override
  public List<VcsFileRevision> getRevisions() {
    return myPerforceRevisions;
  }

  @Override
  public int getLineCount() {
    return myAnnotationInfo.getLineCount();
  }

  // Override default provider to use changes from myIntegratedChanges
  @Override
  public @Nullable RevisionChangesProvider getRevisionsChangesProvider() {
    RevisionChangesProvider defaultProvider = super.getRevisionsChangesProvider();
    if (defaultProvider == null || myIntegratedChanges.isEmpty()) return defaultProvider;
    return lineNumber -> {
      ChangeListData data = findRevisionForLine(lineNumber) == null ? findIntegratedChangeForLine(lineNumber) : null;
      if (data == null) return defaultProvider.getChangesIn(lineNumber);
      PerforceChangeList changeList = new PerforceChangeList(data, myProject, myConnection, new PerforceChangeCache(myProject));
      // pre-load changes to avoid doing it on EDT
      changeList.getChanges();
      return Pair.create(changeList, VcsUtil.getFilePath(myFile));
    };
  }

  private abstract class PerforceAnnotationAspect extends LineAnnotationAspectAdapter {
    PerforceAnnotationAspect(String id, @NlsContexts.ListItem String displayName, boolean showByDefault) {
      super(id, displayName, showByDefault);
    }

    @Override
    protected void showAffectedPaths(int lineNum) {
      LineChange change = findChangeForLine(lineNum);
      P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(myFile);
      if (change != null && connection != null) {
        ShowAllSubmittedFilesAction.showAllSubmittedFiles(myProject, change.number(),
                                                          change.description(),
                                                          change.date(),
                                                          change.user(),
                                                          connection);
      }
    }
  }

  @Override
  public VcsKey getVcsKey() {
    return PerforceVcs.getKey();
  }

  @Override
  public boolean isBaseRevisionChanged(@NotNull VcsRevisionNumber number) {
    return PerforceVcs.revisionsSame(getCurrentRevision(), number);
  }

  @Override
  public VirtualFile getFile() {
    return myFile;
  }

  @Override
  public @Nullable LineModificationDetailsProvider getLineModificationDetailsProvider() {
    return DefaultLineModificationDetailsProvider.create(this);
  }
}
