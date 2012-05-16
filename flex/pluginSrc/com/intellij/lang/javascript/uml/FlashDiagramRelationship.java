package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationshipInfoAdapter;
import com.intellij.diagram.DiagramRelationships;
import com.intellij.diagram.presentation.DiagramLineType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * User: ksafonov
 */
public interface FlashDiagramRelationship extends DiagramRelationshipInfo {

  @NotNull
  String getType();

  @Nullable
  PsiElement getElement();

  DiagramRelationshipInfo INTERFACE_GENERALIZATION = DiagramRelationships.INTERFACE_GENERALIZATION;

  DiagramRelationshipInfo GENERALIZATION = DiagramRelationships.GENERALIZATION;

  DiagramRelationshipInfo REALIZATION = DiagramRelationships.REALIZATION;

  DiagramRelationshipInfo ANNOTATION = DiagramRelationships.ANNOTATION;

  String TYPE_DEPENDENCY = "DEPENDENCY";
  String TYPE_CREATE = "CREATE";
  String TYPE_ONE_TO_ONE = "ONE_TO_ONE";
  String TYPE_ONE_TO_MANY = "ONE_TO_MANY";

  class Factory {

    public static FlashDiagramRelationship dependency(@Nullable String label, @NotNull PsiElement element) {
      return new Impl(TYPE_DEPENDENCY, DiagramLineType.DASHED, StringUtil.notNullize(label), null, null, 1,
                      DiagramRelationships.getAngleArrow(), null, element);
    }

    public static FlashDiagramRelationship create(@NotNull PsiElement element) {
      return new Impl(TYPE_CREATE, DiagramLineType.DASHED, DiagramRelationships.CREATE.getLabel(), null, null, 1,
                      DiagramRelationships.getAngleArrow(), null, element);
    }

    public static FlashDiagramRelationship oneToOne(String label, @NotNull PsiElement element) {
      return new Impl(TYPE_ONE_TO_ONE, DiagramLineType.SOLID, label, "1", "1", 1, DiagramRelationships.getAngleArrow(), DIAMOND, element);
    }

    public static FlashDiagramRelationship oneToMany(String label, @NotNull PsiElement element) {
      return new Impl(TYPE_ONE_TO_MANY, DiagramLineType.SOLID, label, "1", "*", 1, DiagramRelationships.getAngleArrow(), DIAMOND, element);
    }

    private static class Impl extends DiagramRelationshipInfoAdapter implements FlashDiagramRelationship {

      private final String myType;
      private final Shape myStartArrow;
      private final Shape myEndArrow;
      @Nullable
      private SmartPsiElementPointer<PsiElement> myElementPointer;

      public Impl(@NotNull final String type,
                  final DiagramLineType lineType,
                  @Nullable final String label,
                  @Nullable final String fromLabel,
                  @Nullable final String toLabel,
                  final int width,
                  final Shape startArrow,
                  final Shape endArrow,
                  @Nullable PsiElement element) {
        super(type, lineType, label, fromLabel, toLabel, width);
        myType = type;
        myStartArrow = startArrow;
        myEndArrow = endArrow;
        myElementPointer = element != null ? SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element) : null;
      }

      @Nullable
      public PsiElement getElement() {
        return myElementPointer != null ? myElementPointer.getElement() : null;
      }

      @NotNull
      @Override
      public String getType() {
        return myType;
      }

      @Override
      public Shape getStartArrow() {
        return myStartArrow;
      }

      @Override
      public Shape getEndArrow() {
        return myEndArrow;
      }

      @Override
      public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Impl impl = (Impl)o;

        PsiElement element = getElement();
        if (element != null ? !element.equals(impl.getElement()) : impl.getElement() != null) return false;
        if (myType != null ? !myType.equals(impl.myType) : impl.myType != null) return false;

        return true;
      }

      @Override
      public int hashCode() {
        int result = myType != null ? myType.hashCode() : 0;
        PsiElement element = getElement();
        result = 31 * result + (element != null ? element.hashCode() : 0);
        return result;
      }
    }
  }
}





