import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import ModModule from "./mod.module";

@Component({
  selector: 'app-comp-b',
  standalone: true,
  imports: [CommonModule,ModModule],
  template: `
    <app-comp-a></app-comp-a>
    <<warning descr="Unknown html tag foo-bar">foo-bar</warning>></<warning descr="Unknown html tag foo-bar">foo-bar</warning>>
  `,
})
export default class CompBComponent { }
