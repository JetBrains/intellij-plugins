import {Component} from "@angular/core";

@Component({
  template: `
    {{ a = 12 }}
    {{ a += 12 }}
    {{ a &&= 12 }}
    {{ a &= 12 }}
    
    <div (click)="a = 12"></div>
    <div (click)="a += 12"></div>
    <div (click)="a &&= 12"></div>
    <div (click)="a &= 12"></div>
    
    <div [title]="a = 12"></div>
    <div [title]="a += 12"></div>
    <div [title]="a &&= 12"></div>
    <div [title]="a &= 12"></div>
  `
})
export class MyDirective {
  a: number
}