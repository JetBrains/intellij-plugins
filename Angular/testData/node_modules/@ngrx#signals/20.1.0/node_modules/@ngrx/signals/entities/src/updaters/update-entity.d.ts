import { PartialStateUpdater } from '@ngrx/signals';
import { EntityChanges, EntityId, EntityState, NamedEntityState, SelectEntityId } from '../models';
export declare function updateEntity<Collection extends string, State extends NamedEntityState<any, Collection>, Entity = State extends NamedEntityState<infer E, Collection> ? E : never>(update: {
    id: EntityId;
    changes: EntityChanges<NoInfer<Entity>>;
}, config: {
    collection: Collection;
    selectId: SelectEntityId<NoInfer<Entity>>;
}): PartialStateUpdater<State>;
export declare function updateEntity<Collection extends string, State extends NamedEntityState<any, Collection>, Entity = State extends NamedEntityState<infer E extends {
    id: EntityId;
}, Collection> ? E : never>(update: {
    id: EntityId;
    changes: EntityChanges<NoInfer<Entity>>;
}, config: {
    collection: Collection;
}): PartialStateUpdater<State>;
export declare function updateEntity<Entity>(update: {
    id: EntityId;
    changes: EntityChanges<NoInfer<Entity>>;
}, config: {
    selectId: SelectEntityId<NoInfer<Entity>>;
}): PartialStateUpdater<EntityState<Entity>>;
export declare function updateEntity<Entity extends {
    id: EntityId;
}>(update: {
    id: EntityId;
    changes: EntityChanges<NoInfer<Entity>>;
}): PartialStateUpdater<EntityState<Entity>>;
