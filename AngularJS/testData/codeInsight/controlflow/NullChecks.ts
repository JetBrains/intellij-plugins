// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
import {Component} from "@angular/core";
import {CommonModule} from "@angular/common";

@Component({
  selector: 'app-flow',
  imports: [CommonModule],
  standalone: true,
  template: `
    <div>{{bar.<error descr="Qualifier of 'length' is possibly undefined">length</error>}}</div>
    
    <div *ngIf="bar">
      <div>{{bar.length}}</div>
    </div>
    
    {{isString(bar) ? bar.length : bar.<error descr="Unresolved variable length">length</error>}}
    <div>{{bar && bar.length}}</div>
    
    <div *ngFor="let item of items">
      <div>{{isString(item) && item.length}}</div>
      <div [title]="item.length" *ngIf="item"></div>
    </div>
    
    <div>{{bar.<error descr="Qualifier of 'length' is possibly undefined">length</error>}}</div>
  `,
})
export class FlowComponent {
  bar: string | undefined = 'hello';
  items: (string | undefined)[] = ['hello'];

  isString(x: unknown): x is string {
    return typeof x === "string";
  }
}
