import {Component, Directive} from '@angular/core';

// @ts-ignore
@Directive({
   selector: '[appBold]',
   standalone: true,
   exportAs: "bo<caret>ld"
 })
export class BoldDirective {
}

@Directive({
   selector: '[appMouseenter]',
   standalone: true,
   hostDirectives: [BoldDirective],
   exportAs: "bold"
 })
export class MouseenterDirective {
}

@Component({
   standalone: true,
   selector: 'app-test',
   templateUrl: "./template.html",
   hostDirectives: [MouseenterDirective],
   imports: [BoldDirective],
   exportAs: "bold"
 })
export class TestComponent {
}
