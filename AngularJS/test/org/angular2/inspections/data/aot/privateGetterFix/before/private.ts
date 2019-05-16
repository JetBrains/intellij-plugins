// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

@Component({
  templateUrl: "./private.html"
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

  /** My method */
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
