<template>
  <NuxtLink to="/declaration/insideDeclaration"/>
  <NuxtLink to="/doesnExist"/>
  <NuxtLink :to="{ name: 'jobs-job-id-summary'}"/>
  <NuxtLink :to="`/doesnExist`"/>
</template>