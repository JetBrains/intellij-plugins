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

  static ngTemplateContextGuard<T>(_dir: HelloDirective<T>, _ctx: unknown): _ctx is HelloContext<T> {
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

  static ngTemplateContextGuard<T extends number>(_dir: HelloConstrainedDirective<T>, _ctx: unknown): _ctx is HelloConstrainedContext<T> {
    return true;
  }
}

@Component({
  selector: 'app-test',
  imports: [CommonModule, HelloDirective, HelloConstrainedDirective],
  standalone: true,
  template: `
    <div *appHello="as person"> <!-- string -->
      {{expectNumber(<error descr="TS2345: Argument of type 'string' is not assignable to parameter of type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
      {{expectString(person)}}
    </div>
    <div *appHello="let person"> <!-- string -->
      {{expectNumber(<error descr="TS2345: Argument of type 'string' is not assignable to parameter of type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
      {{expectString(person)}}
    </div>
    <ng-template appHello let-person> <!-- string -->
      {{expectNumber(<error descr="TS2345: Argument of type 'string' is not assignable to parameter of type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
      {{expectString(person)}}
    </ng-template>
    <ng-template <warning descr="[appHello] requires value" textAttributesKey="WARNING_ATTRIBUTES">[appHello]</warning> let-person> <!-- undefined -->
      {{expectNumber(<error descr="TS2345: Argument of type 'undefined' is not assignable to parameter of type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
      {{expectString(<error descr="TS2345: Argument of type 'undefined' is not assignable to parameter of type 'string'." textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
    </ng-template>
    <ng-template [appHello]="" let-person> <!-- undefined -->
      {{expectNumber(<error descr="TS2345: Argument of type 'undefined' is not assignable to parameter of type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">person</error>) }}
      {{expectString(<error descr="TS2345: Argument of type 'undefined' is not assignable to parameter of type 'string'." textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
    </ng-template>
    
    <div <error descr="TS2322: Type 'string' is not assignable to type 'number'." textAttributesKey="ERRORS_ATTRIBUTES"><warning descr="*appHelloConstrained requires value" textAttributesKey="WARNING_ATTRIBUTES">*appHelloConstrained</warning></error>></div>
    <div <error descr="TS2322: Type 'string' is not assignable to type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">*appHelloConstrained</error>="as person"> <!-- number -->
      {{expectNumber(person)}}
      {{expectString(<error descr="TS2345: Argument of type 'number' is not assignable to parameter of type 'string'." textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
    </div>
    <div <error descr="TS2322: Type 'string' is not assignable to type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">*appHelloConstrained</error>="let person"> <!-- number -->
      {{expectNumber(person)}}
      {{expectString(<error descr="TS2345: Argument of type 'number' is not assignable to parameter of type 'string'." textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
    </div>
    <ng-template <error descr="TS2322: Type 'string' is not assignable to type 'number'." textAttributesKey="ERRORS_ATTRIBUTES"><warning descr="appHelloConstrained requires value" textAttributesKey="WARNING_ATTRIBUTES">appHelloConstrained</warning></error> let-person> <!-- number -->
      {{expectNumber(person)}}
      {{expectString(<error descr="TS2345: Argument of type 'number' is not assignable to parameter of type 'string'." textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
    </ng-template>
    <ng-template <error descr="TS2322: Type 'undefined' is not assignable to type 'number'." textAttributesKey="ERRORS_ATTRIBUTES"><warning descr="[appHelloConstrained] requires value" textAttributesKey="WARNING_ATTRIBUTES">[appHelloConstrained]</warning></error> let-person> <!-- number -->
      {{expectNumber(person)}}
      {{expectString(<error descr="TS2345: Argument of type 'number' is not assignable to parameter of type 'string'." textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
    </ng-template>
    <ng-template <error descr="TS2322: Type 'undefined' is not assignable to type 'number'." textAttributesKey="ERRORS_ATTRIBUTES">[appHelloConstrained]</error>="" let-person> <!-- number -->
      {{expectNumber(person) }}
      {{expectString(<error descr="TS2345: Argument of type 'number' is not assignable to parameter of type 'string'." textAttributesKey="ERRORS_ATTRIBUTES">person</error>)}}
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
