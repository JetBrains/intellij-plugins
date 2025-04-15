import {Component, Directive} from '@angular/core';

@Directive({
  selector: 'example-dir'
})
export class Dir1Directive {
}

@Component({
  selector: 'app-test',
  template: `
    <ng-content select="example-dir"></ng-content>
  `
})
export class ContentComponent {
}

@Component({
  selector: 'app-root',
  imports: [ContentComponent],
  template: `
    <app-test>
      <<error descr="Component or directive matching example-dir element is out of scope of the current template">example-dir</error>></example-dir>
    </app-test>
  `,
})
export class AppComponent1 {
}

@Component({
  selector: 'app-root',
  imports: [ContentComponent, Dir1Directive],
  template: `
    <app-test>
      <example-dir></example-dir>
    </app-test>
  `,
})
export class AppComponent2 {
}
