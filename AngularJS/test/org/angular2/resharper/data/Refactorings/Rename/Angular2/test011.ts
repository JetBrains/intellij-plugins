import {Component} from "@angular/core"

@Component({
  selector: 'my-app', 
  template: `
   <input #phone{caret} placeholder="phone number">
   <button (click)="callPhone(phone.value)">Call</button> 

   <input ref-fax placeholder="fax number">
   <button (click)="callFax(fax.value)">Fax</button>
  `
})
export class AppComponent {
  title = 'Tour of Heroes';
  heroes = [];
  selectedHero = { firstName: "eee" }
}
