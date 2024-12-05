import { Component, inject, Input } from '@angular/core';
import { SharedService } from './shared.service';
import { BaseEntity, ChildEntity, EntityContainer } from './types';

@Component({
  selector: 'lib-shared',
  standalone: false,
  templateUrl: 'shared.component.html',
})
export class SharedComponent {
  readonly sharedService = inject(SharedService);

  trackById(_index: number, entity: BaseEntity) {
    return entity.id;
  }

  @Input() name: string = 'world';
  @Input() childContainer: EntityContainer<ChildEntity> = { contents: [] };
  @Input() column: string = 'name';

  count = 0;
}
