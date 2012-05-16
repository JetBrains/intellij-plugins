package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationshipInfoAdapter;
import com.intellij.diagram.DiagramRelationships;
import com.intellij.diagram.presentation.DiagramLineType;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * User: ksafonov
 */
public interface FlashDiagramRelationship extends DiagramRelationshipInfo {

  String getType();

  DiagramRelationshipInfo INTERFACE_GENERALIZATION = DiagramRelationships.INTERFACE_GENERALIZATION;

  DiagramRelationshipInfo GENERALIZATION = DiagramRelationships.GENERALIZATION;

  DiagramRelationshipInfo REALIZATION = DiagramRelationships.REALIZATION;

  DiagramRelationshipInfo ANNOTATION = DiagramRelationships.ANNOTATION;

  String TYPE_DEPENDENCY = "DEPENDENCY";
  String TYPE_CREATE = "CREATE";
  String TYPE_ONE_TO_ONE = "ONE_TO_ONE";
  String TYPE_ONE_TO_MANY = "ONE_TO_MANY";

  class Factory {

    public static FlashDiagramRelationship dependency(@Nullable String label) {
      return new Impl(TYPE_DEPENDENCY, DiagramLineType.DASHED, StringUtil.notNullize(label), null, null, 1,
                      DiagramRelationships.getAngleArrow(), null);
    }

    public static FlashDiagramRelationship create() {
      return new Impl(TYPE_CREATE, DiagramLineType.DASHED, DiagramRelationships.CREATE.getLabel(), null, null, 1,
                      DiagramRelationships.getAngleArrow(), null);
    }

    public static FlashDiagramRelationship oneToOne(String label) {
      return new Impl(TYPE_ONE_TO_ONE, DiagramLineType.SOLID, label, "1", "1", 1, DiagramRelationships.getAngleArrow(), DIAMOND);
    }

    public static FlashDiagramRelationship oneToMany(String label) {
      return new Impl(TYPE_ONE_TO_MANY, DiagramLineType.SOLID, label, "1", "*", 1, DiagramRelationships.getAngleArrow(), DIAMOND);
    }

    private static class Impl extends DiagramRelationshipInfoAdapter implements FlashDiagramRelationship {

      private final String myType;
      private final Shape myStartArrow;
      private final Shape myEndArrow;

      public Impl(final String type,
                  final DiagramLineType lineType,
                  @Nullable final String label,
                  @Nullable final String fromLabel,
                  @Nullable final String toLabel,
                  final int width, final Shape startArrow, final Shape endArrow) {
        super(type, lineType, label, fromLabel, toLabel, width);
        myType = type;
        myStartArrow = startArrow;
        myEndArrow = endArrow;
      }

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
    }
  }
}





