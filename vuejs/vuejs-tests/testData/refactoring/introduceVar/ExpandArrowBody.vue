<template>
</template>

<script>
    export default {
        name: 'DndList',
        components: {draggable},
        props: {
            list1: {
                type: Array,
                default() {
                    return []
                }
            }
        },
        methods: {
            isNotInList1(v) {
                return this.list1.every(k =><selection>v.id !== k.id</selection><caret>)
            }
        }
    }
</script>
