import {
  Component,
  Directive,
  HostBinding,
  HostListener,
  Input,
} from '@angular/core';
import {NgClass} from "@angular/common";

@Directive({
   selector: '[appClicks]',
   standalone: true,
   host: {
     '[title]': 'foo<error descr="Host binding expression cannot contain pipes"> </error>| bar',
     '(click)': 'onClick(); <error descr="TS2349: This expression is not callable.
  Type 'String' has no call signatures.">foo</error>()'
   }
 })
export abstract class AppClicksDirective {
  foo!: string
  onClick() {
    console.log('Click');
  }
}

@Component({
   selector: 'oy-chip',
   template: `
        <div [class.oy-chip--small]="small"></div>
        <div [ngClass]="{'oy-chip--small' : <error descr="TS2339: Property 'big' does not exist on type 'ChipComponent'.">big</error>}"></div>
    `,
   standalone: true,
   styles: `
      .oy-chip {
        &.oy-chip--small {
        }
      }
   `,
   host: {
     "class": "oy-chip oy-chip--small <warning descr="Unrecognized name">some</warning>",
     "[class.oy-chip--small]": "<error descr="TS2339: Property 'big' does not exist on type 'ChipComponent'.">big</error>",
     '(keydown)': 'onKeyDown($event)',
     '(<warning descr="Unrecognized event">foo</warning>)': '<error descr="TS2339: Property 'bar' does not exist on type 'ChipComponent'.">bar</error>($event)'
   },
   imports: [
     NgClass
   ]
 })
export class ChipComponent {
  @HostBinding("class.oy-chip--small")
  @Input('small')
  public small: boolean = false;

  @HostListener('keydown', ['$event'])
  onKeyDown(<warning descr="Unused parameter $event"><weak_warning descr="TS6133: '$event' is declared but its value is never read.">$event</weak_warning></warning>: string) {
  }
}
