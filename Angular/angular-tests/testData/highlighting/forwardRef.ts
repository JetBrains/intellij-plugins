import {Component, forwardRef} from "@angular/core";

@Component({
  selector: 'component-a',
  standalone: true,
  template: `i'm a component A`,
})
export class AComponent {}

@Component({
  selector: 'component-b',
  standalone: true,
  imports: [
    forwardRef(() => AComponent)
  ],
  template: `
    <component-a></component-a>
    <component-b></component-b>
    <<error descr="Component or directive matching component-c element is out of scope of the current template">component-c</error>></component-c>
  `,
})
export class BComponent {}

@Component({
             selector: 'component-c',
             standalone: true,
             template: `i'm a component C`,
           })
export class CComponent {}