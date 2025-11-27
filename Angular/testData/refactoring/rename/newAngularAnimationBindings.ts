import {Component, signal} from '@angular/core';
import {ShowcaseComponent} from './showcase';

@Component({
  selector: 'my-component',
  template: `
    <div animate.enter="foo"></div>
    <div animate.leave="foo"></div>
    <div [animate.enter]="'foo'"></div>
    <div [animate.leave]="'foo'"></div>
    <div [animate.enter]="'bar foo'"></div>
    <div [animate.leave]="'bar foo'"></div>
    <div [animate.enter]="['bar','foo']"></div>
    <div [animate.leave]="['bar','foo']"></div>
    <div [animate.enter]="getClass('foo')"></div>
    <div [animate.leave]="getClass('foo')"></div>
    <div class="foo"></div>
    <div title="foo"></div>
    <div [title]="'foo'"></div>
  `,
  styles: `
    .fo<caret>o {
      text-align: center;
    }
    .bar {
      text-align: center;
    }
  `
})
export class MyComponent {
  getClass(name: string) {
    return "foo"
  }
}
