import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-svg-completion',
  templateUrl: './svg-completion.component.svg'
})
export class SvgHighlightingComponent implements OnInit {

  @Input()
  height: number;

  items: {width: number, foo: string}[];

  constructor() { }

  ngOnInit() {
  }

}
