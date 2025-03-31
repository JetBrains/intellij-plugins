import {Component, Directive, Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'myPipe'})
export class MyPipePipeMock implements PipeTransform {
  transform(value: unknown, ...args: unknown[]): unknown {return null;}
}

@Pipe({name: 'myPipe'})
export class MyPipePipe implements PipeTransform {
  transform(value: unknown, ...args: unknown[]): unknown {return null;}
}

@Pipe({name: 'myPipe2'})
export class MyPipePipe2 implements PipeTransform {
  transform(value: unknown, ...args: unknown[]): unknown {return null;}
}

@Directive({selector: '[foo]'})
export class Foo {}

@Directive({selector: '[foo]'})
export class FooMock {}

@Directive({selector: '[bar]'})
export class Bar {}

@Directive({selector: '[bar]'})
export class BarMock {}

@Component({
  selector: 'app-my-component',
  imports: [
    MyPipePipe,
    <error descr="Pipe MyPipePipe2 is never used in a component template">MyPipePipe2</error>,
    Foo,
    <error descr="Directive Bar is never used in a component template">Bar</error>
  ],
  templateUrl: './unused-imports-multiple-components-same-template.html',
})
export class MyComponentComponent {

}

@Component({
  selector: 'app-my-component',
  imports: [
    MyPipePipeMock,
    <error descr="Pipe MyPipePipe2 is never used in a component template">MyPipePipe2</error>,
    FooMock,
    <error descr="Directive BarMock is never used in a component template">BarMock</error>,
  ],
  templateUrl: './unused-imports-multiple-components-same-template.html',
})
export class MyComponentComponentTesting {

}
