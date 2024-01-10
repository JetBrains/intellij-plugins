import {Component} from '@angular/core';

@Component({   
  selector: 'my-appww23',  
  template: `<div [style.font-size]="title ? 'medium' : 'small'"></div>`
})       
export class AppComponent {    
  title = 'Tour of Heroes'; 
  heroes = HEROES;
  selectedHero = {firstName: "eee"};
  @Input() ae = 5;
  @Input('eb') be = 5;

  @Output() ce = 5;
  @Output('ed') de = 5;
}
