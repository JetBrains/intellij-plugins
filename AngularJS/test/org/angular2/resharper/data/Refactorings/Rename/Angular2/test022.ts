import {Component} from '@angular/core';

@Component({
  selector: 'my-appw{caret}w',
  template: `<div [style.font-size]="title ? 'medium' : 'small'"></div>`
})
export class AppComponent {
  title = 'Tour of Heroes';
  heroes = HEROES;
  selectedHero = {firstName: "eee"}
}
