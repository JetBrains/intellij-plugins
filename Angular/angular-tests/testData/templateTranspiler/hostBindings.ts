import {
  Component,
  Directive,
  EventEmitter,
  HostBinding,
  HostListener,
  Input, Output,
} from '@angular/core';
import {NgClass} from "@angular/common";

@Directive({
   selector: '[appClicks]',
   standalone: true,
   host: {
     '(click)': 'onClick(); foo()'
   }
 })
export class AppClicksDirective {
  foo!: string
  onClick() {
    console.log('Click');
  }
}

@Component({
   selector: 'oy-chip',
   template: `
        <div [class.oy-chip--small]="small"></div>
        <div [ngClass]="{'oy-chip--small' : small}"></div>
    `,
   host: {
     "class": "oy-chip oy-chip--small something",
     "[class.oy-chip--small]": "small",
     "[small]": "12",
     '(keydown)': 'onKeyDown($event)',
     '(bar)': 'onKeyDown($event)',
     '(foo)': 'foo($event)'
   },
   imports: [
     NgClass
   ]
 })
export class ChipComponent {
  @HostBinding("class.oy-chip--small")
  @Input('small')
  public small: boolean = false;

  @Output()
  public bar = new EventEmitter<string>()

  @HostListener('keydown', ['$event'])
  onKeyDown($event: string) {
  }
}
