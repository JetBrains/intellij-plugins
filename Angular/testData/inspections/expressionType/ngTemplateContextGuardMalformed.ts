// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
// noinspection JSUnusedLocalSymbols

import {Component, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test',
  imports: [
    CommonModule,
    <error descr="TS2449: Class 'MalformedDirective1' used before its declaration.">MalformedDirective1</error>,
    <error descr="TS2449: Class 'MalformedDirective2' used before its declaration.">MalformedDirective2</error>,
    <error descr="TS2449: Class 'MalformedDirective3' used before its declaration.">MalformedDirective3</error>,
    <error descr="TS2449: Class 'MalformedDirective4' used before its declaration.">MalformedDirective4</error>,
    <error descr="TS2449: Class 'MalformedDirective5' used before its declaration.">MalformedDirective5</error>,
    <error descr="TS2449: Class 'MalformedDirective6' used before its declaration.">MalformedDirective6</error>
  ],
  standalone: true,
  template: `
    <section>
      <!-- Angular evaluates local as the $implict -->
      <div *appMalformed1="true; let local">
        {{expectNumber(<error descr="TS2345: Argument of type 'boolean' is not assignable to parameter of type 'number'.">local</error>)}}
        {{local.<error descr="TS2339: Property 'allYouEverWanted' does not exist on type 'boolean'.">allYouEverWanted</error>}}
      </div>
      <div *appMalformed1="1; let local">
        {{expectNumber(local)}}
        {{local.<error descr="TS2339: Property 'allYouEverWanted' does not exist on type 'number'.">allYouEverWanted</error>}}
      </div>
    </section>
    <section>
      <!-- Angular evaluates local as any -->
      <div *appMalformed2="true; let local">
        {{expectNumber(local)}}
        {{local.allYouEverWanted}}
      </div>
      <div *appMalformed2="1; let local">
        {{expectNumber(local)}}
        {{local.allYouEverWanted}}
      </div>
    </section>
    <section>
      <!-- Angular evaluates local as any -->
      <div *appMalformed3="true; let local">
        {{expectNumber(local)}}
        {{local.allYouEverWanted}}
      </div>
      <div *appMalformed3="1; let local">
        {{expectNumber(local)}}
        {{local.allYouEverWanted}}
      </div>
    </section>
    <section>
      <!-- Angular highlights the whole embedded template as error, WebStorm is more permissive -->
      <div *appMalformed4="true; let local">
        {{expectNumber(<error descr="TS2345: Argument of type 'boolean' is not assignable to parameter of type 'number'.">local</error>)}}
        {{local.<error descr="TS2339: Property 'allYouEverWanted' does not exist on type 'boolean'.">allYouEverWanted</error>}}
      </div>
      <div *appMalformed4="1; let local">
        {{expectNumber(local)}}
        {{local.<error descr="TS2339: Property 'allYouEverWanted' does not exist on type 'number'.">allYouEverWanted</error>}}
      </div>
    </section>
    <section>
      <!-- Angular evaluates local as any -->
      <div *appMalformed5="true; let local">
        {{expectNumber(local)}}
        {{local.allYouEverWanted}}
      </div>
      <div *appMalformed5="1; let local">
        {{expectNumber(local)}}
        {{local.allYouEverWanted}}
      </div>
    </section>
    <section>
      <!-- Angular evaluates local as any -->
      <div *appMalformed6="true; let local">
        {{expectNumber(local)}}
        {{local.allYouEverWanted}}
      </div>
      <div *appMalformed6="1; let local">
        {{expectNumber(local)}}
        {{local.allYouEverWanted}}
      </div>
    </section>
  `,
})
export class TestComponent {
  expectNumber(num: number): number {
    return num;
  }
}

@Directive({
  selector: '[appMalformed1]',
  standalone: true
})
export class MalformedDirective1<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appMalformed1!: T;

  // arrow function instead of method
  static ngTemplateContextGuard = <T>(_dir: MalformedDirective1<T>, _ctx: unknown): _ctx is { $implicit: T; appMalformed1: T } => {
    return null!;
  }
}

@Directive({
  selector: '[appMalformed2]',
  standalone: true
})
export class MalformedDirective2<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appMalformed2!: T;

  // instance instead of static method
  ngTemplateContextGuard<T>(_dir: MalformedDirective2<T>, _ctx: unknown): _ctx is { $implicit: T; appMalformed2: T } {
    return null!;
  }
}

@Directive({
  selector: '[appMalformed3]',
  standalone: true
})
export class MalformedDirective3<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appMalformed3!: T;

  // random return type
  static ngTemplateContextGuard<T>(_dir: MalformedDirective3<T>, _ctx: unknown): boolean {
    return null!;
  }
}

@Directive({
  selector: '[appMalformed4]',
  standalone: true
})
export class MalformedDirective4<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appMalformed4!: T;

  // redundant trailing parameters – divergence, WebStorm is more permissive
  static ngTemplateContextGuard<T>(_dir: MalformedDirective4<T>, _ctx: unknown, _hello: number): _ctx is { $implicit: T; appMalformed4: T } {
    return null!;
  }
}

@Directive({
  selector: '[appMalformed5]',
  standalone: true
})
export class MalformedDirective5<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appMalformed5!: T;

  // flipped parameters
  static ngTemplateContextGuard<T>(_ctx: unknown, _dir: MalformedDirective5<T>): _ctx is { $implicit: T; appMalformed5: T } {
    return null!;
  }
}

@Directive({
  selector: '[appMalformed6]',
  standalone: true
})
export class MalformedDirective6<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appMalformed6!: T;

  // normal return instead of type predicate
  static ngTemplateContextGuard<T>(_dir: MalformedDirective6<T>, _ctx: unknown): { $implicit: T; appMalformed6: T } {
    return null!;
  }
}
