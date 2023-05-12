// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component, Directive, Input, TemplateRef, ViewContainerRef} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-test-one',
  imports: [CommonModule, AgreeDirective],
  standalone: true,
  template: `
    <div *appAgree="personPromise | async as person; second: personPromise | async">
      {{expectPerson(<error descr="Argument type Person | null is not assignable to parameter type Person  Type null is not assignable to type Person">person</error>)}}
      {{expectNumber(<error descr="Argument type Person | null is not assignable to parameter type number  Type Person is not assignable to type number">person</error>)}}
    </div>
    <div *appAgree="null as person; second: undefined">
      {{expectPerson(<error descr="Argument type null is not assignable to parameter type Person">person</error>)}}
      {{expectNumber(<error descr="Argument type null is not assignable to parameter type number">person</error>)}}
    </div>
    <div *appAgree="null as person"> <!-- Angular types variables as any in case of omitted inputs -->
      {{expectPerson(<error descr="Argument type null is not assignable to parameter type Person">person</error>)}}
      {{expectNumber(<error descr="Argument type null is not assignable to parameter type number">person</error>)}}
    </div>
    <div *appAgree="personPromise | async as person"> <!-- Angular types variables as any in case of omitted inputs -->
      {{expectPerson(<error descr="Argument type Person | null is not assignable to parameter type Person  Type null is not assignable to type Person">person</error>)}}
      {{expectNumber(<error descr="Argument type Person | null is not assignable to parameter type number  Type Person is not assignable to type number">person</error>)}}
    </div>
    <footer>{{<error descr="Indexed expression can be null or undefined">(personPromise | async)</error>[0]}}</footer> <!-- ensure that null checks work -->
  `,
})
export class TestComponentOne extends TestComponentBase {
}

@Component({
  selector: 'app-test-two',
  imports: [CommonModule, AgreeDirective, AgreeDirectiveDuplicate],
  standalone: true,
  template: `
    <div *appAgree="personPromise | async as person; second: personPromise | async">
      {{expectPerson(<error descr="Argument type Person | null is not assignable to parameter type Person  Type null is not assignable to type Person">person</error>)}}
      {{expectNumber(<error descr="Argument type Person | null is not assignable to parameter type number  Type Person is not assignable to type number">person</error>)}}
    </div>
    <div *appAgree="null as person; second: undefined">
      {{expectPerson(<error descr="Argument type null is not assignable to parameter type Person">person</error>)}}
      {{expectNumber(<error descr="Argument type null is not assignable to parameter type number">person</error>)}}
    </div>
    <div *appAgree="null as person"> <!-- Angular types variables as any in case of omitted inputs -->
      {{expectPerson(<error descr="Argument type null is not assignable to parameter type Person">person</error>)}}
      {{expectNumber(<error descr="Argument type null is not assignable to parameter type number">person</error>)}}
    </div>
    <div *appAgree="personPromise | async as person"> <!-- Angular types variables as any in case of omitted inputs -->
      {{expectPerson(<error descr="Argument type Person | null is not assignable to parameter type Person  Type null is not assignable to type Person">person</error>)}}
      {{expectNumber(<error descr="Argument type Person | null is not assignable to parameter type number  Type Person is not assignable to type number">person</error>)}}
    </div>
    <footer>{{<error descr="Indexed expression can be null or undefined">(personPromise | async)</error>[0]}}</footer> <!-- ensure that null checks work -->
  `,
})
export class TestComponentTwo extends TestComponentBase {
}

abstract class TestComponentBase {
  personPromise = Promise.resolve<Person>({
    familyName: 'Doe'
  });

  expectPerson(num: Person): Person {
    return num;
  }

  expectNumber(num: number): number {
    return num;
  }
}

export interface Person {
  familyName: string;
}

interface AgreeContext<T> {
  $implicit: T;
  appAgree: T;
}

@Directive({
  selector: '[appAgree]',
  standalone: true
})
export class AgreeDirective<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appAgree!: T;
  @Input() appAgreeSecond!: T | undefined | null;

  static ngTemplateContextGuard<T>(dir: AgreeDirective<T>, ctx: unknown): ctx is AgreeContext<T> {
    return true;
  }
}

@Directive({
  selector: '[appAgree]',
  standalone: true
})
export class AgreeDirectiveDuplicate<T> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<any>) {
  }

  @Input() appAgree: T | undefined | null;
}
