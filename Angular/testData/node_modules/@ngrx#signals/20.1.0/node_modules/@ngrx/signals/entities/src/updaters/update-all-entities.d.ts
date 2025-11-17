import { PartialStateUpdater } from '@ngrx/signals';
import { EntityChanges, EntityId, EntityState, NamedEntityState, SelectEntityId } from '../models';
export declare function updateAllEntities<Collection extends string, State extends NamedEntityState<any, Collection>, Entity = State extends NamedEntityState<infer E, Collection> ? E : never>(changes: EntityChanges<NoInfer<Entity>>, config: {
    collection: Collection;
    selectId: SelectEntityId<NoInfer<Entity>>;
}): PartialStateUpdater<State>;
export declare function updateAllEntities<Collection extends string, State extends NamedEntityState<any, Collection>, Entity = State extends NamedEntityState<infer E extends {
    id: EntityId;
}, Collection> ? E : never>(changes: EntityChanges<NoInfer<Entity>>, config: {
    collection: Collection;
}): PartialStateUpdater<State>;
export declare function updateAllEntities<Entity>(changes: EntityChanges<NoInfer<Entity>>, config: {
    selectId: SelectEntityId<NoInfer<Entity>>;
}): PartialStateUpdater<EntityState<Entity>>;
export declare function updateAllEntities<Entity extends {
    id: EntityId;
}>(changes: EntityChanges<NoInfer<Entity>>): PartialStateUpdater<EntityState<Entity>>;
