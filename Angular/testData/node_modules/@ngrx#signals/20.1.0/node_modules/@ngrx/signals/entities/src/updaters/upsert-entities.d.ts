import { PartialStateUpdater } from '@ngrx/signals';
import { EntityId, EntityState, NamedEntityState, SelectEntityId } from '../models';
export declare function upsertEntities<Entity extends {
    id: EntityId;
}>(entities: Entity[]): PartialStateUpdater<EntityState<Entity>>;
export declare function upsertEntities<Entity, Collection extends string>(entities: Entity[], config: {
    collection: Collection;
    selectId: SelectEntityId<NoInfer<Entity>>;
}): PartialStateUpdater<NamedEntityState<Entity, Collection>>;
export declare function upsertEntities<Entity extends {
    id: EntityId;
}, Collection extends string>(entities: Entity[], config: {
    collection: Collection;
}): PartialStateUpdater<NamedEntityState<Entity, Collection>>;
export declare function upsertEntities<Entity>(entities: Entity[], config: {
    selectId: SelectEntityId<NoInfer<Entity>>;
}): PartialStateUpdater<EntityState<Entity>>;
