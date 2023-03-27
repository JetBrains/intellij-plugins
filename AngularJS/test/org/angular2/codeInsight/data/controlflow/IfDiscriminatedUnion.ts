// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Component, Directive, Input, NgModule, TemplateRef, ViewContainerRef } from "@angular/core";
import { CommonModule } from "@angular/common";

type Named = { type: "Named"; name: string };
type Aged = { type: "Aged"; age: number };
type Union = Named | Aged;

@Component({
  selector: 'app-flow',
  template: `
    <div>
      <div *customIf="item.type === 'Named'">Name: {{ item.name }}</div>
      <div *customIf="item.type === 'Named'">Age: {{ item.<error descr="Unresolved variable age">age</error> }}</div>
      <div *customIf="item.type === 'Aged'">Name: {{ item.<error descr="Unresolved variable name">name</error> }}</div>
      <div *customIf="item.type === 'Aged'">Age: {{ item.age }}</div>
      
      <ng-template [customIf]="item.type === 'Named'">Name: {{ item.name }}</ng-template>
      <ng-template [customIf]="item.type === 'Named'">Age: {{ item.<error descr="Unresolved variable age">age</error> }}</ng-template>
      
      <div *customIf="isNamed(item)">Name: {{ item.name }}</div>
      <div *customIf="isNamed(item)">Age: {{ item.<error descr="Unresolved variable age">age</error> }}</div>
    </div>
  `,
})
export class FlowComponent {
  item: Union = { type: "Named", name: "John" };

  isNamed(item: Union): item is Named {
    return item.type === 'Named';
  }
}

// noinspection JSUnusedGlobalSymbols
export class NgIfContext<T = unknown> {
  $implicit!: T;
  ngIf!: T;
}

@Directive({
  selector: '[customIf]'
})
export class IfDirective<T = unknown> {
  constructor(_viewContainer: ViewContainerRef, _templateRef: TemplateRef<NgIfContext<T>>) {}

  // noinspection JSUnusedLocalSymbols
  @Input() set customIf(condition: T) {}

  static ngTemplateGuard_customIf: 'binding';

  static ngTemplateContextGuard<T>(dir: IfDirective<T>, ctx: any): ctx is NgIfContext<Exclude<T, false | 0 | '' | null | undefined>> {
    return true;
  }
}


@NgModule({
  imports: [CommonModule],
  declarations: [FlowComponent, IfDirective],
})
export class FlowModule {}
