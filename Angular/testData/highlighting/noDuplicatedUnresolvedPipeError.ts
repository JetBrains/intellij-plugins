import {Component} from '@angular/core';
import {NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-goose',
  standalone: true,
  imports: [
    NgForOf,
    NgIf
  ],
  template: `
    <div *ngIf="isOrderHistoryLoading | <error descr="Unresolved pipe async">async</error>">
      <div class="row" *ngFor="let <warning descr="Unused constant order"><weak_warning descr="TS6133: 'order' is declared but its value is never read.">order</weak_warning></warning> of orderHistory" id="order-history"></div>
    </div>`,
  styleUrl: './goose.component.css'
})
export class GooseComponent {
  isOrderHistoryLoading: string | undefined;
  orderHistory: any;
}