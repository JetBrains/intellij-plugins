import {Component, Directive, OnInit, ElementRef, EventEmitter, HostListener, Output, Input} from '@angular/core';

// @ts-ignore
@Directive({
   selector: '[appBold]',
   standalone: true,
   exportAs: "bold",
   hostDirectives: [MyDirective]
 })
export class BoldDirective {
  @Input()
  weight: Number = 12
}

@Directive({
   selector: '[appMouseenter]',
   standalone: true,
   exportAs: "mouseenter",
   hostDirectives: [{
     directive: BoldDirective,
     inputs: ['weight']
   }],
 })
export class MouseenterDirective {
}

@Directive({
   selector: '[mydir]',
   standalone: true,
   exportAs: "mydir",
   hostDirectives: [MouseenterDirective],
 })
export class MyDirective {
}

@Component({
   standalone: true,
   selector: 'app-test',
   template: `
      <app-test ref-a="" [we]></app-test>
   `,
   hostDirectives: [MyDirective],
 })
export class TestComponent implements OnInit {


  constructor() {
  }

  ngOnInit(): void {
  }

}
