package com.jetbrains.lang.dart.ide.structure;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.PlatformIcons;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {

  private static final Sorter[] SORTERS = new Sorter[]{Sorter.ALPHA_SORTER};

  public DartStructureViewModel(@NotNull PsiFile psiFile, @Nullable Editor editor) {
    super(psiFile, editor, new DartStructureViewElement(psiFile));
    // order matters, first elements are compared first when walking up parents in AST:
    withSuitableClasses(DartVarAccessDeclaration.class, DartFunctionDeclarationWithBodyOrNative.class, DartMethodDeclaration.class,
                        DartFactoryConstructorDeclaration.class, DartNamedConstructorDeclaration.class,
                        DartFunctionTypeAlias.class, DartGetterDeclaration.class, DartSetterDeclaration.class,
                        DartEnumConstantDeclaration.class, DartClass.class);
  }

  @Override
  public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
    return false;
  }

  @NotNull
  @Override
  public Filter[] getFilters() {
    return new Filter[]{ourFieldsFilter};
  }

  @NotNull
  public Sorter[] getSorters() {
    return SORTERS;
  }

  @Override
  public boolean isAlwaysLeaf(StructureViewTreeElement element) {
    final Object value = element.getValue();
    return value instanceof DartComponent && !(value instanceof DartClass);
  }

  @Override
  public boolean shouldEnterElement(Object element) {
    return element instanceof DartClass;
  }


  private static final Filter ourFieldsFilter = new Filter() {
    @NonNls public static final String ID = "SHOW_FIELDS";

    @Override
    public boolean isVisible(TreeElement treeNode) {
      if (!(treeNode instanceof DartStructureViewElement)) return true;
      final PsiElement element = ((DartStructureViewElement)treeNode).getElement();

      DartComponentType type = DartComponentType.typeOf(element);
      if (type == DartComponentType.FIELD || type == DartComponentType.VARIABLE) {
        return false;
      }

      if (element instanceof DartComponent && (((DartComponent)element).isGetter() || ((DartComponent)element).isGetter())) {
        return false;
      }

      return true;
    }

    @Override
    public boolean isReverted() {
      return true;
    }

    @Override
    @NotNull
    public ActionPresentation getPresentation() {
      return new ActionPresentationData(
        IdeBundle.message("action.structureview.show.fields"),
        null,
        PlatformIcons.FIELD_ICON
      );
    }

    @Override
    @NotNull
    public String getName() {
      return ID;
    }
  };
}
