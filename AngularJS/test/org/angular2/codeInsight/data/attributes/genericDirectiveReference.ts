/* tslint:disable */
import { Component } from '@angular/core';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {MatDatepicker, MatDatepickerModule} from '@angular/material/datepicker';
import {MatInputModule} from "@angular/material/input";

export interface Moment extends Object {

  month(): number;

  year(): number;
}

export function moment(<warning descr="Unused parameter inp">inp</warning>?: string, <warning descr="Unused parameter strict">strict</warning>?: boolean): Moment {
  return {
    month(): number {
      return 0;
    }, year(): number {
      return 0;
    }
  }
}

/** @title Datepicker emulating a Year and month picker */
@Component({
             standalone: true,
             selector: 'datepicker-views-selection-example',
             template: `
               <mat-form-field>
                 <input
                   matInput
                   [matDatepicker]="dp"
                   placeholder="Month and Year"
                   [formControl]="date"
                   [max]="my"
                 />
                 <mat-datepicker-toggle matSuffix [for]="dp"></mat-datepicker-toggle>
                 <mat-datepicker
                   #dp
                   startView="multi-year"
                   (monthSelected)="setMonthAndYear($event, dp)"
                   panelClass="example-month-picker"
                 >
                 </mat-datepicker>
               </mat-form-field>
               <mat-datepicker
                 #dp2
                 [startAt]="12"
                 startView="multi-year"
                 (monthSelected)="setMonthAndYear(<error descr="Argument type number is not assignable to parameter type Moment">$event</error>, <error descr="Argument type MatDatepicker<number> is not assignable to parameter type MatDatepicker<Moment>...  Type (date: number) => void is not assignable to type (date: Moment) => void    Type Moment is not assignable to type number">dp2</error>)"
                 panelClass="example-month-picker"
               />
             `,
             imports: [
               MatDatepickerModule,
               MatInputModule,
               ReactiveFormsModule
             ]
           })
export class DatepickerViewsSelectionExample {
  validFrom = '2022-06-17T09:08:15.382+00:00';
  date = new FormControl(moment(this.validFrom));
  my = moment();

  setMonthAndYear(
    normalizedMonthAndYear: Moment,
    datepicker: MatDatepicker<Moment>
  ) {
    const ctrlValue = this.date.value!;
    this.date.setValue(ctrlValue);
    datepicker.close();
  }
}

