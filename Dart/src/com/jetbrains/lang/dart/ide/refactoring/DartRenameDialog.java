// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.refactoring;

import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.ui.NameSuggestionsField;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.*;
import com.intellij.util.SmartList;
import com.intellij.util.ui.JBUI;
import com.intellij.xml.util.XmlStringUtil;
import com.intellij.xml.util.XmlTagUtilBase;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.ide.findUsages.DartServerFindUsagesHandler;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.dartlang.analysis.server.protocol.SourceEdit;
import org.dartlang.analysis.server.protocol.SourceFileEdit;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

final class DartRenameDialog extends ServerRefactoringDialog<ServerRenameRefactoring> {
  private final JLabel myNewNamePrefix = new JLabel("");
  private NameSuggestionsField myNameSuggestionsField;

  DartRenameDialog(@NotNull final Project project, @Nullable final Editor editor, @NotNull final ServerRenameRefactoring refactoring) {
    super(project, editor, refactoring);

    setTitle(DartBundle.message("dialog.title.rename.0", refactoring.getElementKindName()));
    createNewNameComponent();
    init();
  }

  @Override
  protected void canRun() throws ConfigurationException {
    if (Comparing.strEqual(getNewName(), myRefactoring.getOldName())) {
      throw new ConfigurationException(null);
    }
    super.canRun();
  }

  @Override
  protected JComponent createCenterPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbConstraints = new GridBagConstraints();

    gbConstraints.insets = JBUI.insetsBottom(4);
    gbConstraints.weighty = 0;
    gbConstraints.weightx = 1;
    gbConstraints.gridwidth = GridBagConstraints.REMAINDER;
    gbConstraints.fill = GridBagConstraints.BOTH;
    JLabel nameLabel = new JLabel();
    panel.add(nameLabel, gbConstraints);
    nameLabel.setText(XmlStringUtil.wrapInHtml(XmlTagUtilBase.escapeString(getLabelText(), false)));

    gbConstraints.insets = JBUI.insetsBottom(4);
    gbConstraints.gridwidth = 1;
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.weightx = 0;
    gbConstraints.gridx = 0;
    gbConstraints.anchor = GridBagConstraints.WEST;
    panel.add(myNewNamePrefix, gbConstraints);

    gbConstraints.insets = JBUI.insetsBottom(8);
    gbConstraints.gridwidth = 2;
    gbConstraints.fill = GridBagConstraints.BOTH;
    gbConstraints.weightx = 1;
    gbConstraints.gridx = 0;
    gbConstraints.weighty = 1;
    panel.add(myNameSuggestionsField.getComponent(), gbConstraints);

