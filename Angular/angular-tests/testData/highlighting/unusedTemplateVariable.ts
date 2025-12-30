import { Component, ViewChild } from '@angular/core';
import {NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    NgForOf,
    NgIf
  ],
  template: `
    @let letUsed = 12; {{ letUsed }}
    @let <warning descr="Unused constant letUnused"><weak_warning descr="TS6133: 'letUnused' is declared but its value is never read.">letUnused</weak_warning></warning> = 12;
    @for (item of [1, 2, 3]; track item; let <warning descr="Unused constant first"><weak_warning descr="TS6133: 'first' is declared but its value is never read.">first</weak_warning></warning> = $first, last = $last) {
      <div #refUnused1></div>
      <div #refUsed1></div>
      
      <div ref-refUnused2></div>
      <div ref-refUsed2></div>
      
      <ng-template let-ngTemplateUsed>{{ngTemplateUsed}}</ng-template>
      <ng-template let-<warning descr="Unused constant ngTemplateUnused"><weak_warning descr="TS6133: 'ngTemplateUnused' is declared but its value is never read.">ngTemplateUnused</weak_warning></warning>></ng-template>{{<error descr="TS2339: Property 'ngTemplateUnused' does not exist on type 'AppComponent'.">ngTemplateUnused</error>}}
      
      <div *ngFor="let forUsed of [1,2,3];">{{forUsed}}</div>
      <div *ngFor="let <warning descr="Unused constant forUnused"><weak_warning descr="TS6133: 'forUnused' is declared but its value is never read.">forUnused</weak_warning></warning> of [1,2,3];"></div>{{<error descr="TS2339: Property 'forUnused' does not exist on type 'AppComponent'.">forUnused</error>}}
      
      <div *ngIf="true as ifUsed">{{ifUsed}}</div>
      <div *ngIf="true as <warning descr="Unused constant ifUnused"><weak_warning descr="TS6133: 'ifUnused' is declared but its value is never read.">ifUnused</weak_warning></warning>"></div>{{<error descr="TS2339: Property 'ifUnused' does not exist on type 'AppComponent'.">ifUnused</error>}}
      
      {{ last }}
    }
  `
})
export class AppComponent {
  <warning descr="Unused field title">title</warning> = 'untitled7';

  @ViewChild('refUsed1')
  foo1!: any

  @ViewChild('refUsed2')
  foo2!: any
}
