import {Component} from 'angular2/core';
      
@Component({   
  selector: 'my-appww23',  
  template: `<div [style.font-size]="title ? 'medium' : 'small'"></div>`,
  inputs: ['ae:ea', 'be'],
  outputs: ['ce', 'de']
})       
export class AppComponent {    
  title = 'Tour of Heroes'; 
  heroes = HEROES;
  selectedHero = {firstName: "eee"}
}

