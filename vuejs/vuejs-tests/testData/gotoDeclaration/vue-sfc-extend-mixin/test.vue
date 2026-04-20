<template>
    <div :class="classes" :style="style">
        {{ classes }}
    </div>
</template>

<script lang="ts">
    import Mixin from './mixin';

    export default Mixin.extend({
        computed: {
            style() {
                return {
                    background: 'red'
                }
            }
        },

        mounted() {
            this.classes;
        }
    })
</script>