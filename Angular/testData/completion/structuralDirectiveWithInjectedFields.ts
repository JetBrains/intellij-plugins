import {
  Component,
  Directive,
  inject,
  ViewContainerRef,
} from '@angular/core';

@Directive({
  selector: '[fooBar1]',
})
export class FooBar1 {

}

@Directive({
  selector: '[fooBar2]',
})
export class FooBar2 {
  templateRef = inject(ViewContainerRef)
}

@Component({
 selector: 'app-root',
 template: `{{ <div *foo<caret> }}`,
 standalone: true,
})
export class AppComponent {
}
