const {PrismaClient} = require('@prisma/client')

const prisma = new PrismaClient()

const req = {};

async function signup() {
    const {name, email, posts} = req.body

    const postData = posts
        ? posts.map((post) => {
            return {title: post.title, content: post.content || undefined}
        })
        : []

    const result = await prisma.us<caret>
}