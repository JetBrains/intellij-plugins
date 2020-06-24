<!-- simple -->
<template>{{name}}</template>
<template>
    {{long_long_long_long_long_long_long_long_long_long_long_long_long_long_long_long_long_long_long_long_long_long_long_long_long_name}}
</template>

<!-- complex -->
<template>{{12 + 34}}</template>
<template>
    {{1212312312 * 34213123 + 56123123 + 3412321312 * 56123123 - test(very_long_long_long_long_long_long_long_long_identifier) - 3456213211543}}
</template>
<template>{{(function () {
    var test = 45;
    return test;
})()}}
</template>

<!-- check if end delimiter is glued to the interpolation expression - hard wrap at 120 -->
<template>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec velit u
    {{foo_bar_long_long_long_foo_}}
</template>
<template>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec velit u
    {{foo_bar_long_long_long_foo}}
</template>
<template>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec velit u
    {{foo_bar_long_long_long_fo}}
</template>