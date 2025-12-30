import {Directive, Component} from '@angular/core';

@Directive({
   standalone: true,
   selector: '[fooBar]',
 })
class FooBarDirective {

}

@Directive({
   standalone: true,
   selector: '[fooBar2]',
 })
class FooBarDirective2 {

}

@Component({
   standalone: true,
   selector: 'the-cmp',
   template: `
     <div *fooBar></div>
   `,
   imports: [
     FooBarDirective,
     <error descr="Directive FooBarDirective2 is never used in a component template">FooBarDirective2</error>
   ]
 })
export class SettingsComponent {
}