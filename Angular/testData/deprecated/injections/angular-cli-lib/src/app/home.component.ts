import {Component, OnInit} from '@angular/core';

import {Language} from 'angular-l10n';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {
  @Language() lang: string;

  ngOnInit(): void { }

}
