package com.intellij.javascript.flex.refactoring.extractSuper;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.refactoring.extractSuper.JSExtractInterfaceHandler;
import com.intellij.lang.javascript.refactoring.extractSuper.JSExtractSuperMode;
import com.intellij.lang.javascript.refactoring.ui.JSMemberSelectionPanel;
import com.intellij.lang.javascript.refactoring.util.JSInterfaceContainmentVerifier;
import com.intellij.lang.javascript.refactoring.util.JSMemberInfo;
import com.intellij.lang.javascript.refactoring.util.JSUsesAndInterfacesDependencyMemberInfoModel;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.classMembers.MemberInfoChange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class FlexExtractInterfaceDialog extends FlexExtractSuperBaseDialog {

  public FlexExtractInterfaceDialog(@NotNull JSClass sourceClass, @Nullable JSElement selectedElement) {
    super(sourceClass, JSMemberInfo.selectMembers(sourceClass, selectedElement, member ->
            member instanceof JSClass ||
            member instanceof JSFunction && !JSPsiImplUtils.hasModifier(member, JSAttributeList.ModifierType.STATIC)),
          JSExtractInterfaceHandler.getRefactoringName());
    init();
  }

  @Override
  protected String getExtractedSuperNameNotSpecifiedMessage() {
    return RefactoringBundle.message("no.interface.name.specified");
  }

  @Override
  protected String getTopLabelText() {
    return RefactoringBundle.message("extract.interface.from");
  }

  @Override
  protected BaseRefactoringProcessor createProcessor() {
    return new FlexExtractSuperProcessor(mySourceClass, getSelectedMemberInfos().toArray(JSMemberInfo.EMPTY_ARRAY),
                                         getExtractedSuperName(), getTargetPackageName(), getDocCommentPolicy(),
                                         getMode(), false, getTargetDirectory());
  }

  @Override
  protected JComponent createCenterPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    final JSMemberSelectionPanel memberSelectionPanel =
      new JSMemberSelectionPanel(RefactoringBundle.message("members.to.form.interface"), myMemberInfos, null);

    MyMemberInfoModel model = new MyMemberInfoModel(mySourceClass);
    memberSelectionPanel.getTable().setMemberInfoModel(model);
    memberSelectionPanel.getTable().addMemberInfoChangeListener(model);
    model.memberInfoChanged(new MemberInfoChange<>(myMemberInfos)); // force table revaliation
    panel.add(memberSelectionPanel, BorderLayout.CENTER);

    if (!mySourceClass.isInterface()) {
      panel.add(myDocCommentPanel, BorderLayout.EAST);
    }
    return panel;
  }

  @Override
  protected String getHelpId() {
    return "refactor.extractInterface";
  }

  @Override
  protected String getClassNameLabelText() {
    JSExtractSuperMode mode = getMode();
    return mode == JSExtractSuperMode.ExtractSuper || mode == JSExtractSuperMode.ExtractSuperTurnRefs
           ? RefactoringBundle.message("interface.name.prompt")
           : RefactoringBundle
             .message(mySourceClass.isInterface() ? "rename.original.interface.to" : "rename.implementation.class.to");
  }

  @Override
  protected String getPackageNameLabelText() {
    JSExtractSuperMode mode = getMode();
    return mode == JSExtractSuperMode.ExtractSuper || mode == JSExtractSuperMode.ExtractSuperTurnRefs
           ? RefactoringBundle.message("package.for.new.interface")
           : RefactoringBundle.message(mySourceClass.isInterface() ? "package.for.original.interface" : "package.for.original.class");
  }

  @NotNull
  @Override
  protected String getEntityName() {
    return RefactoringBundle.message("extractSuperInterface.interface");
  }


  private class MyMemberInfoModel extends JSUsesAndInterfacesDependencyMemberInfoModel {
    MyMemberInfoModel(JSClass aClass) {
      super(aClass, null, false, JSInterfaceContainmentVerifier.create(myMemberInfos));
    }

    @Override
    public int checkForProblems(@NotNull JSMemberInfo member) {
      // don't highlight checked interfaces members in blue, since we're extracting an interface and members will implicitly be there
      // don't highlight elements with red, since all the user methods will just be abstracted
      return OK;
    }
  }
}
