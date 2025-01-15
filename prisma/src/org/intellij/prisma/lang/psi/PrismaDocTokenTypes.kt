@file:JvmName("PrismaDocTokenTypes")

// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma.lang.psi

@JvmField
val DOC_COMMENT_START: PrismaTokenType = PrismaTokenType("DOC_COMMENT_START")

@JvmField
val DOC_COMMENT_BODY: PrismaTokenType = PrismaTokenType("DOC_COMMENT_BODY")

@JvmField
val DOC_COMMENT_LEADING_ASTERISK: PrismaTokenType = PrismaTokenType("DOC_COMMENT_LEADING_ASTERISK")

@JvmField
val DOC_COMMENT_END: PrismaTokenType = PrismaTokenType("DOC_COMMENT_END")

@JvmField
val DOC_COMMENT: PrismaDocCommentElementType = PrismaDocCommentElementType()