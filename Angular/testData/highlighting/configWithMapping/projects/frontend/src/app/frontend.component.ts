import { Component, inject, Input } from '@angular/core';
import { BaseEntity, ChildEntity, EntityContainer, SharedService } from 'shared';

@Component({
  selector: 'app-frontend',
  standalone: false,
  templateUrl: 'frontend.component.html',
})
export class FrontendComponent {

  readonly sharedService = inject(SharedService);

  trackById(_index: number, entity: BaseEntity) {
    return entity.id;
  }

  @Input() name: string = 'world';
  @Input() childContainer: EntityContainer<ChildEntity> = { contents: [] };
  @Input() column: string = 'name';

  count = 0;
}
