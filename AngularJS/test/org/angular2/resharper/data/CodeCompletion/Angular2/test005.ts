import {Component} from "@angular/core"

@Component({
  selector: 'my-app', 
  templateUrl: 'test005.html'
})
export class AppComponent {
  title = 'Tour of Heroes';
  heroes = [];
  selectedHero = { firstName: "eee" }
}
