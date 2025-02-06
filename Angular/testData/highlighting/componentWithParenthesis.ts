import { Component } from '@angular/core';
import {NgClass} from '@angular/common';

@Component(((({
  selector: 'app-root',
  standalone: true,
  imports: [NgClass],
  template: `
    <div [ngClass]="{ 'open': isOpened  }">open</div>
    <div [ngClass]="<error descr="TS2551: Property 'pinnd' does not exist on type 'AppComponent2'. Did you mean 'pinned'?">pinnd</error> ? '<warning descr="Unrecognized name">pin</warning>' : ''">pinned</div>
  `,
}))))
export class AppComponent2 {
  <warning descr="Unused field pinned">pinned</warning> = true;
  isOpened = true;
}