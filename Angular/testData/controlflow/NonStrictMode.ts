// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import { Component } from "@angular/core";
import { CommonModule } from "@angular/common";

type Named = { type: "Named"; name: string };
type Aged = { type: "Aged"; age: number };
type Union = Named | Aged;

@Component({
  selector: 'app-flow',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div>
      <div *ngIf="item.type === 'Named'">Name: {{ item.name }}</div>
      <div *ngIf="item.type === 'Named'">Age: {{ item.age }}</div>
      <div *ngIf="item.type === 'Aged'">Name: {{ item.name }}</div>
      <div *ngIf="item.type === 'Aged'">Age: {{ item.age }}</div>
      <ng-template [ngIf]="item.type === 'Named'">Name: {{ item.name }}</ng-template>
      <ng-template [ngIf]="item.type === 'Named'">Age: {{ item.age }}</ng-template>
    </div>
  `,
})
export class FlowComponent {
  item: Union = { type: "Named", name: "John" };
}
