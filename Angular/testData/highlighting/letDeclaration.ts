import { Component } from '@angular/core';
import { NgIf } from '@angular/common';

@Component({
 selector: 'app-root',
 standalone: true,
 imports: [NgIf],
 template: `
      @let topLevel = value;
      <div>
        @let insideDiv = value;
      </div>
      {{topLevel}} <!-- Valid -->
      {{insideDiv}} <!-- Valid -->
      {{checkType(<error descr="TS2345: Argument of type 'number' is not assignable to parameter of type 'string'.">insideDiv</error>)}}
      @if (condition) {
        {{topLevel + insideDiv}} <!-- Valid -->
        @let nested = value;
        @if (condition) {
          {{topLevel + insideDiv + nested}} <!-- Valid -->
        }
      }
      <div *ngIf="condition">
        {{topLevel + insideDiv}} <!-- Valid -->
        @let nestedNgIf = value;
        <div *ngIf="condition">
           {{topLevel + insideDiv + nestedNgIf}} <!-- Valid -->
           <ng-template [ngIf]="true">{{redeclared}}</ng-template><!-- Valid -->
           {{<error descr="Cannot read @let declaration redeclared before it has been defined">redeclared</error>}} <!-- Error, variable used before declaration -->
           <div (click)="redeclared"></div> <!-- Valid -->
           @let redeclared = 12; <!-- Valid, different scope -->
        </div>
        @let redeclared = 12; <!-- Valid, different scope -->
        {{checkType(<error descr="TS2345: Argument of type 'number' is not assignable to parameter of type 'string'.">redeclared</error>)}}
      </div>
      @let <error descr="Cannot declare @let called redeclared as there is another symbol in the same template scope with the same name.">redeclared = 12</error>; <!-- Error, redeclared  -->
      @let <error descr="Cannot declare @let called redeclared as there is another symbol in the same template scope with the same name."><weak_warning descr="TS6133: 'redeclared' is declared but its value is never read.">redeclared</weak_warning> = 12</error>; <!-- Error, redeclared -->
      {{checkType(<error descr="TS2345: Argument of type 'number' is not assignable to parameter of type 'string'.">redeclared</error>)}}
      {{<error descr="TS2339: Property 'nested' does not exist on type 'ParentComponent'.">nested</error>}} <!-- Error, not hoisted from @if -->
      {{<error descr="TS2339: Property 'nestedNgIf' does not exist on type 'ParentComponent'.">nestedNgIf</error>}} <!-- Error, not hoisted from *ngIf -->
    `,
})
export class ParentComponent {

  value!: number
  condition!: boolean

  checkType(_arg: string) {

  }

}