// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";


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

@Component({
  selector: 'app-test',
  imports: [CommonModule, HelloDirective, HelloConstrainedDirective],
  standalone: true,
  template: `
    <div *appHello="as person"> <!-- any in WebStorm, string in Angular -->
      {{expectNumber(<error descr="Argument type string is not assignable to parameter type number" textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
      {{expectString(person)}}
    </div>
    <div *appHello="let person"> <!-- string -->
      {{expectNumber(<error descr="Argument type string is not assignable to parameter type number" textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
      {{expectString(person)}}
    </div>
    <ng-template appHello let-person> <!-- string -->
      {{expectNumber(<error descr="Argument type string is not assignable to parameter type number" textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
      {{expectString(person)}}
    </ng-template>
    <ng-template <warning descr="[appHello] requires value" textAttributesKey="WARNING_ATTRIBUTES">[appHello]</warning> let-person> <!-- undefined -->
      {{expectNumber(<error descr="Argument type undefined is not assignable to parameter type number" textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
      {{expectString(<error descr="Argument type undefined is not assignable to parameter type string" textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
    </ng-template>
    <ng-template [appHello]="" let-person> <!-- undefined -->
      {{expectNumber(<error descr="Argument type undefined is not assignable to parameter type number" textAttributesKey="ERRORS_ATTRIBUTES">person</error>) }}
      {{expectString(<error descr="Argument type undefined is not assignable to parameter type string" textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
    </ng-template>
    
    <div <error descr="Type \"\" is not assignable to type number" textAttributesKey="ERRORS_ATTRIBUTES">*appHelloConstrained</error>></div>
    <div <error descr="Type \"\" is not assignable to type number" textAttributesKey="ERRORS_ATTRIBUTES">*appHelloConstrained</error>="as person"> <!-- number -->
      {{expectNumber(person)}}
      {{expectString(<error descr="Argument type number is not assignable to parameter type string" textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
    </div>
    <div <error descr="Type \"\" is not assignable to type number" textAttributesKey="ERRORS_ATTRIBUTES">*appHelloConstrained</error>="let person"> <!-- number -->
      {{expectNumber(person)}}
      {{expectString(<error descr="Argument type number is not assignable to parameter type string" textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
    </div>
    <ng-template <error descr="Type \"\" is not assignable to type number" textAttributesKey="ERRORS_ATTRIBUTES">appHelloConstrained</error> let-person> <!-- number -->
      {{expectNumber(person)}}
      {{expectString(<error descr="Argument type number is not assignable to parameter type string" textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
    </ng-template>
    <ng-template <error descr="Type undefined is not assignable to type number" textAttributesKey="ERRORS_ATTRIBUTES">[appHelloConstrained]</error> let-person> <!-- number -->
      {{expectNumber(person)}}
      {{expectString(<error descr="Argument type number is not assignable to parameter type string" textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
    </ng-template>
    <ng-template <error descr="Type undefined is not assignable to type number" textAttributesKey="ERRORS_ATTRIBUTES">[appHelloConstrained]</error>="" let-person> <!-- number -->
      {{expectNumber(person) }}
      {{expectString(<error descr="Argument type number is not assignable to parameter type string" textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
    </ng-template>
  `,
})
export class TestComponent {
  expectNumber(num: number): number {
    return num;
  }
  expectString(str: string): string {
    return str;
  }
}

interface HelloContext<T> {
  $implicit: T;
  appHello: T;
}

interface HelloConstrainedContext<T> {
  $implicit: T;
  appHelloConstrained: T;
}
