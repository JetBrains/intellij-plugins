<template>
  <img src="<warning descr="Cannot resolve file 'a.svg'">a.svg</warning>"/>
  <img src="/<warning descr="Cannot resolve file 'a.svg'">a.svg</warning>"/>
  <img src="<warning descr="Cannot resolve file 'b.svg'">b.svg</warning>"/>
  <img src="/b.svg"/>
</template>