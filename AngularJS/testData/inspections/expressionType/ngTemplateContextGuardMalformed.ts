// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
// noinspection JSUnusedLocalSymbols

import {Component, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test',
  imports: [
    CommonModule,
    MalformedDirective1,
    MalformedDirective2,
    MalformedDirective3,
    MalformedDirective4,
    MalformedDirective5,
    MalformedDirective6
  ],
  standalone: true,
  template: `
    <section>
      <!-- Angular evaluates local as any -->
      <div *appMalformed1="true; let local">
        {{expectNumber(local)}}
        {{local.<weak_warning descr="Unresolved variable allYouEverWanted">allYouEverWanted</weak_warning>}}
      </div>
      <div *appMalformed1="1; let local">
        {{expectNumber(local)}}
        {{local.<weak_warning descr="Unresolved variable allYouEverWanted">allYouEverWanted</weak_warning>}}
      </div>
    </section>
    <section>
      <!-- Angular evaluates local as any -->
      <div *appMalformed2="true; let local">
        {{expectNumber(local)}}
        {{local.<weak_warning descr="Unresolved variable allYouEverWanted">allYouEverWanted</weak_warning>}}
      </div>
      <div *appMalformed2="1; let local">
        {{expectNumber(local)}}
        {{local.<weak_warning descr="Unresolved variable allYouEverWanted">allYouEverWanted</weak_warning>}}
      </div>
    </section>
    <section>
      <!-- Angular evaluates local as any -->
      <div *appMalformed3="true; let local">
        {{expectNumber(local)}}
        {{local.<weak_warning descr="Unresolved variable allYouEverWanted">allYouEverWanted</weak_warning>}}
      </div>
      <div *appMalformed3="1; let local">
        {{expectNumber(local)}}
        {{local.<weak_warning descr="Unresolved variable allYouEverWanted">allYouEverWanted</weak_warning>}}
      </div>
    </section>
    <section>
      <!-- Angular highlights the whole embedded template as error, WebStorm is more permissive -->
      <div *appMalformed4="true; let local">
        {{expectNumber(<error descr="Argument type boolean is not assignable to parameter type number">local</error>)}}
        {{local.<error descr="Unresolved variable allYouEverWanted">allYouEverWanted</error>}}
      </div>
      <div *appMalformed4="1; let local">
        {{expectNumber(local)}}
        {{local.<error descr="Unresolved variable allYouEverWanted">allYouEverWanted</error>}}
      </div>
    </section>
    <section>
      <!-- Angular evaluates local as any -->
      <div *appMalformed5="true; let local">
        {{expectNumber(local)}}
        {{local.<weak_warning descr="Unresolved variable allYouEverWanted">allYouEverWanted</weak_warning>}}
      </div>
      <div *appMalformed5="1; let local">
        {{expectNumber(local)}}
        {{local.<weak_warning descr="Unresolved variable allYouEverWanted">allYouEverWanted</weak_warning>}}
      </div>
    </section>
    <section>
      <!-- Angular evaluates local as any -->
      <div *appMalformed6="true; let local">
        {{expectNumber(local)}}
        {{local.<weak_warning descr="Unresolved variable allYouEverWanted">allYouEverWanted</weak_warning>}}
      </div>
      <div *appMalformed6="1; let local">
        {{expectNumber(local)}}
        {{local.<weak_warning descr="Unresolved variable allYouEverWanted">allYouEverWanted</weak_warning>}}
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
  static ngTemplateContextGuard = <T>(dir: MalformedDirective1<T>, ctx: unknown): ctx is { $implicit: T; appMalformed1: T } => {
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
  ngTemplateContextGuard<T>(dir: MalformedDirective2<T>, ctx: unknown): ctx is { $implicit: T; appMalformed2: T } {
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
  static ngTemplateContextGuard<T>(dir: MalformedDirective3<T>, ctx: unknown): boolean {
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
  static ngTemplateContextGuard<T>(dir: MalformedDirective4<T>, ctx: unknown, hello: number): ctx is { $implicit: T; appMalformed4: T } {
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
  static ngTemplateContextGuard<T>(ctx: unknown, dir: MalformedDirective5<T>): ctx is { $implicit: T; appMalformed5: T } {
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
  static ngTemplateContextGuard<T>(dir: MalformedDirective6<T>, ctx: unknown): { $implicit: T; appMalformed6: T } {
    return null!;
  }
}
