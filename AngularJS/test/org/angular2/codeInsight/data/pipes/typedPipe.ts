import {Component, NgModule, Pipe, PipeTransform} from '@angular/core';

export function myTransform(
  value: string | number,
  <warning descr="Unused parameter options">options</warning>: { formatOptions: Intl.NumberFormatOptions }
): string {
  return value.toString();
}

@Pipe({ name: 'numberFormat' })
export class MyCustomPipe implements PipeTransform {
  public transform: typeof myTransform = myTransform;
}

@Component({
    selector: 'my-app',
    template: `
    <p>
      Start editing to see some magic happen :)
      {{ 123 | numberFormat : <error descr="Argument type {yes: boolean} is not assignable to parameter type {formatOptions: Intl.NumberFormatOptions}">{ yes: true }</error> }}
    </p>`,
})
export class AppComponent  {
}

@NgModule({
  declarations: [AppComponent, MyCustomPipe],
  bootstrap: [AppComponent]
})
export class AppModule {}
