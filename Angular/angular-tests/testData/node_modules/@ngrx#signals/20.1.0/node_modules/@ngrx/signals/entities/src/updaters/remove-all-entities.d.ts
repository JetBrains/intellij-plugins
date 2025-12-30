import { PartialStateUpdater } from '@ngrx/signals';
import { EntityState, NamedEntityState } from '../models';
export declare function removeAllEntities(): PartialStateUpdater<EntityState<any>>;
export declare function removeAllEntities<Collection extends string>(config: {
    collection: Collection;
}): PartialStateUpdater<NamedEntityState<any, Collection>>;
