package com {
public class Bar {
  public static const SIZE: int;
}

public var Const = 0;
}

package com {
public class Foo {
   public static const MESSAGE: String;
}
}

package bar {
public class Zzz {
  public static function func(p:Yyy) {}
}
}

package bar {
public class Yyy {
 public function Yyy() {
  }
}
}

package uuu {
  public function glob() {}
}

package aaa {
 public class A {
   public static const SIZE;
 }
}

package bbb {
 public class B {
   public static var ourLength;
 }
}