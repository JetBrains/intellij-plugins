@Component({
  selector: 'my-app', 
  template: require('test006.html')
})
export class AppComponent {
  title = 'Tour of Heroes';
  heroes = [];
  selectedHero = { firstName: "eee" }
}