    return panel;
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myNameSuggestionsField.getFocusableComponent();
  }

  private void createNewNameComponent() {
    String[] suggestedNames = getSuggestedNames();
    myNameSuggestionsField = new NameSuggestionsField(suggestedNames, myProject, FileTypes.PLAIN_TEXT, myEditor) {
      @Override
      protected boolean shouldSelectAll() {
        return myEditor == null || myEditor.getSettings().isPreselectRename();
      }
    };

    myNameSuggestionsField.addDataChangedListener(this::processNewNameChanged);
  }

  private @NotNull @Nls String getLabelText() {
    final String kindName = StringUtil.toLowerCase(myRefactoring.getElementKindName());
    final String name = myRefactoring.getOldName().isEmpty() ? kindName : kindName + " " + myRefactoring.getOldName();
    return RefactoringBundle.message("rename.0.and.its.usages.to", name);
  }

  private String getNewName() {
    return myNameSuggestionsField.getEnteredName().trim();
  }

  private String @NotNull [] getSuggestedNames() {
    return new String[]{myRefactoring.getOldName()};
  }

  private void processNewNameChanged() {
    myRefactoring.setNewName(getNewName());
  }

  @Override
  protected boolean hasPreviewButton() {
    return true;
  }

  @Override
  protected boolean isForcePreview() {
    final Set<String> potentialEdits = myRefactoring.getPotentialEdits();
    return !potentialEdits.isEmpty() && !ApplicationManager.getApplication().isUnitTestMode();
  }

  @Override
  protected void previewRefactoring() {
    final UsageViewPresentation presentation = new UsageViewPresentation();
    presentation.setTabText(RefactoringBundle.message("usageView.tabText"));
    presentation.setShowCancelButton(true);
    presentation
      .setTargetsNodeText(RefactoringBundle.message("0.to.be.renamed.to.1.2", myRefactoring.getElementKindName(), "", getNewName()));
    presentation.setNonCodeUsagesString(DartBundle.message("usages.in.comments.to.rename"));
    presentation.setCodeUsagesString(DartBundle.message("usages.in.code.to.rename"));
    presentation.setDynamicUsagesString(DartBundle.message("dynamic.usages.to.rename"));
    presentation.setUsageTypeFilteringAvailable(false);

    final List<UsageTarget> usageTargets = new SmartList<>();
    final Map<Usage, String> usageToEditIdMap = new HashMap<>();
    fillTargetsAndUsageToEditIdMap(usageTargets, usageToEditIdMap);

    final UsageTarget[] targets = usageTargets.toArray(UsageTarget.EMPTY_ARRAY);
    final Set<Usage> usageSet = usageToEditIdMap.keySet();
    final Usage[] usages = usageSet.toArray(Usage.EMPTY_ARRAY);

    final UsageView usageView = UsageViewManager.getInstance(myProject).showUsages(targets, usages, presentation);

    final SourceChange sourceChange = myRefactoring.getChange();
    assert sourceChange != null;

    usageView.addPerformOperationAction(createRefactoringRunnable(usageView, usageToEditIdMap),
                                        sourceChange.getMessage(),
                                        DartBundle.message("rename.need.reRun"),
                                        RefactoringBundle.message("usageView.doAction"), false);
  }

  private void fillTargetsAndUsageToEditIdMap(@NotNull final List<UsageTarget> usageTargets,
                                              @NotNull final Map<Usage, String> usageToEditIdMap) {
    final SourceChange change = myRefactoring.getChange();
    assert change != null;

    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(myProject);
    final PsiManager psiManager = PsiManager.getInstance(myProject);

    for (SourceFileEdit fileEdit : change.getEdits()) {
      final VirtualFile file = AssistUtils.findVirtualFile(fileEdit);
      final PsiFile psiFile = file == null ? null : psiManager.findFile(file);
      if (psiFile == null) continue;

      for (SourceEdit sourceEdit : fileEdit.getEdits()) {
        final int offset = service.getConvertedOffset(file, sourceEdit.getOffset());
        final int length = service.getConvertedOffset(file, sourceEdit.getOffset() + sourceEdit.getLength()) - offset;
        final TextRange range = TextRange.create(offset, offset + length);
        final boolean potentialUsage = myRefactoring.getPotentialEdits().contains(sourceEdit.getId());
        final PsiElement usageElement = DartServerFindUsagesHandler.getUsagePsiElement(psiFile, range);
        if (usageElement != null) {
          if (DartComponentType.typeOf(usageElement) != null) {
            usageTargets.add(new PsiElement2UsageTargetAdapter(usageElement));
          }
          else {
            final UsageInfo usageInfo = DartServerFindUsagesHandler.getUsageInfo(usageElement, range, potentialUsage);
            if (usageInfo != null) {
              usageToEditIdMap.put(new UsageInfo2UsageAdapter(usageInfo), sourceEdit.getId());
            }
          }
        }
      }
    }
  }

  @NotNull
  private Runnable createRefactoringRunnable(@NotNull final UsageView usageView, @NotNull final Map<Usage, String> usageToEditIdMap) {
    return () -> {
      final Set<String> excludedIds = new HashSet<>();

      // usageView.getExcludedUsages() and usageView.getUsages() doesn't contain deleted usages, that's why we need to start with full set usageToEditIdMap.keySet()
      final Set<Usage> excludedUsages = new HashSet<>(usageToEditIdMap.keySet());
      excludedUsages.removeAll(usageView.getUsages());
      excludedUsages.addAll(usageView.getExcludedUsages());

      for (Usage excludedUsage : excludedUsages) {
        excludedIds.add(usageToEditIdMap.get(excludedUsage));
      }

      super.doRefactoring(excludedIds);
    };
  }
}
