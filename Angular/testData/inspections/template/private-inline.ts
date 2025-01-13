// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component} from "@angular/core"

@Component({
  template: `
    {{ <error descr="TS2341: Property 'privateUsed' is private and only accessible within class 'MyComponent'.">privateUsed</error> }}
    {{ protectedUsed }}
    {{ publicUsed }}

    <div (click)="<error descr="TS2341: Property 'privateUsedSet' is private and only accessible within class 'MyComponent'.">privateUsedSet</error> = 12"></div>
    <div (click)="protectedUsedSet = 12"></div>
    <div (click)="publicUsedSet = 12"></div>

    {{ <error descr="TS2341: Property 'privateUsedGet' is private and only accessible within class 'MyComponent'.">privateUsedGet</error> }}
    {{ protectedUsedGet }}
    {{ publicUsedGet }}

    {{ <error descr="TS2341: Property 'privateUsedFun' is private and only accessible within class 'MyComponent'.">privateUsedFun</error>() }}
    {{ protectedUsedFun() }}
    {{ publicUsedFun() }}
  `
})
export class MyComponent {
  private privateUsed: string;
  private <weak_warning descr="TS6133: 'privateUnused' is declared but its value is never read.">privateUnused</weak_warning>: string;

  protected protectedUsed: string;
  protected protectedUnused: string;

  publicUsed: string;
  publicUnused: string;

  private set privateUsedSet(<weak_warning descr="TS6133: 'value' is declared but its value is never read.">value</weak_warning>: any) {}
  private set <weak_warning descr="TS6133: 'privateUnusedSet' is declared but its value is never read.">privateUnusedSet</weak_warning>(<weak_warning descr="TS6133: 'value' is declared but its value is never read.">value</weak_warning>: any) {}

  protected set protectedUsedSet(<weak_warning descr="TS6133: 'value' is declared but its value is never read.">value</weak_warning>: any) {}
  protected set protectedUnusedSet(<weak_warning descr="TS6133: 'value' is declared but its value is never read.">value</weak_warning>: any) {}

  public set publicUsedSet(<weak_warning descr="TS6133: 'value' is declared but its value is never read.">value</weak_warning>: any) {}
  public set publicUnusedSet(<weak_warning descr="TS6133: 'value' is declared but its value is never read.">value</weak_warning>: any) {}

  private get <error descr="TS2378: A 'get' accessor must return a value.">privateUsedGet</error>() {}
  private get <error descr="TS2378: A 'get' accessor must return a value.">privateUnusedGet</error>() {}

  protected get <error descr="TS2378: A 'get' accessor must return a value.">protectedUsedGet</error>() {}
  protected get <error descr="TS2378: A 'get' accessor must return a value.">protectedUnusedGet</error>() {}

  public get <error descr="TS2378: A 'get' accessor must return a value.">publicUsedGet</error>() {}
  public get <error descr="TS2378: A 'get' accessor must return a value.">publicUnusedGet</error>() {}

  private privateUsedFun() {}
  private <weak_warning descr="TS6133: 'privateUnusedFun' is declared but its value is never read.">privateUnusedFun</weak_warning>() {}

  protected protectedUsedFun() {}
  protected protectedUnusedFun() {}

  public publicUsedFun() {}
  public publicUnusedFun() {}

}
