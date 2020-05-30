package com.intellij.javascript.flex.refactoring.extractSuper;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.extractSuper.JSExtractSuperClassHandler;
import com.intellij.lang.javascript.refactoring.extractSuper.JSExtractSuperMode;
import com.intellij.lang.javascript.refactoring.ui.JSMemberSelectionPanel;
import com.intellij.lang.javascript.refactoring.util.JSInterfaceContainmentVerifier;
import com.intellij.lang.javascript.refactoring.util.JSMemberInfo;
import com.intellij.lang.javascript.refactoring.util.JSUsesAndInterfacesDependencyMemberInfoModel;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.classMembers.MemberInfoBase;
import com.intellij.refactoring.classMembers.MemberInfoChange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class FlexExtractSuperClassDialog extends FlexExtractSuperBaseDialog {

  public FlexExtractSuperClassDialog(@NotNull JSClass sourceClass, @Nullable JSElement selectedElement) {
    super(sourceClass, JSMemberInfo.selectMembers(sourceClass, selectedElement, new MemberInfoBase.EmptyFilter<>()),
          JSExtractSuperClassHandler.getRefactoringName());
    init();
  }

  @Override
  protected String getExtractedSuperNameNotSpecifiedMessage() {
    return RefactoringBundle.message("no.superclass.name.specified");
  }

  @Override
  protected String getTopLabelText() {
    return RefactoringBundle.message("extract.superclass.from");
  }

  @Override
  protected BaseRefactoringProcessor createProcessor() {
    return new FlexExtractSuperProcessor(mySourceClass, getSelectedMemberInfos().toArray(JSMemberInfo.EMPTY_ARRAY),
                                         getExtractedSuperName(), getTargetPackageName(), getDocCommentPolicy(),
                                         getMode(), true, getTargetDirectory());
  }

  @Override
  protected JComponent createCenterPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    final JSMemberSelectionPanel memberSelectionPanel =
      new JSMemberSelectionPanel(RefactoringBundle.message("members.to.form.superclass"), myMemberInfos, null);

    MyMemberInfoModel model = new MyMemberInfoModel(mySourceClass);
    memberSelectionPanel.getTable().setMemberInfoModel(model);
    memberSelectionPanel.getTable().addMemberInfoChangeListener(model);
    model.memberInfoChanged(new MemberInfoChange<>(myMemberInfos)); // force table revaliation
    panel.add(memberSelectionPanel, BorderLayout.CENTER);
    //panel.add(myDocCommentPanel, BorderLayout.EAST); ASDoc is always moved
    return panel;
  }

  @Override
  protected String getHelpId() {
    return "refactor.extractSuperclass";
  }

  @Override
  protected String getClassNameLabelText() {
    JSExtractSuperMode mode = getMode();
    return mode == JSExtractSuperMode.ExtractSuper || mode == JSExtractSuperMode.ExtractSuperTurnRefs
           ? RefactoringBundle.message("superclass.name")
           : RefactoringBundle.message("rename.implementation.class.to");
  }

  @Override
  protected String getPackageNameLabelText() {
    JSExtractSuperMode mode = getMode();
    return mode == JSExtractSuperMode.ExtractSuper || mode == JSExtractSuperMode.ExtractSuperTurnRefs
           ? RefactoringBundle.message("package.for.new.superclass")
           : RefactoringBundle.message("package.for.original.class");
  }

  @NotNull
  @Override
  protected String getEntityName() {
    return RefactoringBundle.message("ExtractSuperClass.superclass");
  }


  private class MyMemberInfoModel extends JSUsesAndInterfacesDependencyMemberInfoModel {
    MyMemberInfoModel(JSClass aClass) {
      super(aClass, null, false, JSInterfaceContainmentVerifier.create(myMemberInfos));
    }
  }
}
