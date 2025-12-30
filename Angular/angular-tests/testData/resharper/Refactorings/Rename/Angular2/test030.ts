import {Component} from '@angular/core';

@Component({   
  selector: 'my-appww2',  
  template: `<div class="a25" [style.font-size]="title ? 'medium' : 'small'"></div>`,
  outputs: ['ce', 'de:a52']
})        
export class AppComponent {    
  title = 'Tour of Heroes'; 
  heroes = HEROES;
  selectedHero = {firstName: "eee"};
  @Input() ae = 5;
  @Input('ebt') be = 5;
    
  ce = 5;  
  {caret}de = 5;
}

new AppComponent().ce; 
new AppComponent().de; 
