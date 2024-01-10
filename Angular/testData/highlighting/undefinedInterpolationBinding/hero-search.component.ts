import { Component } from '@angular/core';

@Component({
    selector: 'app-hero-search',
    templateUrl: './hero-search.component.html',
    standalone: true
})
export class HeroSearchComponent {
  hero!: { name: string };

  constructor() {}

}
