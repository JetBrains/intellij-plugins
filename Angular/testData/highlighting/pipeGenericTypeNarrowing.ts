import { Component, Input, Pipe, PipeTransform } from '@angular/core';

export type Box<T> = { value: T };

@Pipe({
  name: 'badUnbox',
  standalone: true,
})
export class BadUnboxPipe implements PipeTransform {
  transform = <T>(box: Box<T>): T => box.value;
}

@Pipe({
  name: 'goodUnbox',
  standalone: true,
})
export class GoodUnboxPipe implements PipeTransform {
  transform = <T>(_: '', box: Box<T>): T => box.value;
}

@Component({
 selector: 'app-child',
 standalone: true,
 template: `{{ value }}`,
})
export class ChildComponent {
  @Input({ required: true }) value?: 'foo';
}

@Component({
 selector: 'app-root',
 standalone: true,
 template: `
        <app-child [value]="{ value: 'foo' } | badUnbox" />
        <app-child <error descr="TS2322: Type '\"bar\"' is not assignable to type '\"foo\"'.">[value]</error>="{ value: 'bar' } | badUnbox" />
        <app-child <error descr="TS2322: Type 'string' is not assignable to type '\"foo\"'.">[value]</error>="({ value: {foo: 'foo'} } | badUnbox).foo" />
        <app-child <error descr="TS2322: Type 'string' is not assignable to type '\"foo\"'.">[value]</error>="({ value: {foo: 'bar'} } | badUnbox).foo" />
        <app-child [value]="'' | goodUnbox: { value: 'foo' }" />
        <app-child <error descr="TS2322: Type '\"bar\"' is not assignable to type '\"foo\"'.">[value]</error>="'' | goodUnbox: { value: 'bar' }" />
    `,
 imports: [ChildComponent, BadUnboxPipe, GoodUnboxPipe],
})
export class ParentComponent { }