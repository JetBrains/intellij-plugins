// Allows PHPStorm to recognize global components
import WithSlots from './WithSlots.vue';

export default async ({Vue}) => {
    Vue.component('WithSlots', WithSlots)
}
