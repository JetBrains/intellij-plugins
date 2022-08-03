// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

@Component({
  template: `
    {{ <warning descr="Private field privateUsed cannot be resolved when using the AOT compiler.">privateUsed</warning> }}
    {{ protectedUsed }}
    {{ publicUsed }}

    <div (click)="<warning descr="Private property privateUsedSet cannot be resolved when using the AOT compiler.">privateUsedSet</warning> = 12"></div>
    <div (click)="protectedUsedSet = 12"></div>
    <div (click)="publicUsedSet = 12"></div>

    {{ <warning descr="Private property privateUsedGet cannot be resolved when using the AOT compiler.">privateUsedGet</warning> }}
    {{ protectedUsedGet }}
    {{ publicUsedGet }}

    {{ <warning descr="Private method privateUsedFun cannot be resolved when using the AOT compiler.">privateUsedFun</warning>() }}
    {{ protectedUsedFun() }}
    {{ publicUsedFun() }}
  `
})
export class MyComponent {
  private <warning descr="Private field privateUsed cannot be resolved from the component template when using the AOT compiler">privateUsed</warning>: string;
  private privateUnused: string;

  protected protectedUsed: string;
  protected protectedUnused: string;

  publicUsed: string;
  publicUnused: string;

  private set <warning descr="Private property privateUsedSet cannot be resolved from the component template when using the AOT compiler">privateUsedSet</warning>(value) {}
  private set privateUnusedSet(value) {}

  protected set protectedUsedSet(value) {}
  protected set protectedUnusedSet(value) {}

  public set publicUsedSet(value) {}
  public set publicUnusedSet(value) {}

  private get <warning descr="Private property privateUsedGet cannot be resolved from the component template when using the AOT compiler">privateUsedGet</warning>() {}
  private get privateUnusedGet() {}

  protected get protectedUsedGet() {}
  protected get protectedUnusedGet() {}

  public get publicUsedGet() {}
  public get publicUnusedGet() {}

  private <warning descr="Private method privateUsedFun cannot be resolved from the component template when using the AOT compiler">privateUsedFun</warning>() {}
  private privateUnusedFun() {}

  protected protectedUsedFun() {}
  protected protectedUnusedFun() {}

  public publicUsedFun() {}
  public publicUnusedFun() {}

}
