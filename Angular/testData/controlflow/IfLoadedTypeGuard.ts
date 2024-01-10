import { Component, Directive, Input, TemplateRef, ViewContainerRef } from "@angular/core";

export type Loaded<T> = { type: 'loaded', data: T };

export type Loading = { type: 'loading' };

export type LoadingState<T> = Loaded<T> | Loading;

// noinspection JSUnusedLocalSymbols
@Directive({ selector: '[appIfLoaded]', standalone: true })
export class IfLoadedDirective<T> {
  private isViewCreated = false;

  @Input('appIfLoaded') set state(state: LoadingState<T>) {
    // elided
  }

  constructor(
    private readonly viewContainerRef: ViewContainerRef,
    private readonly templateRef: TemplateRef<unknown>
  ) {}

  static ngTemplateGuard_appIfLoaded<T>(
    dir: IfLoadedDirective<T>,
    state: LoadingState<T>
  ): state is Loaded<T> {
    return true;
  }
}


class Hero {
  name: string = "A";
}

@Component({
  selector: "app-example",
  standalone: true,
  imports: [IfLoadedDirective],
  template: `
    <p>{{ heroLoadingState.<error descr="Unresolved variable data">data</error>.name }}</p>
    <p *appIfLoaded="heroLoadingState">{{ heroLoadingState.data.name }}</p>
    <p *appIfLoaded="heroLoadingState">{{ heroLoadingState.data.<error descr="Unresolved variable data">data</error> }}</p>
    <p>{{ heroLoadingState.<error descr="Unresolved variable data">data</error>.name }}</p>
  `,
})
export class ExampleComponent {
  heroLoadingState: LoadingState<Hero> = { type: "loading" };
}
