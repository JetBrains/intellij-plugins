/* eslint-disable */
import { defineComponent, type PropType } from 'vue';

type RuntimeSlot = {
    config: string;
    data: {
        foo: number;
    };
};
export let Mixin = {
    register,
    getByName,
};

let CmsElementMixin = Mixin.register(
    'cms-element',
    defineComponent({
        props: {
            element: {
                type: Object as PropType<RuntimeSlot>,
                required: true,
            },
        },
    }),
);

interface MixinContainer {
    'cms-element': typeof CmsElementMixin;
}

function register<T, MixinName extends keyof MixinContainer>(mixinName: MixinName, mixin: T): T {
    return {} as T;
}

function getByName<MN extends keyof MixinContainer>(mixinName: MN): MixinContainer[MN] {
    return {} as MixinContainer[MN];
}
