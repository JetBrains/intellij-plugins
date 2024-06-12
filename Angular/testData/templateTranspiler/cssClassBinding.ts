import { Component } from '@angular/core';
import { NgForOf } from "@angular/common";

@Component({
   selector: 'app-root',
   imports:[NgForOf],
   template: `
   <li
    attr="value" 
    *ngFor="let hero of heroes as test"
    [class.selected]="hero === selectedHero"
    (click)="onSelect(hero)"
   ></li>
   `,
   standalone: true,
})
export class App<caret>Component {
  heroes: string[];
  selectedHero: string;
  hero: number;
}
