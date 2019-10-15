<template>
  <div :user-id="userId">User</div>
</template>
<script>
  import props from './props';

  export default {
    name: 'TestComponent',
    props
  };
</script>
