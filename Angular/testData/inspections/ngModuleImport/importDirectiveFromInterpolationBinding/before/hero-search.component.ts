import { Component } from '@angular/core';
import { NgFor } from '@angular/common';

@Component({
    selector: 'app-hero-search',
    templateUrl: './hero-search.component.html',
    standalone: true,
    imports: [NgFor]
})
export class HeroSearchComponent {
  hero!: { name: string };

  constructor() {}

}
