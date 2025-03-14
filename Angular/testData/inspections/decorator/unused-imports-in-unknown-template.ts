import { Component, Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'bar',
  standalone: true
})
export class BarPipe implements PipeTransform  {
  transform(value: unknown): unknown {
    return value;
  }
}

const TEMPLATE = ``;

@Component({
 selector: 'app-root',
 imports: [
   BarPipe
 ],
 standalone: true,
 template: TEMPLATE
})
export class AppComponent1 {
}

@Component({
  selector: 'app-root',
  imports: [
    BarPipe
  ],
  standalone: true,
  templateUrl: "./foo"
})
export class AppComponent2 {
}

@Component({
  selector: 'app-root',
  imports: [
    <error descr="Pipe BarPipe is never used in a component template">BarPipe</error>
  ],
  standalone: true,
  template: ``
})
export class AppComponent3 {
}

@Component({
  selector: 'app-root',
  imports: [
    BarPipe
  ],
  standalone: true,
  template: `{{12 | bar}}`
})
export class AppComponent4 {
}