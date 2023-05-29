// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test',
  imports: [CommonModule, HelloDirective, HelloConstrainedDirective],
  standalone: true,
  template: `
    <div *appHello="as person"> <!-- any in WebStorm, string in Angular -->
      {{expectNumber(person)}}
    </div>
    <div *appHello="let person"> <!-- any in WebStorm, string in Angular -->
      {{expectNumber(person)}}
    </div>
    <ng-template appHello let-person> <!-- any in WebStorm, string in Angular -->
      {{expectNumber(person)}}
    </ng-template>
    <ng-template <warning descr="[appHello] requires value">[appHello]</warning> let-person> <!-- any in WebStorm, undefined in Angular -->
      {{expectNumber(person)}}
    </ng-template>
    <ng-template [appHello]="" let-person> <!-- undefined -->
      {{expectNumber(<error descr="Argument type undefined is not assignable to parameter type number">person</error>) }}
    </ng-template>

    <div *appHelloConstrained="as person"> <!-- any in WebStorm, number in Angular --> <!-- todo missing assignment type error -->
      {{expectNumber(person)}}
    </div>
    <div *appHelloConstrained="let person"> <!-- any in WebStorm, number in Angular --> <!-- todo missing assignment type error -->
      {{expectNumber(person)}}
    </div>
    <ng-template appHelloConstrained let-person> <!-- any in WebStorm, number in Angular --> <!-- todo missing assignment type error -->
      {{expectNumber(person)}}
    </ng-template>
    <ng-template <warning descr="[appHelloConstrained] requires value">[appHelloConstrained]</warning> let-person> <!-- any in WebStorm, number in Angular --> <!-- todo missing assignment type error -->
      {{expectNumber(person)}}
    </ng-template>
    <ng-template [appHelloConstrained]="" let-person> <!-- any in WebStorm, number in Angular --> <!-- todo missing assignment type error -->
      {{expectNumber(person) }}
    </ng-template>
  `,
})
export class TestComponent {
  expectNumber(num: number): number {
    return num;
  }
}

interface HelloContext<T> {
  $implicit: T;
  appHello: T;
}

@Directive({
  selector: '[appHello]',
  standalone: true
})
export class HelloDirective<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appHello!: T;

  static ngTemplateContextGuard<T>(dir: HelloDirective<T>, ctx: unknown): ctx is HelloContext<T> {
    return true;
  }
}

interface HelloConstrainedContext<T> {
  $implicit: T;
  appHelloConstrained: T;
}

@Directive({
  selector: '[appHelloConstrained]',
  standalone: true
})
export class HelloConstrainedDirective<T extends number> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appHelloConstrained!: T;

  static ngTemplateContextGuard<T extends number>(dir: HelloConstrainedDirective<T>, ctx: unknown): ctx is HelloConstrainedContext<T> {
    return true;
  }
}
