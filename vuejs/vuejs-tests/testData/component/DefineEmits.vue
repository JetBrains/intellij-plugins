<script setup>
defineEmits(['add', 'change', 'delete'])
</script>

<template>
    <div>
        <button @click="$emit(<weak_warning descr="Argument type  string  is not assignable to parameter type  \"add\" | \"change\" | \"delete\"   Type string is not assignable to type \"delete\"    Type string is not assignable to type \"change\"">'remove'</weak_warning>, 12)">Add</button>

        <button @click="$emit('delete', 'identifier', 123)">Add</button>
    </div>
</template>
