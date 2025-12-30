import {Component, Directive, OnInit, ElementRef, EventEmitter, HostListener, Output, Input} from '@angular/core';

// @ts-ignore
@Directive({
   selector: '[appBold]',
   standalone: true,
   exportAs: "bold"
 })
export class BoldDirective {
  @Input()
  weight: Number = 12

  @Output
  hover = new EventEmitter()
}

@Directive({
             selector: '[appUnderline]',
             standalone: true,
             exportAs: "bold",
           })
export class UnderlineDirective {
  @Input()
  color: String = "black"
}

@Directive({
   selector: '[appMouseenter]',
   standalone: true,
   exportAs: "mouseenter",
   hostDirectives: [{
     directive: UnderlineDirective,
     inputs: ["color: underlineColor"],
   },{
     directive: BoldDirective,
     inputs: ['weight: fontWeight'],
     outputs: ["hover: boldHover"]
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
      <app-test <caret>></app-test>
   `,
   hostDirectives: [MyDirective],
 })
export class TestComponent implements OnInit {


  constructor() {
  }

  ngOnInit(): void {
  }

}
