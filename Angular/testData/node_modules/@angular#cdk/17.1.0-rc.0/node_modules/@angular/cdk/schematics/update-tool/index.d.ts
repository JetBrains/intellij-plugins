/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import * as ts from 'typescript';
import { FileSystem, WorkspacePath } from './file-system';
import { UpdateLogger } from './logger';
import { MigrationCtor } from './migration';
import { TargetVersion } from './target-version';
/**
 * An update project that can be run against individual migrations. An update project
 * accepts a TypeScript program and a context that is provided to all migrations. The
 * context is usually not used by migrations, but in some cases migrations rely on
 * specifics from the tool that performs the update (e.g. the Angular CLI). In those cases,
 * the context can provide the necessary specifics to the migrations in a type-safe way.
 */
export declare class UpdateProject<Context> {
    /** Context provided to all migrations. */
    private _context;
    /** TypeScript program using workspace paths. */
    private _program;
    /** File system used for reading, writing and editing files. */
    private _fileSystem;
    /**
     * Set of analyzed files. Used for avoiding multiple migration runs if
     * files overlap between targets.
     */
    private _analyzedFiles;
    /** Logger used for printing messages. */
    private _logger;
    private readonly _typeChecker;
    constructor(
    /** Context provided to all migrations. */
    _context: Context, 
    /** TypeScript program using workspace paths. */
    _program: ts.Program, 
    /** File system used for reading, writing and editing files. */
    _fileSystem: FileSystem, 
    /**
     * Set of analyzed files. Used for avoiding multiple migration runs if
     * files overlap between targets.
     */
    _analyzedFiles?: Set<WorkspacePath>, 
    /** Logger used for printing messages. */
    _logger?: UpdateLogger);
    /**
     * Migrates the project to the specified target version.
     * @param migrationTypes Migrations that should be run.
     * @param target Version the project should be updated to. Can be `null` if the set of
     *   specified migrations runs regardless of a target version.
     * @param data Upgrade data that is passed to all migration rules.
     * @param additionalStylesheetPaths Additional stylesheets that should be migrated, if not
     *   referenced in an Angular component. This is helpful for global stylesheets in a project.
     * @param limitToDirectory If specified, changes will be limited to the given directory.
     */
    migrate<Data>(migrationTypes: MigrationCtor<Data, Context>[], target: TargetVersion | null, data: Data, additionalStylesheetPaths?: string[], limitToDirectory?: string): {
        hasFailures: boolean;
    };
    /**
     * Creates instances of the given migrations with the specified target
     * version and data.
     */
    private _createMigrations;
    /**
     * Creates a program form the specified tsconfig and patches the host
     * to read files and directories through the given file system.
     *
     * @throws {TsconfigParseError} If the tsconfig could not be parsed.
     */
    static createProgramFromTsconfig(tsconfigPath: WorkspacePath, fs: FileSystem): ts.Program;
}
