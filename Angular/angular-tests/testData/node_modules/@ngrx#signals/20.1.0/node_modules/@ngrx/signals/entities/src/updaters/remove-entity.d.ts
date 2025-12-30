import { PartialStateUpdater } from '@ngrx/signals';
import { EntityId, EntityState, NamedEntityState } from '../models';
export declare function removeEntity(id: EntityId): PartialStateUpdater<EntityState<any>>;
export declare function removeEntity<Collection extends string>(id: EntityId, config: {
    collection: Collection;
}): PartialStateUpdater<NamedEntityState<any, Collection>>;
