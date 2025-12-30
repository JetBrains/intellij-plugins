@Component({
  selector: 'my-app', 
  template: `
    <h2> My Heroes </h2>
    <ul class="heroes">                
      <li *ngFor ="let hero of heroes; let i = {caret}index; let kk3 = index; trackBy: a"
        [class.selected]="hero === selectedHero"
    (click) = "onSelect(hero)" > 
        <span class="badge" >{{hero.id}}</span> {{hero.name}} {{i + kk3}}
      </li>    
    </ul>                  
  `
})
export class AppComponent {
  title = 'Tour of Heroes';
  heroes = [];
  selectedHero = { firstName: "eee" }
}
