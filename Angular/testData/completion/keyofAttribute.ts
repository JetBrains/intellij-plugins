import { Component, Input } from '@angular/core';

type Point = { x: number; y: number };

@Component({
   standalone: true,
   selector: 'app-item-detail',
   template: `<app-item-detail childItem="<caret>"></app-item-detail>`,
 })
export class ItemDetailComponent {
  @Input() childItem: keyof Point = 'x';
}
