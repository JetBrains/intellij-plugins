import { defineConfig, env } from 'prisma/config'
import 'dotenv/config'

export default defineConfig({
    schema: 'prisma/schema/subDir',
    migrations: {
        path: 'prisma/migrations',
        seed: 'tsx ./prisma/seed.ts',
    },
    datasource: {
        url: env('DATABASE_URL'),
        shadowDatabaseUrl: env('SHADOW_DATABASE_URL'),
    },
})
