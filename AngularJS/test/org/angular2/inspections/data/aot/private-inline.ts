// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

@Component({
  template: `
    {{ <warning descr="Property is inaccessible in AOT compilation mode.">privateUsed</warning> }}
    {{ <warning descr="Property is inaccessible in AOT compilation mode.">protectedUsed</warning> }}
    {{ publicUsed }}

    <div (click)="<warning descr="Property is inaccessible in AOT compilation mode.">privateUsedSet</warning> = 12"></div>
    <div (click)="<warning descr="Property is inaccessible in AOT compilation mode.">protectedUsedSet</warning> = 12"></div>
    <div (click)="publicUsedSet = 12"></div>

    {{ <warning descr="Property is inaccessible in AOT compilation mode.">privateUsedGet</warning> }}
    {{ <warning descr="Property is inaccessible in AOT compilation mode.">protectedUsedGet</warning> }}
    {{ publicUsedGet }}

    {{ <warning descr="Method is inaccessible in AOT compilation mode.">privateUsedFun</warning>() }}
    {{ <warning descr="Method is inaccessible in AOT compilation mode.">protectedUsedFun</warning>() }}
    {{ publicUsedFun() }}
  `
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
