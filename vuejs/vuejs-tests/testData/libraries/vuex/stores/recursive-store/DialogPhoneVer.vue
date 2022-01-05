<template>
  <A11yDialog
    :id="dialogId"
    :is-title-hidden="true"
    @dialog-ref="(dialog) => bindDialog(dialogId, dialog)"
    @hide="onHide"
  >
    <transition appear name="fade" mode="out-in">
      <component
        :is="currentStep"
        :disabled="disabledConditions[currentStep]"
        :is-error="isError"
        @hide="hideDialog(dialogId)"
        @done="handleDone"
        @phoneVerified="$emit('phoneVerified', $event)"
      >
        {{ verbiage[currentStep] || defaultVerbiage[currentStep] }}</component
      >
    </transition>
  </A11yDialog>
</template>

<script>
import { Dialog } from '@/mixins/dialog';
import A11yDialog from '@/components/controls/A11yDialog';
import stepInputNumber from './DialogPhoneVerificationPhoneNumber';
import stepVerificationChannel from './DialogPhoneVerificationChannel';
import stepVerifyCode from './DialogPhoneVerificationCode';
import stepResultMessage from './DialogPhoneVerificationMessage';
import { mapActions, mapGetters } from 'vuex';
import { getErrorMessage } from '~/utils/utils';

export default {
  name: 'DialogPhoneVerification',
  computed: {
    actions() {
      return {
        inputNumber: this.handlePhoneNumber,
        selectChannel: this.handleSelectChannel,
        verifyCode: this.handleSubmitCode,
        resultMessage: () => {},
      };
    },
  },
  methods: {
    ...mapActions({
                    getPhoneVerificationCode: 'self-service-portal/getPhoneVerificationCode',
                    confirmPhoneVerificationCode: 'self-service-portal/confirmPhoneVerificationCode',
                  })
  }
};
</script>
