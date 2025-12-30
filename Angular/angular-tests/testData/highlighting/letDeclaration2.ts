import {Component} from '@angular/core';

@Component({
   template: `
        <!--\`Property 'value' does not exist on type 'Main'.-->
        @let <warning descr="Unused constant value"><weak_warning descr="TS6133: 'value' is declared but its value is never read.">value</weak_warning></warning> = 1 ;
        {{ this.<error descr="TS2339: Property 'value' does not exist on type 'Case1'.">value</error> }}
    `,
   standalone: true,
})
export class Case1 {
}


@Component({
  template: `
        <!-- \`Cannot read @let declaration 'value' before it has been defined.\`, -->
        @let value = <error descr="Cannot read @let declaration value before it has been defined">value</error> ;
        {{ value }}
    `,
  standalone: true,
})
export class Case2 {
  <warning descr="Unused field value">value</warning>!: number
}

@Component({
 template: `
      <!--Cannot read @let declaration 'value' before it has been defined.-->
      @let <weak_warning descr="TS6133: 'value' is declared but its value is never read.">value</weak_warning> = <error descr="Cannot read @let declaration value before it has been defined">value</error>.a.b.c ;
      @if (true) {
        <!-- Cannot read @let declaration 'value' before it has been defined. -->
        @let value = <error descr="Cannot read @let declaration value before it has been defined">value</error> ;
        {{ value }}
      }
  `,
 standalone: true,
})
export class Case3 {
}

@Component({
   template: `
        <!--Cannot read @let declaration 'value' before it has been defined.-->
        @let value = <error descr="Cannot read @let declaration value before it has been defined">value</error>() ;
        {{ value }}
    `,
   standalone: true,
 })
export class Case4 {
}

@Component({
   template: `
        @let value = 1 ;
        <!--Property 'value' does not exist on type 'Main'.-->
        <button (click)="this.<error descr="TS2339: Property 'value' does not exist on type 'Case5'.">value</error> = 2">Click me</button>
        {{ value }}
    `,
   standalone: true,
 })
export class Case5 {
}
