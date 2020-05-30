// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

@Component({
  templateUrl: "./private-ts.html"
})
export class MyComponent {
  private <warning descr="Private field privateUsed cannot be resolved from the component template when using the AOT compiler">privateUsed</warning>: string;
  private privateUnused: string;

  protected <warning descr="Protected field protectedUsed cannot be resolved from the component template when using the AOT compiler">protectedUsed</warning>: string;
  protected protectedUnused: string;

  publicUsed: string;
  publicUnused: string;

  private set <warning descr="Private property privateUsedSet cannot be resolved from the component template when using the AOT compiler">privateUsedSet</warning>(value) {}
  private set privateUnusedSet(value) {}

  protected set <warning descr="Protected property protectedUsedSet cannot be resolved from the component template when using the AOT compiler">protectedUsedSet</warning>(value) {}
  protected set protectedUnusedSet(value) {}

  public set publicUsedSet(value) {}
  public set publicUnusedSet(value) {}

  private get <warning descr="Private property privateUsedGet cannot be resolved from the component template when using the AOT compiler">privateUsedGet</warning>() {}
  private get privateUnusedGet() {}

  protected get <warning descr="Protected property protectedUsedGet cannot be resolved from the component template when using the AOT compiler">protectedUsedGet</warning>() {}
  protected get protectedUnusedGet() {}

  public get publicUsedGet() {}
  public get publicUnusedGet() {}

  private <warning descr="Private method privateUsedFun cannot be resolved from the component template when using the AOT compiler">privateUsedFun</warning>() {}
  private privateUnusedFun() {}

  protected <warning descr="Protected method protectedUsedFun cannot be resolved from the component template when using the AOT compiler">protectedUsedFun</warning>() {}
  protected protectedUnusedFun() {}

  public publicUsedFun() {}
  public publicUnusedFun() {}

}
