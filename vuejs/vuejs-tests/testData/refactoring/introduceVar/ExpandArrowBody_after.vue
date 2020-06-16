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
                return this.list1.every(k => {
                  var created = v.id !== k.id;
                  return created;
                })
            }
        }
    }
</script>
