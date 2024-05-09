import { Component, Directive, Input, Pipe, TemplateRef, ViewContainerRef } from '@angular/core';
import { NgClass, NgIf, NgIfContext } from '@angular/common';
import { RouterOutlet } from '@angular/router';

@Directive({
   standalone: true,
   selector: "ng-template[ngIf]"
 })
export class TestIf<T> {
  constructor(_viewContainer: ViewContainerRef, templateRef: TemplateRef<NgIfContext<T>>) {

  }

  @Input()
  set ngIf(condition: T) {
  }

  @Input()
  set ngIfThen(templateRef: TemplateRef<NgIfContext<T>> | null) {
  }

  static ngTemplateGuard_ngIf: 'binding';

  static ngTemplateContextGuard<T>(dir: NgIf<T>, ctx: any): ctx is NgIfContext<number> {
    return true
  }
}

@Directive({
 standalone: true,
 selector: "[foo]"
})
export class TestFoo<T extends number> {
  @Input()
  set foo(condition: T) {
  }

  @Input()
  set foo2(condition: T) {
  }
}

@Pipe({
  standalone: true,
  name: "thePipe"
})
export class MyPipe {

  transform(i: number): string  {
    return ""
  }

}

type mytype = string

@Component({
 selector: 'app-root',
 standalone: true,
 imports: [RouterOutlet, TestIf, NgClass, TestFoo, MyPipe],
 templateUrl: "template.html",
})
export class App<caret>Component<T extends mytype> {
  title = 'My Awesome Signal Store';
  minutes!: number;

  protected check(arg: T) {
  }
}
