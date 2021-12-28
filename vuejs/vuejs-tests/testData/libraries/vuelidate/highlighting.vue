<template>
    <div>
        <div class="form-group" :class="{ 'form-group--error': $v.name.$error }">
            <label class="form__label">Name</label>
            <input class="form__input" v-model.trim="name" @input="setName($event.target.value)"/>
        </div>
        <div class="error" v-if="!$v.name.required">Field is required</div>
        <div class="error" v-if="!$v.name.minLength">Name must have at least {{$v.name.$params.minLength.min}} letters.
        </div>
        <div class="form-group" :class="{ 'form-group--error': $v.age.$error }">
            <label class="form__label">Age</label>
            <input class="form__input" :value="age" @change="setAge($event.target.value)"/>
        </div>
        {{$v.brb.$params.between.min}}
        <div class="error"
             v-if="!$v.age.between">Must be between {{$v.age.$params.between.min}} and {{$v.age.$params.between.max}}
        </div>
        <span tabindex="0">Blur to see changes</span>
    </div>
</template>

<script>
import {required, minLength, between} from 'vuelidate/lib/validators'

const fake = {
    age: 12
}

export default {
    name: "basicForm",
    props: {
      typed: String
    },
    data() {
        return {
            name: '',
            age: 0
        }
    },
    validations: {
        name: {
            required,
            minLength: minLength(4)
        },
        age: {
            between: between(20, 30)
        }
    },

    methods: {
        setName(value) {
            this.name = value
            this.$v.name.$touch()
        },
        setAge(value) {
            this.age = value
            this.$v.age.$touch()
            this.$v.age.<weak_warning descr="Method expression is not of Function type">$foo</weak_warning>()
        }
    }
}
</script>
