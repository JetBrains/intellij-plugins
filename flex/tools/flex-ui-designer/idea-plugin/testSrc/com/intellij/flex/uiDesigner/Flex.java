package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Flex {
  String version() default "";
  boolean requireLocalStyleHolder() default false;
  boolean rawProjectRoot() default false;
  TargetPlatform platform() default TargetPlatform.Desktop;
}