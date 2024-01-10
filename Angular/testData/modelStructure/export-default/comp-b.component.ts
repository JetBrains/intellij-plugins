import { Component } from '@angular/core';
import ModModule from "./mod.module";

@Component({
  selector: 'app-comp-b',
  standalone: true,
  imports: [ModModule],
  template: `
    <app-comp-a></app-comp-a>
  `,
})
export default class CompBComponent { }
