import { Component } from '@angular/core';

const selector = 'my' + '-element';

@Component({
  selector: selector,
  standalone: true,
  template: ``,
})
export class MyElementComponent {
}

@Component({
  selector: `the-${selector + `s`}`,
  standalone: true,
  template: ``,
})
export class TheMyElementsComponent {
}

@Component({
  selector: "foo-" + selector + `bar`,
  standalone: true,
  template: ``,
})
export class FooMyElementBarComponent {
}
@Component({
  selector: "foo-" + selector + `-` + selector,
  standalone: true,
  template: ``,
})
export class FooMyElementMyElementComponent {
}

@Component({
  selector: 'root-element',
  standalone: true,
  imports: [MyElementComponent, TheMyElementsComponent, FooMyElementBarComponent,FooMyElementMyElementComponent],
  template: `
    <my-element></my-element>
    <the-my-elements></the-my-elements>
    <foo-my-elementbar></foo-my-elementbar>
    <foo-my-element-my-element></foo-my-element-my-element>
    <<warning descr="Unknown html tag your-element">your-element</warning>></<warning descr="Unknown html tag your-element">your-element</warning>>
  `,
})
export class AppComponent {
}