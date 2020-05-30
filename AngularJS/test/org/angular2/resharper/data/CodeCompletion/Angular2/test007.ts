import {Component} from "@angular/core"

@Component({
  selector: 'my-app', 
  template: require('test007.html')
})
export class AppComponent {
  title = 'Tour of Heroes';
  heroes = [];
  selectedHero = { firstName: "eee" }
}
