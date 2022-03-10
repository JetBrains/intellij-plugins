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
import com.intellij.openapi.vcs.VcsBundle;
import com.intellij.openapi.vcs.VcsKey;
import com.intellij.openapi.vcs.annotate.*;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.actions.ShowAllSubmittedFilesAction;
import org.jetbrains.idea.perforce.application.PerforceFileRevision;
import org.jetbrains.idea.perforce.application.PerforceOnlyRevisionNumber;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.application.PerforceVcsRevisionNumber;
import org.jetbrains.idea.perforce.perforce.P4Revision;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PerforceFileAnnotation extends FileAnnotation {
  private final AnnotationInfo myAnnotationInfo;
  private final P4Revision[] myRevisions;
  private final VirtualFile myFile;
  private final long myRevision;

  private final Project myProject;
  private final List<VcsFileRevision> myPerforceRevisions;


  public PerforceFileAnnotation(final AnnotationInfo annotationInfo,
                                final VirtualFile file,
                                P4Revision[] revisions,
                                final Project project, @NotNull final P4Connection connection, long revision) {
    super(project);
    myAnnotationInfo = annotationInfo;
    myRevisions = revisions;
    myProject = project;
    myFile = file;
    myRevision = revision;

    myPerforceRevisions = new ArrayList<>();
    for (P4Revision p4Revision : myRevisions) {
      myPerforceRevisions.add(new PerforceFileRevision(p4Revision, connection, project));
    }
    myPerforceRevisions.sort((o1, o2) -> -1 * o1.getRevisionNumber().compareTo(o2.getRevisionNumber()));
  }

  private final LineAnnotationAspect REVISION =
    new PerforceAnnotationAspect(LineAnnotationAspect.REVISION, VcsBundle.message("line.annotation.aspect.revision"), false) {
      @Override
      public String getValue(int lineNumber) {
        P4Revision p4Revision = findRevisionForLine(lineNumber);
        if (p4Revision != null) {
          return String.valueOf(p4Revision.getChangeNumber());
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
        P4Revision p4Revision = findRevisionForLine(lineNumber);
        if (p4Revision != null) {
          return p4Revision.getUser();
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
        P4Revision p4Revision = findRevisionForLine(lineNumber);
        if (p4Revision != null) {
          return FileAnnotation.formatDate(p4Revision.getDate());
        }
        else {
          return "";
        }
      }
    };

  @Override
  public void dispose() {
  }

  @Override
  public LineAnnotationAspect @NotNull [] getAspects() {
    return new LineAnnotationAspect[]{REVISION, DATE, CLIENT};
  }

  @Nullable
  @Override
  public String getToolTip(int lineNumber) {
    return getToolTip(lineNumber, false);
  }

  @Nullable
  @Override
  public String getHtmlToolTip(int lineNumber) {
    return getToolTip(lineNumber, true);
  }

  @Nullable
  private @NlsContexts.Tooltip String getToolTip(int lineNumber, boolean asHtml) {
    P4Revision revision = findRevisionForLine(lineNumber);
    if (revision == null) return null;

    return AnnotationTooltipBuilder.buildSimpleTooltip(getProject(), asHtml, PerforceBundle.message("file.revision"),
                                                       String.valueOf(revision.getChangeNumber()),
                                                       revision.getSubmitMessage());
  }

  @Override
  public String getAnnotatedContent() {
    return myAnnotationInfo.getContent();
  }

  @Nullable
  @VisibleForTesting
  public P4Revision findRevisionForLine(final int lineNumber) {
    final long revision = myAnnotationInfo.getRevision(lineNumber);
    if (revision == -1) return null;
    for (P4Revision p4Revision : myRevisions) {
      if (myAnnotationInfo.isUseChangelistNumbers()) {
        if (p4Revision.getChangeNumber() == revision) return p4Revision;
      }
      else {
        if (p4Revision.getRevisionNumber() == revision) return p4Revision;
      }
    }
    return null;
  }

  @Override
  public VcsRevisionNumber getLineRevisionNumber(final int lineNumber) {
    P4Revision p4Revision = findRevisionForLine(lineNumber);
    if (p4Revision != null) {
      return new PerforceVcsRevisionNumber(p4Revision);
    }
    return null;
  }

  @Nullable
  @Override
  public Date getLineDate(int lineNumber) {
    P4Revision p4Revision = findRevisionForLine(lineNumber);
    return p4Revision == null ? null : p4Revision.getDate();
  }

  @NotNull
  @Override
  public VcsRevisionNumber getCurrentRevision() {
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

  private abstract class PerforceAnnotationAspect extends LineAnnotationAspectAdapter {
    PerforceAnnotationAspect(String id, @NlsContexts.ListItem String displayName, boolean showByDefault) {
      super(id, displayName, showByDefault);
    }

    @Override
    protected void showAffectedPaths(int lineNum) {
      P4Revision p4Revision = findRevisionForLine(lineNum);
      P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(myFile);
      if (p4Revision != null && connection != null) {
        final long changeNumber = p4Revision.getChangeNumber();
        ShowAllSubmittedFilesAction.showAllSubmittedFiles(myProject, changeNumber,
                                                          p4Revision.getSubmitMessage(),
                                                          p4Revision.getDate(),
                                                          p4Revision.getUser(),
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

  @Nullable
  @Override
  public LineModificationDetailsProvider getLineModificationDetailsProvider() {
    return DefaultLineModificationDetailsProvider.create(this);
  }
}
