// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Foo, FooBar} from "./source";

class Bar extends Foo {
  do1(p: FooBar): Error {
      return super.do1(p);
  }
}