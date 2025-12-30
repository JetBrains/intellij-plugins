// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test',
  imports: [CommonModule, <error descr="TS2449: Class 'HelloDirective' used before its declaration.">HelloDirective</error>],
  standalone: true,
  template: `
    <div *appHello="true as local; let foo = somethingOmitted">
      {{expectNumber(<error descr="TS2345: Argument of type 'boolean' is not assignable to parameter of type 'number'.">local</error>)}}
      {{expectNumber(foo)}}
      {{foo.allYouEverWanted}}
    </div>
  `,
})
export class TestComponent {
  expectNumber(num: number): number {
    return num;
  }
}

interface HelloContext<T, U> {
  $implicit: T;
  appHello: T;
  somethingOmitted: U;
}

@Directive({
  selector: '[appHello]',
  standalone: true
})
export class HelloDirective<T, U> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appHello!: T;
  @Input() appHelloOmitted!: U;

  static ngTemplateContextGuard<T, U>(_dir: HelloDirective<T, U>, _ctx: unknown): _ctx is HelloContext<T, U> {
    return true;
  }
}
