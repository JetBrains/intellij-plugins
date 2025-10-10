import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  imports: [],
  template: `
    <div (animate.enter)="$event.animationComplete();$event.<error descr="TS2339: Property 'animationEnd' does not exist on type 'AnimationCallbackEvent'.">animationEnd</error>()"></div>
    <div (animate.leave)="$event.animationComplete();$event.<error descr="TS2339: Property 'animationEnd' does not exist on type 'AnimationCallbackEvent'.">animationEnd</error>()"></div>
  `
})
export class App {
}
