import {Component, Directive, Pipe, PipeTransform} from '@angular/core';
import {NgFor, NgIf} from '@angular/common';

@Pipe({name: 'pipe1',})
export class Pipe1 implements PipeTransform {
  transform(value: string): string {
    return ""
  }
}

@Pipe({name: 'pipe2',})
export class Pipe2 implements PipeTransform {
  transform(value: string): string {
    return ""
  }
}

@Pipe({name: 'pipe3',})
export class Pipe3 implements PipeTransform {
  transform(value: string): string {
    return ""
  }
}

@Pipe({name: 'pipe4',})
export class Pipe4 implements PipeTransform {
  transform(value: string): string {
    return ""
  }
}

@Pipe({name: 'pipe5',})
export class Pipe5 implements PipeTransform {
  transform(value: string): string {
    return ""
  }
}

@Directive({selector: '[dir1]'})
export class Dir1 {}

@Directive({selector: '[dir2]'})
export class Dir2 {}

@Component({
  selector: 'app-root',
  imports: [
    Pipe1,
    Pipe2,
    Pipe3,
    Pipe4,
    <error descr="Pipe Pipe5 is never used in a component template">Pipe5</error>,
    Dir1,
    <error descr="Directive Dir2 is never used in a component template">Dir2</error>,
    NgIf,
    <error descr="Directive NgForOf is never used in a component template">NgFor</error>
  ],
  templateUrl: './unused-imports-in-pug.pug'
})
export class AppComponent {
  title = 'untitled3';
}
