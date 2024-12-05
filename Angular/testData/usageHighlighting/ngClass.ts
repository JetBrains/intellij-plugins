import { Component } from '@angular/core';
import {NgClass} from '@angular/common';

@Component({
 selector: 'app-root',
 standalone: true,
 imports: [NgClass],
 template: `
    <div [ngClass]="{'<usage>pin</usage>': pinned}">pinned</div>
    <div [ngClass]="{<usage>pin</usage>: pinned}">pinned</div>
    <div [ngClass]="['<usage>pin</usage>']">pinned</div>
    <div [ngClass]="[pin]">pinned</div>
    <div [ngClass]="pinned ? '<usage>p<caret>in</usage>' : ''">pinned</div>
  `,
 styles: `
    .<usage>pin</usage> {
      color: #3000b3;
    }
   `
 })
export class AppComponent {
  pinned = true;
  isOpened = true;
}