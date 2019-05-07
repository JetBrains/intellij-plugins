// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

@Component({
  templateUrl: "./private-ts.html"
})
export class MyComponent {
  private <warning descr="Property is inaccessible from component's template in AOT compilation mode">privateUsed</warning>: string;
  private privateUnused: string;

  protected <warning descr="Property is inaccessible from component's template in AOT compilation mode">protectedUsed</warning>: string;
  protected protectedUnused: string;

  publicUsed: string;
  publicUnused: string;

  private set <warning descr="Property is inaccessible from component's template in AOT compilation mode">privateUsedSet</warning>(value) {}
  private set privateUnusedSet(value) {}

  protected set <warning descr="Property is inaccessible from component's template in AOT compilation mode">protectedUsedSet</warning>(value) {}
  protected set protectedUnusedSet(value) {}

  public set publicUsedSet(value) {}
  public set publicUnusedSet(value) {}

  private get <warning descr="Property is inaccessible from component's template in AOT compilation mode">privateUsedGet</warning>() {}
  private get privateUnusedGet() {}

  protected get <warning descr="Property is inaccessible from component's template in AOT compilation mode">protectedUsedGet</warning>() {}
  protected get protectedUnusedGet() {}

  public get publicUsedGet() {}
  public get publicUnusedGet() {}

  private <warning descr="Method is inaccessible from component's template in AOT compilation mode">privateUsedFun</warning>() {}
  private privateUnusedFun() {}

  protected <warning descr="Method is inaccessible from component's template in AOT compilation mode">protectedUsedFun</warning>() {}
  protected protectedUnusedFun() {}

  public publicUsedFun() {}
  public publicUnusedFun() {}

}
