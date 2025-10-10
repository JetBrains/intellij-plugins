import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  imports: [],
  template: `
    <div (animate.enter)="$event.animationComplete()"></div>
    <div (animate.leave)="$event.animationComplete()"></div>
  `
})
export class App {
}
