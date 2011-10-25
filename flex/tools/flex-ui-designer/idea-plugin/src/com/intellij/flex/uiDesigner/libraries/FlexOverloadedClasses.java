package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.abc.AbcFilter;
import gnu.trove.THashSet;

import java.util.Set;

public final class FlexOverloadedClasses {
  public static final Set<CharSequence> AIR_SPARK_CLASSES = new THashSet<CharSequence>(2, AbcFilter.HASHING_STRATEGY);
  public static final Set<CharSequence> MX_CLASSES = new THashSet<CharSequence>(7, AbcFilter.HASHING_STRATEGY);

  public static final String STYLE_PROTO_CHAIN = "mx.styles:StyleProtoChain";
  public static final String SKINNABLE_COMPONENT = "spark.components.supportClasses:SkinnableComponent";

  public static final String SPARK_WINDOW = "spark.components:Window";

  public enum InjectionClassifier {
    framework, spark
  }

  static {
    AIR_SPARK_CLASSES.add(SPARK_WINDOW);
    AIR_SPARK_CLASSES.add("spark.components:WindowedApplication");

    MX_CLASSES.add("mx.managers:LayoutManager");
    MX_CLASSES.add("mx.managers:CursorManager");
    MX_CLASSES.add("mx.managers:CursorManagerImpl");
    MX_CLASSES.add("mx.resources:ResourceManager");
    MX_CLASSES.add("mx.resources:ResourceManagerImpl");
    MX_CLASSES.add("mx.styles:StyleManager");
    MX_CLASSES.add("mx.styles:StyleManagerImpl");
  }
}