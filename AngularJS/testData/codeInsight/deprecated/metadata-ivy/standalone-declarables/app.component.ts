// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core";
import {ToolbeltComponent, ToolbeltDirective, ToolbeltPipe} from "toolbelt";
import {ClassicToolbeltComponent} from "toolbelt/lib/classic.component";

@Component({
  selector: "app-root",
  template: `
    <div>
      <lib-toolbelt></lib-toolbelt>
      <span libToolbelt></span>
      <span>{{1 | toolbelt}}</span>
      <<error descr="Component or directive matching lib-classic-toolbelt element is out of scope of the current template">lib-classic-toolbelt</error>></lib-classic-toolbelt>
    </div>
  `,
  standalone: true,
  imports: [
    ToolbeltComponent,
    ToolbeltPipe,
    ToolbeltDirective,
    ClassicToolbeltComponent, // todo add warning
  ],
})
export class AppComponent {
}

@Component({
  selector: "app-without-imports",
  template: `
    <div>
      <<error descr="Component or directive matching lib-toolbelt element is out of scope of the current template">lib-toolbelt</error>></lib-toolbelt>
      <span <warning descr="Directive that provides attribute libToolbelt is out of scope of the current template">libToolbelt</warning>></span>
      <span>{{1 | <error descr="Unresolved pipe toolbelt">toolbelt</error>}}</span>
    </div>
  `,
  standalone: true,
  imports: [],
})
export class AppWithoutImports {
}
