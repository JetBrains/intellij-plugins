import {Component, Directive, Input} from '@angular/core';

// @ts-ignore
/**
 * This is the bold directive
 */
@Directive({
   selector: '[appBold]',
   standalone: true,
   exportAs: "bold"
 })
export class BoldDirective {
}

@Directive({
   selector: '[appUnderline]',
   standalone: true,
   exportAs: "underline"
 })
export class UnderlineDirective<C> {
  @Input() color: C;
}
@Directive({
   selector: '[appMouseenter]',
   standalone: true,
   hostDirectives: [{
     directive: BoldDirective,
     outputs: ['hover']
   }, {
     directive: UnderlineDirective,
     inputs: ['color: underlineColor']
   }],
   exportAs: "underline,bold,mouseenter"
 })
export class MouseenterDirective<M> {
  @Input()
  mouse: M
}

@Component({
   standalone: true,
   selector: 'app-test',
   template: `
      <app-test [underlineColor]="12"  ref-a="<caret>"></app-test>
   `,
   hostDirectives: [MouseenterDirective],
   exportAs: "bold,mouseenter,test"
 })
export class TestComponent {
}
