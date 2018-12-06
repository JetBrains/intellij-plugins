import {Component} from 'angular2/core';

@Component({   
  selector: 'my-appww{caret}',  
  template: `<div [style.font-size]="title ? 'medium' : 'small'"></div>`
})      
export class AppComponent {    
  title = 'Tour of Heroes'; 
  heroes = HEROES;
  selectedHero = {firstName: "eee"}
}
