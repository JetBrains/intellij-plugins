import { Component } from '@angular/core';
import { NgFor } from '@angular/common';
import {RouterLink} from "@angular/router";

@Component({
    selector: 'app-hero-search',
    templateUrl: './hero-search.component.html',
    standalone: true,
    imports: [NgFor, RouterLink]
})
export class HeroSearchComponent {
  hero!: { name: string };

  constructor() {}

}
