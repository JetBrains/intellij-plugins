import {Component} from '@angular/core';


@Component({
template: `
  <div title="" <warning descr="Attribute foo is not allowed here">foo</warning>></div>
  <<warning descr="Unknown html tag foo">foo</warning> bar=""></<warning descr="Unknown html tag foo">foo</warning>>
`
})
export class MyComponent {

}