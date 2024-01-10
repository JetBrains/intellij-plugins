import {Component} from '@angular/core';

@Component({
  selector: 'my-app22',  
  template: `<div [style.font-size]="title ? 'large' : 'small'"></div>`
})      
export class AppComponent {    
  title = 'Tour of Heroes'; 
  heroes = HEROES;
  selectedHero = {firstName: "eee"}
}


   
