import { Component, Pipe, PipeTransform } from '@angular/core';

export class BasePipe implements PipeTransform {
  transform(value: unknown): unknown {
    return value;
  }
}

@Pipe({
  name: 'foo',
  standalone: true
})
export class FooPipe extends BasePipe {
}

@Pipe({
  name: 'bar',
  standalone: true
})
export class BarPipe extends BasePipe {
}

@Component({
 selector: 'app-root',
 imports: [FooPipe, <error descr="Pipe BarPipe is never used in a component template">BarPipe</error>],
 standalone: true,
 template: `{{ 'hello' | foo }}`
})
export class AppComponent {
}