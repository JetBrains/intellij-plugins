import { ChangeDetectionStrategy, Component, effect, inject, OnInit } from '@angular/core';
import {
  AsyncPipe,
  CommonModule,
  CurrencyPipe,
  DatePipe,
  DecimalPipe,
  I18nPluralPipe,
  JsonPipe,
  NgForOf,
  NgIf,
  NgPlural,
  NgPluralCase,
  NgStyle,
  PercentPipe,
} from '@angular/common';

export const UNUSED_PSEUDO_MODULE = [NgPlural, NgPluralCase]
export const PARTIALLY_USED_PSEUDO_MODULE = [NgStyle, NgIf]

@Component({
  standalone: true,
  selector: 'cdt-settings',
  templateUrl: './unused-imports-in-standalone-component.html',
  imports: [
    <error descr="Pipe AsyncPipe is never used in a component template">AsyncPipe</error>,
    CurrencyPipe, // incorrectly used in event binding
    DatePipe, // used in an interpolation
    JsonPipe, // used in property binding expression
    PercentPipe, // used in template binding expr
    NgForOf, // template directive
    DecimalPipe, //used in block expression
    PARTIALLY_USED_PSEUDO_MODULE,
    <error descr="None of the declarations provided by UNUSED_PSEUDO_MODULE are used in a component template">UNUSED_PSEUDO_MODULE</error>, //no-spread
    <error descr="Directive NgIf is never used in a component template">NgIf</error>,
    <error descr="Pipe I18nPluralPipe is never used in a component template">I18nPluralPipe</error>,
    CommonModule, // should not be optimized
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SettingsComponent {}


@Component({
 standalone: true,
 selector: 'cdt-settings',
 template: `
    <div (click)="check(12<error descr="Action expression cannot contain pipes"> </error>| async)" [title]="12 | json">
      {{ 12 | date}}
    </div>
    <div *ngFor="let item of [12 | percent, 13]">
      {{ item }}
    </div>
    <div [ngStyle]="{'foo': true}"></div>
    @if (12 | number) {

    }
  `,
 imports: [
   AsyncPipe, // incorrectly used in event binding
   <error descr="Pipe CurrencyPipe is never used in a component template">CurrencyPipe</error>,
   DatePipe, // used in an interpolation
   JsonPipe, // used in property binding expression
   PercentPipe, // used in template binding expr
   NgForOf, // template directive
   ...PARTIALLY_USED_PSEUDO_MODULE,
   ...<error descr="None of the declarations provided by UNUSED_PSEUDO_MODULE are used in a component template">UNUSED_PSEUDO_MODULE</error>, //spread
   DecimalPipe, //used in block expression
   <error descr="Directive NgIf is never used in a component template">NgIf</error>,
   <error descr="Pipe I18nPluralPipe is never used in a component template">I18nPluralPipe</error>,
   CommonModule, // should not be optimized
 ],
 changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SettingsComponent2 {}