// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

@Component({
  template: `
    {{ <error descr="Field privateUsed is private and only accessible within class MyComponent when using the AOT compiler" textAttributesKey="ERRORS_ATTRIBUTES">privateUsed</error> }}
    {{ protectedUsed }}
    {{ publicUsed }}

    <div (click)="<error descr="Property privateUsedSet is private and only accessible within class MyComponent when using the AOT compiler" textAttributesKey="ERRORS_ATTRIBUTES">privateUsedSet</error> = 12"></div>
    <div (click)="protectedUsedSet = 12"></div>
    <div (click)="publicUsedSet = 12"></div>

    {{ <error descr="Property privateUsedGet is private and only accessible within class MyComponent when using the AOT compiler" textAttributesKey="ERRORS_ATTRIBUTES">privateUsedGet</error> }}
    {{ protectedUsedGet }}
    {{ publicUsedGet }}

    {{ <error descr="Method privateUsedFun is private and only accessible within class MyComponent when using the AOT compiler" textAttributesKey="ERRORS_ATTRIBUTES">privateUsedFun</error>() }}
    {{ protectedUsedFun() }}
    {{ publicUsedFun() }}
  `
})
export class MyComponent {
  private privateUsed: string;
  private privateUnused: string;

  protected protectedUsed: string;
  protected protectedUnused: string;

  publicUsed: string;
  publicUnused: string;

  private set privateUsedSet(value) {}
  private set privateUnusedSet(value) {}

  protected set protectedUsedSet(value) {}
  protected set protectedUnusedSet(value) {}

  public set publicUsedSet(value) {}
  public set publicUnusedSet(value) {}

  private get privateUsedGet() {}
  private get privateUnusedGet() {}

  protected get protectedUsedGet() {}
  protected get protectedUnusedGet() {}

  public get publicUsedGet() {}
  public get publicUnusedGet() {}

  private privateUsedFun() {}
  private privateUnusedFun() {}

  protected protectedUsedFun() {}
  protected protectedUnusedFun() {}

  public publicUsedFun() {}
  public publicUnusedFun() {}

}
