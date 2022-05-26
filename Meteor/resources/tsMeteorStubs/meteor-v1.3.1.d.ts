/** Meteor version 1.3.1 **/


declare module Accounts {

    /**
     * Add an email address for a user. Use this instead of directly
     * updating the database. The operation will fail if there is a different user
     * with an email only differing in case. If the specified user has an existing
     * email only differing in case however, we replace it.
     *
     * @locus Server
     *
     * @param {String} userId - <p>The ID of the user to update.</p>
     * @param {String} newEmail - <p>A new email address for the user.</p>
     * @param {Boolean} [verified] - <p>Optional - whether the new email address should
     * be marked as verified. Defaults to false.</p>
     */
    function addEmail(userId:string, newEmail:string, verified?:boolean):any;


    /**
     * Change the current user's password. Must be logged in.
     *
     * @locus Client
     *
     * @param {String} oldPassword - <p>The user's current password. This is <strong>not</strong> sent in plain text over the wire.</p>
     * @param {String} newPassword - <p>A new password for the user. This is <strong>not</strong> sent in plain text over the wire.</p>
     * @param {function} [callback] - <p>Optional callback. Called with no arguments on success, or with a single <code>Error</code> argument on failure.</p>
     */
    function changePassword(oldPassword:string, newPassword:string, callback?:Function):any;


    /**
     * Create a new user.
     *
     * @locus Anywhere
     *
     * @param {Options} options
     * @param {function} [callback] - <p>Client only, optional callback. Called with no arguments on success, or with a single <code>Error</code> argument on failure.</p>
     */
    function createUser(options:{
                   username?:string;
                   email?:string;
                   password?:string;
                   profile?:any
               },
               callback?:Function):any;


    /**
     * Options to customize emails sent from the Accounts system.
     *
     * @locus Server
     */
    var emailTemplates:any;


    /**
     * Finds the user with the specified email.
     * First tries to match email case sensitively; if that fails, it
     * tries case insensitively; but if more than one user matches the case
     * insensitive search, it returns null.
     *
     * @locus Server
     *
     * @param {String} email - <p>The email address to look for</p>
     */
    function findUserByEmail(email:string):any;


    /**
     * Finds the user with the specified username.
     * First tries to match username case sensitively; if that fails, it
     * tries case insensitively; but if more than one user matches the case
     * insensitive search, it returns null.
     *
     * @locus Server
     *
     * @param {String} username - <p>The username to look for</p>
     */
    function findUserByUsername(username:string):any;


    /**
     * Request a forgot password email.
     *
     * @locus Client
     *
     * @param {Options} options
     * @param {function} [callback] - <p>Optional callback. Called with no arguments on success, or with a single <code>Error</code> argument on failure.</p>
     */
    function forgotPassword(options:{
                       email?:string
                   },
                   callback?:Function):any;

    

    
    


    /**
     * Remove an email address for a user. Use this instead of updating
     * the database directly.
     *
     * @locus Server
     *
     * @param {String} userId - <p>The ID of the user to update.</p>
     * @param {String} email - <p>The email address to remove.</p>
     */
    function removeEmail(userId:string, email:string):any;


    /**
     * Reset the password for a user using a token received in email. Logs the user in afterwards.
     *
     * @locus Client
     *
     * @param {String} token - <p>The token retrieved from the reset password URL.</p>
     * @param {String} newPassword - <p>A new password for the user. This is <strong>not</strong> sent in plain text over the wire.</p>
     * @param {function} [callback] - <p>Optional callback. Called with no arguments on success, or with a single <code>Error</code> argument on failure.</p>
     */
    function resetPassword(token:string, newPassword:string, callback?:Function):any;


    /**
     * Send an email with a link the user can use to set their initial password.
     *
     * @locus Server
     *
     * @param {String} userId - <p>The id of the user to send email to.</p>
     * @param {String} [email] - <p>Optional. Which address of the user's to send the email to. This address must be in the user's <code>emails</code> list. Defaults to the first email in the list.</p>
     */
    function sendEnrollmentEmail(userId:string, email?:string):any;


    /**
     * Send an email with a link the user can use to reset their password.
     *
     * @locus Server
     *
     * @param {String} userId - <p>The id of the user to send email to.</p>
     * @param {String} [email] - <p>Optional. Which address of the user's to send the email to. This address must be in the user's <code>emails</code> list. Defaults to the first email in the list.</p>
     */
    function sendResetPasswordEmail(userId:string, email?:string):any;


    /**
     * Send an email with a link the user can use verify their email address.
     *
     * @locus Server
     *
     * @param {String} userId - <p>The id of the user to send email to.</p>
     * @param {String} [email] - <p>Optional. Which address of the user's to send the email to. This address must be in the user's <code>emails</code> list. Defaults to the first unverified email in the list.</p>
     */
    function sendVerificationEmail(userId:string, email?:string):any;


    /**
     * Forcibly change the password for a user.
     *
     * @locus Server
     *
     * @param {String} userId - <p>The id of the user to update.</p>
     * @param {String} newPassword - <p>A new password for the user.</p>
     * @param {Options} [options]
     */
    function setPassword(userId:string,
                newPassword:string,
                options?:{
                    logout?:any
                }):any;


    /**
     * Change a user's username. Use this instead of updating the
     * database directly. The operation will fail if there is an existing user
     * with a username only differing in case.
     *
     * @locus Server
     *
     * @param {String} userId - <p>The ID of the user to update.</p>
     * @param {String} newUsername - <p>A new username for the user.</p>
     */
    function setUsername(userId:string, newUsername:string):any;


    /**
     * Marks the user's email address as verified. Logs the user in afterwards.
     *
     * @locus Client
     *
     * @param {String} token - <p>The token retrieved from the verification URL.</p>
     * @param {function} [callback] - <p>Optional callback. Called with no arguments on success, or with a single <code>Error</code> argument on failure.</p>
     */
    function verifyEmail(token:string, callback?:Function):any;


    /**
     * Set global accounts options.
     *
     * @locus Anywhere
     *
     * @param {Options} options
     */
    function config(options:{
        sendVerificationEmail?:boolean;
        forbidClientAccountCreation?:boolean;
        restrictCreationByEmailDomain?:any;
        loginExpirationInDays?:Number;
        oauthSecretKey?:string
    }):any;


    /**
     * Customize new user creation.
     *
     * @locus Server
     *
     * @param {function} func - <p>Called whenever a new user is created. Return the new user object, or throw an <code>Error</code> to abort the creation.</p>
     */
    function onCreateUser(func:Function):any;


    /**
     * Register a function to call when an email verification link is
     * clicked in an email sent by
     * [`Accounts.sendVerificationEmail`](#accounts_sendverificationemail).
     * This function should be called in top-level code, not inside
     * `Meteor.startup()`.
     *
     * @locus Client
     *
     * @param {function} callback - <p>The function to call. It is given two arguments:</p>
     * <ol>
     * <li><code>token</code>: An email verification token that can be passed to
     * <a href="#accounts_verifyemail"><code>Accounts.verifyEmail</code></a>.</li>
     * <li><code>done</code>: A function to call when the email verification UI flow is complete.
     * The normal login process is suspended until this function is called, so
     * that the user can be notified that they are verifying their email before
     * being logged in.</li>
     * </ol>
     */
    function onEmailVerificationLink(callback:Function):any;


    /**
     * Register a function to call when an account enrollment link is
     * clicked in an email sent by
     * [`Accounts.sendEnrollmentEmail`](#accounts_sendenrollmentemail).
     * This function should be called in top-level code, not inside
     * `Meteor.startup()`.
     *
     * @locus Client
     *
     * @param {function} callback - <p>The function to call. It is given two arguments:</p>
     * <ol>
     * <li><code>token</code>: A password reset token that can be passed to
     * <a href="#accounts_resetpassword"><code>Accounts.resetPassword</code></a> to give the newly
     * enrolled account a password.</li>
     * <li><code>done</code>: A function to call when the enrollment UI flow is complete.
     * The normal login process is suspended until this function is called, so that
     * user A can be enrolled even if user B was logged in.</li>
     * </ol>
     */
    function onEnrollmentLink(callback:Function):any;


    /**
     * Register a callback to be called after a login attempt succeeds.
     *
     * @locus Anywhere
     *
     * @param {function} func - <p>The callback to be called when login is successful.</p>
     */
    function onLogin(func:Function):any;


    /**
     * Register a callback to be called after a login attempt fails.
     *
     * @locus Anywhere
     *
     * @param {function} func - <p>The callback to be called after the login has failed.</p>
     */
    function onLoginFailure(func:Function):any;


    /**
     * Register a function to call when a reset password link is clicked
     * in an email sent by
     * [`Accounts.sendResetPasswordEmail`](#accounts_sendresetpasswordemail).
     * This function should be called in top-level code, not inside
     * `Meteor.startup()`.
     *
     * @locus Client
     *
     * @param {function} callback - <p>The function to call. It is given two arguments:</p>
     * <ol>
     * <li><code>token</code>: A password reset token that can be passed to
     * <a href="#accounts_resetpassword"><code>Accounts.resetPassword</code></a>.</li>
     * <li><code>done</code>: A function to call when the password reset UI flow is complete. The normal
     * login process is suspended until this function is called, so that the
     * password for user A can be reset even if user B was logged in.</li>
     * </ol>
     */
    function onResetPasswordLink(callback:Function):any;


    /**
     * Get the current user id, or `null` if no user is logged in. A reactive data source.
     *
     * @locus Anywhere but publish functions
     */
    function userId():any;


    /**
     * Validate login attempts.
     *
     * @locus Server
     *
     * @param {function} func - <p>Called whenever a login is attempted (either successful or unsuccessful).  A login can be aborted by returning a falsy value or throwing an exception.</p>
     */
    function validateLoginAttempt(func:Function):any;


    /**
     * Set restrictions on new user creation.
     *
     * @locus Server
     *
     * @param {function} func - <p>Called whenever a new user is created. Takes the new user object, and returns true to allow the creation or false to abort.</p>
     */
    function validateNewUser(func:Function):any;

    interface Iui {

        /**
         * Configure the behavior of [`{{> loginButtons}}`](#accountsui).
         *
         * @locus Client
         *
         * @param {Options} options
         */
        config(options:{
                   requestPermissions?:any;
                   requestOfflineToken?:any;
                   forceApprovalPrompt?:any;
                   passwordSignupFields?:string
               }):any;

    }

    var ui:Iui;

}



interface IApp {

    /**
     * Set a new access rule based on origin domain for your app.
     * By default your application has a limited list of servers it can contact.
     * Use this method to extend this list.
     *
     * Default access rules:
     *
     * - `tel:*`, `geo:*`, `mailto:*`, `sms:*`, `market:*` are allowed and
     *   are handled by the system (e.g. opened in the phone app or an email client)
     * - `http://localhost:*` is used to serve the app's assets from.
     * - The domain or address of the Meteor server to connect to for DDP and
     *   hot code push of new versions.
     *
     * Read more about domain patterns in [Cordova
     * docs](http://cordova.apache.org/docs/en/6.0.0/guide_appdev_whitelist_index.md.html).
     *
     * Starting with Meteor 1.0.4 access rule for all domains and protocols
     * (`<access origin="*"/>`) is no longer set by default due to
     * [certain kind of possible
     * attacks](http://cordova.apache.org/announcements/2014/08/04/android-351.html).
     *
     *
     * @param {String} pattern - <p>The pattern defining affected domains or URLs.</p>
     * @param {Options} [options]
     */
    accessRule(pattern:string,
               options?:{
                   type?:string;
                   launchExternal?:boolean
               }):any;


    /**
     * Set the build-time configuration for a Cordova plugin.
     *
     *
     * @param {String} id - <p>The identifier of the plugin you want to
     * configure.</p>
     * @param {Object} config - <p>A set of key-value pairs which will be passed
     * at build-time to configure the specified plugin.</p>
     */
    configurePlugin(id:string, config:any):any;


    /**
     * Set the icons for your mobile app.
     *
     *
     * @param {Object} icons - <p>An Object where the keys are different
     * devices and screen sizes, and values are image paths
     * relative to the project root directory.</p>
     * <p>Valid key values:</p>
     * <ul>
     * <li><code>iphone_2x</code> (120x120)</li>
     * <li><code>iphone_3x</code> (180x180)</li>
     * <li><code>ipad</code> (76x76)</li>
     * <li><code>ipad_2x</code> (152x152)</li>
     * <li><code>ipad_pro</code> (167x167)</li>
     * <li><code>ios_settings</code> (29x29)</li>
     * <li><code>ios_settings_2x</code> (58x58)</li>
     * <li><code>ios_settings_3x</code> (87x87)</li>
     * <li><code>ios_spotlight</code> (40x40)</li>
     * <li><code>ios_spotlight_2x</code> (80x80)</li>
     * <li><code>android_mdpi</code> (48x48)</li>
     * <li><code>android_hdpi</code> (72x72)</li>
     * <li><code>android_xhdpi</code> (96x96)</li>
     * <li><code>android_xxhdpi</code> (144x144)</li>
     * <li><code>android_xxxhdpi</code> (192x192)</li>
     * </ul>
     */
    icons(icons:any):any;


    /**
     * Set your mobile app's core configuration information.
     *
     *
     * @param {Options} options
     */
    info(options:{
             id?:string;
             version?:string;
             name?:string;
             description?:string;
             author?:string;
             email?:string;
             website?:string
         }):any;


    /**
     * Set the launch screen images for your mobile app.
     *
     *
     * @param {Object} launchScreens - <p>A dictionary where keys are different
     * devices, screen sizes, and orientations, and the values are image paths
     * relative to the project root directory.</p>
     * <p>For Android, launch screen images should
     * be special &quot;Nine-patch&quot; image files that specify how they should be
     * stretched. See the <a href="https://developer.android.com/guide/topics/graphics/2d-graphics.html#nine-patch">Android docs</a>.</p>
     * <p>Valid key values:</p>
     * <ul>
     * <li><code>iphone_2x</code> (640x960)</li>
     * <li><code>iphone5</code> (640x1136)</li>
     * <li><code>iphone6</code> (750x1334)</li>
     * <li><code>iphone6p_portrait</code> (1242x2208)</li>
     * <li><code>iphone6p_landscape</code> (2208x1242)</li>
     * <li><code>ipad_portrait</code> (768x1024)</li>
     * <li><code>ipad_portrait_2x</code> (1536x2048)</li>
     * <li><code>ipad_landscape</code> (1024x768)</li>
     * <li><code>ipad_landscape_2x</code> (2048x1536)</li>
     * <li><code>android_mdpi_portrait</code> (320x470)</li>
     * <li><code>android_mdpi_landscape</code> (470x320)</li>
     * <li><code>android_hdpi_portrait</code> (480x640)</li>
     * <li><code>android_hdpi_landscape</code> (640x480)</li>
     * <li><code>android_xhdpi_portrait</code> (720x960)</li>
     * <li><code>android_xhdpi_landscape</code> (960x720)</li>
     * <li><code>android_xxhdpi_portrait</code> (1080x1440)</li>
     * <li><code>android_xxhdpi_landscape</code> (1440x1080)</li>
     * </ul>
     */
    launchScreens(launchScreens:any):any;


    /**
     * Add a preference for your build as described in the
     * [Cordova documentation](http://cordova.apache.org/docs/en/5.1.1/config_ref_index.md.html#The%20config.xml%20File_global_preferences).
     *
     *
     * @param {String} name - <p>A preference name supported by Cordova's
     * <code>config.xml</code>.</p>
     * @param {String} value - <p>The value for that preference.</p>
     * @param {String} [platform] - <p>Optional. A platform name (either <code>ios</code> or <code>android</code>) to add a platform-specific preference.</p>
     */
    setPreference(name:string, value:string, platform?:string):any;

}

declare var App:IApp;


interface IAssets {

    /**
     * Retrieve the contents of the static server asset as an [EJSON Binary](#ejson_new_binary).
     *
     * @locus Server
     *
     * @param {String} assetPath - <p>The path of the asset, relative to the application's <code>private</code> subdirectory.</p>
     * @param {function} [asyncCallback] - <p>Optional callback, which is called asynchronously with the error or result after the function is complete. If not provided, the function runs synchronously.</p>
     */
    getBinary(assetPath:string, asyncCallback?:Function):any;


    /**
     * Retrieve the contents of the static server asset as a UTF8-encoded string.
     *
     * @locus Server
     *
     * @param {String} assetPath - <p>The path of the asset, relative to the application's <code>private</code> subdirectory.</p>
     * @param {function} [asyncCallback] - <p>Optional callback, which is called asynchronously with the error or result after the function is complete. If not provided, the function runs synchronously.</p>
     */
    getText(assetPath:string, asyncCallback?:Function):any;

}

declare var Assets:IAssets;


declare module Blaze {

    /**
     * Constructs a View that renders `contentFunc` for each item in a sequence.
     *
     * @locus Client
     *
     * @param {function} argFunc - <p>A function to reactively re-run. The function can
     * return one of two options:</p>
     * <ol>
     * <li><p>An object with two fields: '_variable' and '_sequence'. Each iterates over
     * '_sequence', it may be a Cursor, an array, null, or undefined. Inside the
     * Each body you will be able to get the current item from the sequence using
     * the name specified in the '_variable' field.</p>
     * </li>
     * <li><p>Just a sequence (Cursor, array, null, or undefined) not wrapped into an
     * object. Inside the Each body, the current item will be set as the data
     * context.</p>
     * </li>
     * </ol>
     * @param {function} contentFunc - <p>A Function that returns  <a href="#renderable_content"><em>renderable
     * content</em></a>.</p>
     * @param {function} [elseFunc] - <p>A Function that returns <a href="#renderable_content"><em>renderable
     * content</em></a> to display in the case when there are no items
     * in the sequence.</p>
     */
    function Each(argFunc:Function, contentFunc:Function, elseFunc?:Function):any;


    /**
     * Constructs a View that renders content conditionally.
     *
     * @locus Client
     *
     * @param {function} conditionFunc - <p>A function to reactively re-run.  Whether the result is truthy or falsy determines whether <code>contentFunc</code> or <code>elseFunc</code> is shown.  An empty array is considered falsy.</p>
     * @param {function} contentFunc - <p>A Function that returns <a href="#renderable_content"><em>renderable content</em></a>.</p>
     * @param {function} [elseFunc] - <p>Optional.  A Function that returns <a href="#renderable_content"><em>renderable content</em></a>.  If no <code>elseFunc</code> is supplied, no content is shown in the &quot;else&quot; case.</p>
     */
    function If(conditionFunc:Function, contentFunc:Function, elseFunc?:Function):any;


    /**
     * Constructs a View setting the local lexical scope in the block.
     *
     *
     * @param {function} bindings - <p>Dictionary mapping names of bindings to
     * values or computations to reactively re-run.</p>
     * @param {function} contentFunc - <p>A Function that returns <a href="#renderable_content"><em>renderable content</em></a>.</p>
     */
    function Let(bindings:Function, contentFunc:Function):any;


    /**
     * Constructor for a Template, which is used to construct Views with particular name and content.
     *
     * @locus Client
     *
     * @param {String} [viewName] - <p>Optional.  A name for Views constructed by this Template.  See <a href="#view_name"><code>view.name</code></a>.</p>
     * @param {function} [renderFunction] - <p>A function that returns <a href="#renderable_content"><em>renderable content</em></a>.  This function is used as the <code>renderFunction</code> for Views constructed by this Template.</p>
     */
    function Template(viewName?:string, renderFunction?:Function):any;


    /**
     * An inverted [`Blaze.If`](#blaze_if).
     *
     * @locus Client
     *
     * @param {function} conditionFunc - <p>A function to reactively re-run.  If the result is falsy, <code>contentFunc</code> is shown, otherwise <code>elseFunc</code> is shown.  An empty array is considered falsy.</p>
     * @param {function} contentFunc - <p>A Function that returns <a href="#renderable_content"><em>renderable content</em></a>.</p>
     * @param {function} [elseFunc] - <p>Optional.  A Function that returns <a href="#renderable_content"><em>renderable content</em></a>.  If no <code>elseFunc</code> is supplied, no content is shown in the &quot;else&quot; case.</p>
     */
    function Unless(conditionFunc:Function, contentFunc:Function, elseFunc?:Function):any;


    /**
     * Constructor for a View, which represents a reactive region of DOM.
     *
     * @locus Client
     *
     * @param {String} [name] - <p>Optional.  A name for this type of View.  See <a href="#view_name"><code>view.name</code></a>.</p>
     * @param {function} [renderFunction] - <p>A function that returns <a href="#renderable_content"><em>renderable content</em></a>.  In this function, <code>this</code> is bound to the View.</p>
     */
    function View(name?:string, renderFunction?:Function):View;


    /**
     * Constructs a View that renders content with a data context.
     *
     * @locus Client
     *
     * @param {Object or function} data - <p>An object to use as the data context, or a function returning such an object.  If a function is provided, it will be reactively re-run.</p>
     * @param {function} contentFunc - <p>A Function that returns <a href="#renderable_content"><em>renderable content</em></a>.</p>
     */
    function With(data:any, contentFunc:Function):any;


    /**
     * The View corresponding to the current template helper, event handler, callback, or autorun.  If there isn't one, `null`.
     *
     * @locus Client
     */
    var currentView:any;


    /**
     * Returns the current data context, or the data context that was used when rendering a particular DOM element or View from a Meteor template.
     *
     * @locus Client
     *
     * @param {DOMElement or Blaze.View} [elementOrView] - <p>Optional.  An element that was rendered by a Meteor, or a View.</p>
     */
    function getData(elementOrView?:any):any;


    /**
     * Gets either the current View, or the View enclosing the given DOM element.
     *
     * @locus Client
     *
     * @param {DOMElement} [element] - <p>Optional.  If specified, the View enclosing <code>element</code> is returned.</p>
     */
    function getView(element?:any):any;


    /**
     * Returns true if `value` is a template object like `Template.myTemplate`.
     *
     * @locus Client
     *
     * @param {Any} value - <p>The value to test.</p>
     */
    function isTemplate(value:any):any;


    /**
     * Removes a rendered View from the DOM, stopping all reactive updates and event listeners on it.
     *
     * @locus Client
     *
     * @param {Blaze.View} renderedView - <p>The return value from <code>Blaze.render</code> or <code>Blaze.renderWithData</code>.</p>
     */
    function remove(renderedView:any):any;


    /**
     * Renders a template or View to DOM nodes and inserts it into the DOM, returning a rendered [View](#blaze_view) which can be passed to [`Blaze.remove`](#blaze_remove).
     *
     * @locus Client
     *
     * @param {Template or Blaze.View} templateOrView - <p>The template (e.g. <code>Template.myTemplate</code>) or View object to render.  If a template, a View object is <a href="#template_constructview">constructed</a>.  If a View, it must be an unrendered View, which becomes a rendered View and is returned.</p>
     * @param {DOMNode} parentNode - <p>The node that will be the parent of the rendered template.  It must be an Element node.</p>
     * @param {DOMNode} [nextNode] - <p>Optional. If provided, must be a child of <em>parentNode</em>; the template will be inserted before this node. If not provided, the template will be inserted as the last child of parentNode.</p>
     * @param {Blaze.View} [parentView] - <p>Optional. If provided, it will be set as the rendered View's <a href="#view_parentview"><code>parentView</code></a>.</p>
     */
    function render(templateOrView:any, parentNode:any, nextNode?:any, parentView?:any):any;


    /**
     * Renders a template or View to DOM nodes with a data context.  Otherwise identical to `Blaze.render`.
     *
     * @locus Client
     *
     * @param {Template or Blaze.View} templateOrView - <p>The template (e.g. <code>Template.myTemplate</code>) or View object to render.</p>
     * @param {Object or function} data - <p>The data context to use, or a function returning a data context.  If a function is provided, it will be reactively re-run.</p>
     * @param {DOMNode} parentNode - <p>The node that will be the parent of the rendered template.  It must be an Element node.</p>
     * @param {DOMNode} [nextNode] - <p>Optional. If provided, must be a child of <em>parentNode</em>; the template will be inserted before this node. If not provided, the template will be inserted as the last child of parentNode.</p>
     * @param {Blaze.View} [parentView] - <p>Optional. If provided, it will be set as the rendered View's <a href="#view_parentview"><code>parentView</code></a>.</p>
     */
    function renderWithData(templateOrView:any, data:any, parentNode:any, nextNode?:any, parentView?:any):any;


    /**
     * Renders a template or View to a string of HTML.
     *
     * @locus Client
     *
     * @param {Template or Blaze.View} templateOrView - <p>The template (e.g. <code>Template.myTemplate</code>) or View object from which to generate HTML.</p>
     */
    function toHTML(templateOrView:any):any;


    /**
     * Renders a template or View to HTML with a data context.  Otherwise identical to `Blaze.toHTML`.
     *
     * @locus Client
     *
     * @param {Template or Blaze.View} templateOrView - <p>The template (e.g. <code>Template.myTemplate</code>) or View object from which to generate HTML.</p>
     * @param {Object or function} data - <p>The data context to use, or a function returning a data context.</p>
     */
    function toHTMLWithData(templateOrView:any, data:any):any;

    interface TemplateInstance {

        /**
         * Find all elements matching `selector` in this template instance, and return them as a JQuery object.
         *
         * @locus Client
         *
         * @param {String} selector - <p>The CSS selector to match, scoped to the template contents.</p>
         */
        $(selector:string):any;


        /**
         * A version of [Tracker.autorun](#tracker_autorun) that is stopped when the template is destroyed.
         *
         * @locus Client
         *
         * @param {function} runFunc - <p>The function to run. It receives one argument: a Tracker.Computation object.</p>
         */
        autorun(runFunc:Function):any;


        /**
         * The data context of this instance's latest invocation.
         *
         * @locus Client
         */
        data:any;


        /**
         * Find one element matching `selector` in this template instance.
         *
         * @locus Client
         *
         * @param {String} selector - <p>The CSS selector to match, scoped to the template contents.</p>
         */
        find(selector:string):any;


        /**
         * Find all elements matching `selector` in this template instance.
         *
         * @locus Client
         *
         * @param {String} selector - <p>The CSS selector to match, scoped to the template contents.</p>
         */
        findAll(selector:string):any;


        /**
         * The first top-level DOM node in this template instance.
         *
         * @locus Client
         */
        firstNode:any;


        /**
         * The last top-level DOM node in this template instance.
         *
         * @locus Client
         */
        lastNode:any;


        /**
         * A version of [Meteor.subscribe](#meteor_subscribe) that is stopped
         * when the template is destroyed.
         *
         * @locus Client
         *
         * @param {String} name - <p>Name of the subscription.  Matches the name of the
         * server's <code>publish()</code> call.</p>
         * @param {Any} [arg1, arg2...] - <p>Optional arguments passed to publisher function
         * on server.</p>
         * @param {function or Object} [options] - <p>If a function is passed instead of an
         * object, it is interpreted as an <code>onReady</code> callback.</p>
         */
        subscribe(name:string, ...args:any[]):any;


        /**
         * A reactive function that returns true when all of the subscriptions
         * called with [this.subscribe](#TemplateInstance-subscribe) are ready.
         *
         */
        subscriptionsReady():any;


        /**
         * The [View](#blaze_view) object for this invocation of the template.
         *
         * @locus Client
         */
        view:any;

    }


    interface View {

    }


}



interface ITemplate {

    /**
     * The [template object](#templates_api) representing your `<body>`
     * tag.
     *
     * @locus Client
     */
    body:TemplateMember;


    /**
     * - Inside an `onCreated`, `onRendered`, or `onDestroyed` callback, returns
     * the data context of the template.
     * - Inside an event handler, returns the data context of the template on which
     * this event handler was defined.
     * - Inside a helper, returns the data context of the DOM node where the helper
     * was used.
     *
     * Establishes a reactive dependency on the result.
     *
     * @locus Client
     */
    currentData():any;


    /**
     * Removes a global [helper function](#template_helpers).
     *
     * @locus Client
     *
     * @param {String} name - <p>The name of the helper function you are defining.</p>
     */
    deregisterHelper(name:string):any;


    /**
     * Choose a template to include dynamically, by name.
     *
     * @locus Templates
     *
     * @param {String} template - <p>The name of the template to include.</p>
     * @param {Object} [data] - <p>Optional. The data context in which to include the
     * template.</p>
     */
    dynamic(template:string, data?:any):any;


    /**
     * The [template instance](#template_inst) corresponding to the current template helper, event handler, callback, or autorun.  If there isn't one, `null`.
     *
     * @locus Client
     */
    instance():Blaze.TemplateInstance;


    /**
     * Accesses other data contexts that enclose the current data context.
     *
     * @locus Client
     *
     * @param {Integer} [numLevels] - <p>The number of levels beyond the current data context to look. Defaults to 1.</p>
     */
    parentData(numLevels?:any):any;


    /**
     * Defines a [helper function](#template_helpers) which can be used from all templates.
     *
     * @locus Client
     *
     * @param {String} name - <p>The name of the helper function you are defining.</p>
     * @param {function} func - <p>The helper function itself.</p>
     */
    registerHelper(name:string, func:Function):any;

}

declare var Template:ITemplate;


interface ICordova {

    /**
     * Specify which [Cordova / PhoneGap](http://cordova.apache.org/)
     * plugins your Meteor package depends on.
     *
     * Plugins are installed from
     * [plugins.cordova.io](http://plugins.cordova.io/), so the plugins and
     * versions specified must exist there. Alternatively, the version
     * can be replaced with a GitHub tarball URL as described in the
     * [Cordova](https://github.com/meteor/meteor/wiki/Meteor-Cordova-integration#meteor-packages-with-cordova-dependencies)
     * page of the Meteor wiki on GitHub.
     *
     * @locus package.js
     *
     * @param {Object} dependencies - <p>An object where the keys are plugin
     * names and the values are version numbers or GitHub tarball URLs
     * in string form.
     * Example:</p>
     * <pre class="prettyprint source lang-js"><code>Cordova.depends({
     *   &quot;org.apache.cordova.camera&quot;: &quot;0.3.0&quot;
     * });</code></pre><p>Alternatively, with a GitHub URL:</p>
     * <pre class="prettyprint source lang-js"><code>Cordova.depends({
     *   &quot;org.apache.cordova.camera&quot;:
     *     &quot;https://github.com/apache/cordova-plugin-camera/tarball/d84b875c449d68937520a1b352e09f6d39044fbf&quot;
     * });</code></pre>
     */
    depends(dependencies:any):any;

}

declare var Cordova:ICordova;


interface IDDP {

    /**
     * Connect to the server of a different Meteor application to subscribe to its document sets and invoke its remote methods.
     *
     * @locus Anywhere
     *
     * @param {String} url - <p>The URL of another Meteor application.</p>
     */
    connect(url:string):any;

}

declare var DDP:IDDP;


declare module DDPCommon {

    /**
     * The state for a single invocation of a method, referenced by this
     * inside a method definition.
     *
     *
     * @param {Options} options
     */
    function MethodInvocation(options:{

                     }):any;

    interface MethodInvocation {

        /**
         * Access inside a method invocation. The [connection](#meteor_onconnection) that this method was received on. `null` if the method is not associated with a connection, eg. a server initiated method call.
         *
         * @locus Server
         */
        connection:any;


        /**
         * Access inside a method invocation.  Boolean value, true if this invocation is a stub.
         *
         * @locus Anywhere
         */
        isSimulation:boolean;


        /**
         * Set the logged in user.
         *
         * @locus Server
         *
         * @param {String or null} userId - <p>The value that should be returned by <code>userId</code> on this connection.</p>
         */
        setUserId(userId:any):any;


        /**
         * Call inside a method invocation.  Allow subsequent method from this client to begin running in a new fiber.
         *
         * @locus Server
         */
        unblock():any;


        /**
         * The id of the user that made this method call, or `null` if no user was logged in.
         *
         * @locus Anywhere
         */
        userId:any;

    }


}



interface IDDPRateLimiter {

    /**
     * Add a rule that matches against a stream of events describing method or
     * subscription attempts. Each event is an object with the following
     * properties:
     *
     * - `type`: Either "method" or "subscription"
     * - `name`: The name of the method or subscription being called
     * - `userId`: The user ID attempting the method or subscription
     * - `connectionId`: A string representing the user's DDP connection
     * - `clientAddress`: The IP address of the user
     *
     * Returns unique `ruleId` that can be passed to `removeRule`.
     *
     * @locus Server
     *
     * @param {Object} matcher - <p>Matchers specify which events are counted towards a rate limit. A matcher
     *   is an object that has a subset of the same properties as the event objects
     *   described above. Each value in a matcher object is one of the following:</p>
     * <ul>
     * <li><p>a string: for the event to satisfy the matcher, this value must be equal
     * to the value of the same property in the event object</p>
     * </li>
     * <li><p>a function: for the event to satisfy the matcher, the function must
     * evaluate to true when passed the value of the same property
     * in the event object</p>
     * </li>
     * </ul>
     * <p>Here's how events are counted: Each event that satisfies the matcher's
     * filter is mapped to a bucket. Buckets are uniquely determined by the
     * event object's values for all properties present in both the matcher and
     * event objects.</p>
     * @param {number} numRequests - <p>number of requests allowed per time interval.
     * Default = 10.</p>
     * @param {number} timeInterval - <p>time interval in milliseconds after which
     * rule's counters are reset. Default = 1000.</p>
     */
    addRule(matcher:any, numRequests:any, timeInterval:any):any;


    /**
     * Removes the specified rule from the rate limiter. If rule had
     * hit a rate limit, that limit is removed as well.
     *
     * @locus Server
     *
     * @param {string} id - <p>'ruleId' returned from <code>addRule</code></p>
     */
    removeRule(id:string):any;


    /**
     * Set error message text when method or subscription rate limit
     * exceeded.
     *
     * @locus Server
     *
     * @param {string or function} message - <p>Functions are passed in an object with a
     * <code>timeToReset</code> field that specifies the number of milliseconds until the next
     * method or subscription is allowed to run. The function must return a string
     * of the error message.</p>
     */
    setErrorMessage(message:any):any;

}

declare var DDPRateLimiter:IDDPRateLimiter;


declare module EJSON {

    /**
     * Add a custom datatype to EJSON.
     *
     * @locus Anywhere
     *
     * @param {String} name - <p>A tag for your custom type; must be unique among custom data types defined in your project, and must match the result of your type's <code>typeName</code> method.</p>
     * @param {function} factory - <p>A function that deserializes a JSON-compatible value into an instance of your type.  This should match the serialization performed by your type's <code>toJSONValue</code> method.</p>
     */
    function addType(name:string, factory:Function):any;


    /**
     * Return a deep copy of `val`.
     *
     * @locus Anywhere
     *
     * @param {EJSON} val - <p>A value to copy.</p>
     */
    function clone(val:any):any;


    /**
     * Return true if `a` and `b` are equal to each other.  Return false otherwise.  Uses the `equals` method on `a` if present, otherwise performs a deep comparison.
     *
     * @locus Anywhere
     *
     * @param {EJSON} a
     * @param {EJSON} b
     * @param {Options} [options]
     */
    function equals(a:any,
           b:any,
           options?:{
               keyOrderSensitive?:boolean
           }):any;


    /**
     * Deserialize an EJSON value from its plain JSON representation.
     *
     * @locus Anywhere
     *
     * @param {JSONCompatible} val - <p>A value to deserialize into EJSON.</p>
     */
    function fromJSONValue(val:any):any;


    /**
     * Returns true if `x` is a buffer of binary data, as returned from [`EJSON.newBinary`](#ejson_new_binary).
     *
     * @locus Anywhere
     *
     * @param {Object} x - <p>The variable to check.</p>
     */
    function isBinary(x:any):any;


    /**
     * Allocate a new buffer of binary data that EJSON can serialize.
     *
     * @locus Anywhere
     */
    var newBinary:any;


    /**
     * Parse a string into an EJSON value. Throws an error if the string is not valid EJSON.
     *
     * @locus Anywhere
     *
     * @param {String} str - <p>A string to parse into an EJSON value.</p>
     */
    function parse(str:string):any;


    /**
     * Serialize a value to a string.
     *
     * For EJSON values, the serialization fully represents the value. For non-EJSON values, serializes the same way as `JSON.stringify`.
     *
     * @locus Anywhere
     *
     * @param {EJSON} val - <p>A value to stringify.</p>
     * @param {Options} [options]
     */
    function stringify(val:any,
              options?:{
                  indent?:any;
                  canonical?:boolean
              }):any;


    /**
     * Serialize an EJSON-compatible value into its plain JSON representation.
     *
     * @locus Anywhere
     *
     * @param {EJSON} val - <p>A value to serialize to plain JSON.</p>
     */
    function toJSONValue(val:any):any;

    interface CustomType {

        /**
         * Return a value `r` such that `this.equals(r)` is true, and modifications to `r` do not affect `this` and vice versa.
         *
         * @locus Anywhere
         */
        clone():any;


        /**
         * Return `true` if `other` has a value equal to `this`; `false` otherwise.
         *
         * @locus Anywhere
         *
         * @param {Object} other - <p>Another object to compare this to.</p>
         */
        equals(other:any):any;


        /**
         * Serialize this instance into a JSON-compatible value.
         *
         * @locus Anywhere
         */
        toJSONValue():any;


        /**
         * Return the tag used to identify this type.  This must match the tag used to register this type with [`EJSON.addType`](#ejson_add_type).
         *
         * @locus Anywhere
         */
        typeName():any;

    }


}



interface IEmail {

    /**
     * Send an email. Throws an `Error` on failure to contact mail server
     * or if mail server returns an error. All fields should match
     * [RFC5322](http://tools.ietf.org/html/rfc5322) specification.
     *
     * If the `MAIL_URL` environment variable is set, actually sends the email.
     * Otherwise, prints the contents of the email to standard out.
     *
     * Note that this package is based on mailcomposer version `0.1.15`, so make
     * sure to refer to the documentation for that version if using the
     * `attachments` or `mailComposer` options.
     * [Click here to read the mailcomposer 0.1.15 docs](https://github.com/andris9/mailcomposer/blob/7c0422b2de2dc61a60ba27cfa3353472f662aeb5/README.md).
     *
     * @locus Server
     *
     * @param {Options} options
     */
    send(options:{
             from?:string;
             to?:any;
             cc?:any;
             bcc?:any;
             replyTo?:any;
             subject?:string;
             text?:string;
             html?:string;
             headers?:any;
             attachments?:any;
             mailComposer?:any
         }):any;

}

declare var Email:IEmail;


interface IHTTP {

    /**
     * Perform an outbound HTTP request.
     *
     * @locus Anywhere
     *
     * @param {String} method - <p>The <a href="http://en.wikipedia.org/wiki/HTTP_method">HTTP method</a> to use, such as &quot;<code>GET</code>&quot;, &quot;<code>POST</code>&quot;, or &quot;<code>HEAD</code>&quot;.</p>
     * @param {String} url - <p>The URL to retrieve.</p>
     * @param {Options} [options]
     * @param {function} [asyncCallback] - <p>Optional callback.  If passed, the method runs asynchronously, instead of synchronously, and calls asyncCallback.  On the client, this callback is required.</p>
     */
    call(method:string,
         url:string,
         options?:{
             content?:string;
             data?:any;
             query?:string;
             params?:any;
             auth?:string;
             headers?:any;
             timeout?:Number;
             followRedirects?:boolean;
             npmRequestOptions?:any;
             beforeSend?:Function
         },
         asyncCallback?:Function):any;


    /**
     * Send an HTTP `DELETE` request. Equivalent to calling [`HTTP.call`](#http_call) with "DELETE" as the first argument. (Named `del` to avoid conflict with the Javascript keyword `delete`)
     *
     * @locus Anywhere
     *
     * @param {String} url - <p>The URL to which the request should be sent.</p>
     * @param {Object} [callOptions] - <p>Options passed on to <a href="#http_call"><code>HTTP.call</code></a>.</p>
     * @param {function} [asyncCallback] - <p>Callback that is called when the request is completed. Required on the client.</p>
     */
    del(url:string, callOptions?:any, asyncCallback?:Function):any;


    /**
     * Send an HTTP `GET` request. Equivalent to calling [`HTTP.call`](#http_call) with "GET" as the first argument.
     *
     * @locus Anywhere
     *
     * @param {String} url - <p>The URL to which the request should be sent.</p>
     * @param {Object} [callOptions] - <p>Options passed on to <a href="#http_call"><code>HTTP.call</code></a>.</p>
     * @param {function} [asyncCallback] - <p>Callback that is called when the request is completed. Required on the client.</p>
     */
    get(url:string, callOptions?:any, asyncCallback?:Function):any;


    /**
     * Send an HTTP `PATCH` request. Equivalent to calling [`HTTP.call`](#http_call) with "PATCH" as the first argument.
     *
     * @locus Anywhere
     *
     * @param {String} url - <p>The URL to which the request should be sent.</p>
     * @param {Object} [callOptions] - <p>Options passed on to <a href="#http_call"><code>HTTP.call</code></a>.</p>
     * @param {function} [asyncCallback] - <p>Callback that is called when the request is completed. Required on the client.</p>
     */
    patch(url:string, callOptions?:any, asyncCallback?:Function):any;


    /**
     * Send an HTTP `POST` request. Equivalent to calling [`HTTP.call`](#http_call) with "POST" as the first argument.
     *
     * @locus Anywhere
     *
     * @param {String} url - <p>The URL to which the request should be sent.</p>
     * @param {Object} [callOptions] - <p>Options passed on to <a href="#http_call"><code>HTTP.call</code></a>.</p>
     * @param {function} [asyncCallback] - <p>Callback that is called when the request is completed. Required on the client.</p>
     */
    post(url:string, callOptions?:any, asyncCallback?:Function):any;


    /**
     * Send an HTTP `PUT` request. Equivalent to calling [`HTTP.call`](#http_call) with "PUT" as the first argument.
     *
     * @locus Anywhere
     *
     * @param {String} url - <p>The URL to which the request should be sent.</p>
     * @param {Object} [callOptions] - <p>Options passed on to <a href="#http_call"><code>HTTP.call</code></a>.</p>
     * @param {function} [asyncCallback] - <p>Callback that is called when the request is completed. Required on the client.</p>
     */
    put(url:string, callOptions?:any, asyncCallback?:Function):any;

}

declare var HTTP:IHTTP;


interface IInputFile {

    /**
     * Call this method to raise a compilation or linting error for the
     * file.
     *
     *
     * @param {Options} options
     */
    error(options:{
              message?:string;
              sourcePath?:string;
              line?:any;
              func?:string
          }):any;


    /**
     * Returns the architecture that is targeted while processing this
     * file.
     *
     */
    getArch():any;


    /**
     * Returns the filename of the file.
     *
     */
    getBasename():any;


    /**
     * Returns the full contents of the file as a buffer.
     *
     */
    getContentsAsBuffer():any;


    /**
     * Returns the full contents of the file as a string.
     *
     */
    getContentsAsString():any;


    /**
     * Returns a list of symbols declared as exports in this target. The
     * result of `api.export('symbol')` calls in target's control file such as
     * package.js.
     *
     */
    getDeclaredExports():any;


    /**
     * Returns the directory path relative to the package or app root.
     * The returned path always uses forward slashes.
     *
     */
    getDirname():any;


    /**
     * Returns a relative path that can be used to form error messages or
     * other display properties. Can be used as an input to a source map.
     *
     */
    getDisplayPath():any;


    /**
     * Returns an object of file options such as those passed as the
     *          third argument to api.addFiles.
     *
     */
    getFileOptions():any;


    /**
     * Returns the name of the package or `null` if the file is not in a
     * package.
     *
     */
    getPackageName():any;


    /**
     * Returns the relative path of file to the package or app root
     * directory. The returned path always uses forward slashes.
     *
     */
    getPathInPackage():any;


    /**
     * Returns a hash string for the file that can be used to implement
     * caching.
     *
     */
    getSourceHash():any;

}

declare var InputFile:IInputFile;


interface IMatch {

    /**
     * Returns true if the value matches the pattern.
     *
     * @locus Anywhere
     *
     * @param {Any} value - <p>The value to check</p>
     * @param {MatchPattern} pattern - <p>The pattern to match <code>value</code> against</p>
     */
    test(value:any, pattern:any):any;

}

declare var Match:IMatch;


declare module Meteor {

    /**
     * @Deprecated
     *
     */
    type Collection = Mongo.Collection;
    


    /**
     * Generate an absolute URL pointing to the application. The server reads from the `ROOT_URL` environment variable to determine where it is running. This is taken care of automatically for apps deployed to Galaxy, but must be provided when using `meteor build`.
     *
     * @locus Anywhere
     *
     * @param {String} [path] - <p>A path to append to the root URL. Do not include a leading &quot;<code>/</code>&quot;.</p>
     * @param {Options} [options]
     */
    function absoluteUrl(path?:string,
                options?:{
                    secure?:boolean;
                    replaceLocalhost?:boolean;
                    rootUrl?:string
                }):any;


    /**
     * Invoke a method passing an array of arguments.
     *
     * @locus Anywhere
     *
     * @param {String} name - <p>Name of method to invoke</p>
     * @param {Array.<EJSONable>} args - <p>Method arguments</p>
     * @param {Options} [options]
     * @param {any} value <p>options.noRetry (Client only) if true, don't send this method again on reload, simply call the callback an error with the error code 'invocation-failed'.</p>
     * @param {function} [asyncCallback] - <p>Optional callback; same semantics as in <a href="#meteor_call"><code>Meteor.call</code></a>.</p>
     */
    function apply(name:string,
          args:any,
          options?:{
              wait?:boolean;
              onResultReceived?:Function
          },
          value?:boolean,
          asyncCallback?:Function):any;


    /**
     * Invokes a method passing any number of arguments.
     *
     * @locus Anywhere
     *
     * @param {String} name - <p>Name of method to invoke</p>
     * @param {EJSONable} [arg1, arg2...] - <p>Optional method arguments</p>
     * @param {function} [asyncCallback] - <p>Optional callback, which is called asynchronously with the error or result after the method is complete. If not provided, the method runs synchronously if possible (see below).</p>
     */
    function call(name:string, ...args:any[]):any;


    /**
     * Cancel a repeating function call scheduled by `Meteor.setInterval`.
     *
     * @locus Anywhere
     *
     * @param {Number} id - <p>The handle returned by <code>Meteor.setInterval</code></p>
     */
    function clearInterval(id:Number):any;


    /**
     * Cancel a function call scheduled by `Meteor.setTimeout`.
     *
     * @locus Anywhere
     *
     * @param {Number} id - <p>The handle returned by <code>Meteor.setTimeout</code></p>
     */
    function clearTimeout(id:Number):any;


    /**
     * Disconnect the client from the server.
     *
     * @locus Client
     */
    function disconnect():any;


    /**
     * Boolean variable.  True if running in client environment.
     *
     * @locus Anywhere
     */
    var isClient:boolean;


    /**
     * Boolean variable.  True if running in a Cordova mobile environment.
     *
     * @locus Anywhere
     */
    var isCordova:boolean;


    /**
     * Boolean variable.  True if running in development environment.
     *
     * @locus Anywhere
     */
    var isDevelopment:boolean;


    /**
     * Boolean variable.  True if running in production environment.
     *
     * @locus Anywhere
     */
    var isProduction:boolean;


    /**
     * Boolean variable.  True if running in server environment.
     *
     * @locus Anywhere
     */
    var isServer:boolean;


    /**
     * True if a login method (such as `Meteor.loginWithPassword`, `Meteor.loginWithFacebook`, or `Accounts.createUser`) is currently in progress. A reactive data source.
     *
     * @locus Client
     */
    function loggingIn():any;


    /**
     * Log the user in with a password.
     *
     * @locus Client
     *
     * @param {Object or String} user - <p>Either a string interpreted as a username or an email; or an object with a
     *   single key: <code>email</code>, <code>username</code> or <code>id</code>. Username or email match in a case
     *   insensitive manner.</p>
     * @param {String} password - <p>The user's password.</p>
     * @param {function} [callback] - <p>Optional callback.
     *   Called with no arguments on success, or with a single <code>Error</code> argument
     *   on failure.</p>
     */
    function loginWithPassword(user:any, password:string, callback?:Function):any;


    /**
     * Log the user out.
     *
     * @locus Client
     *
     * @param {function} [callback] - <p>Optional callback. Called with no arguments on success, or with a single <code>Error</code> argument on failure.</p>
     */
    function logout(callback?:Function):any;


    /**
     * Log out other clients logged in as the current user, but does not log out the client that calls this function.
     *
     * @locus Client
     *
     * @param {function} [callback] - <p>Optional callback. Called with no arguments on success, or with a single <code>Error</code> argument on failure.</p>
     */
    function logoutOtherClients(callback?:Function):any;


    /**
     * Defines functions that can be invoked over the network by clients.
     *
     * @locus Anywhere
     *
     * @param {Object} methods - <p>Dictionary whose keys are method names and values are functions.</p>
     */
    function methods(methods:any):any;


    /**
     * Register a callback to be called when a new DDP connection is made to the server.
     *
     * @locus Server
     *
     * @param {function} callback - <p>The function to call when a new DDP connection is established.</p>
     */
    function onConnection(callback:Function):any;


    /**
     * Publish a record set.
     *
     * @locus Server
     *
     * @param {String} name - <p>Name of the record set.  If <code>null</code>, the set has no name, and the record set is automatically sent to all connected clients.</p>
     * @param {function} func - <p>Function called on the server each time a client subscribes.  Inside the function, <code>this</code> is the publish handler object, described below.  If the client passed arguments to <code>subscribe</code>, the function is called with the same arguments.</p>
     */
    function publish(name:string, func:Function):any;


    /**
     * Force an immediate reconnection attempt if the client is not connected to the server.
     *
     *   This method does nothing if the client is already connected.
     *
     * @locus Client
     */
    function reconnect():any;


    /**
     * `Meteor.release` is a string containing the name of the [release](#meteorupdate) with which the project was built (for example, `"1.2.3"`). It is `undefined` if the project was built using a git checkout of Meteor.
     *
     * @locus Anywhere
     */
    var release:string;


    /**
     * Call a function repeatedly, with a time delay between calls.
     *
     * @locus Anywhere
     *
     * @param {function} func - <p>The function to run</p>
     * @param {Number} delay - <p>Number of milliseconds to wait between each function call.</p>
     */
    function setInterval(func:Function, delay:Number):any;


    /**
     * Call a function in the future after waiting for a specified delay.
     *
     * @locus Anywhere
     *
     * @param {function} func - <p>The function to run</p>
     * @param {Number} delay - <p>Number of milliseconds to wait before calling function</p>
     */
    function setTimeout(func:Function, delay:Number):any;


    /**
     * `Meteor.settings` contains deployment-specific configuration options. You can initialize settings by passing the `--settings` option (which takes the name of a file containing JSON data) to `meteor run` or `meteor deploy`. When running your server directly (e.g. from a bundle), you instead specify settings by putting the JSON directly into the `METEOR_SETTINGS` environment variable. If the settings object contains a key named `public`, then `Meteor.settings.public` will be available on the client as well as the server.  All other properties of `Meteor.settings` are only defined on the server.  You can rely on `Meteor.settings` and `Meteor.settings.public` being defined objects (not undefined) on both client and server even if there are no settings specified.  Changes to `Meteor.settings.public` at runtime will be picked up by new client connections.
     *
     * @locus Anywhere
     */
    var settings:any;


    /**
     * Run code when a client or a server starts.
     *
     * @locus Anywhere
     *
     * @param {function} func - <p>A function to run on startup.</p>
     */
    function startup(func:Function):any;


    /**
     * Get the current connection status. A reactive data source.
     *
     * @locus Client
     */
    function status():any;


    /**
     * Subscribe to a record set.  Returns a handle that provides
     * `stop()` and `ready()` methods.
     *
     * @locus Client
     *
     * @param {String} name - <p>Name of the subscription.  Matches the name of the
     * server's <code>publish()</code> call.</p>
     * @param {EJSONable} [arg1, arg2...] - <p>Optional arguments passed to publisher
     * function on server.</p>
     * @param {function or Object} [callbacks] - <p>Optional. May include <code>onStop</code>
     * and <code>onReady</code> callbacks. If there is an error, it is passed as an
     * argument to <code>onStop</code>. If a function is passed instead of an object, it
     * is interpreted as an <code>onReady</code> callback.</p>
     */
    function subscribe(name:string, ...args:any[]):any;


    /**
     * Get the current user record, or `null` if no user is logged in. A reactive data source.
     *
     * @locus Anywhere but publish functions
     */
    function user():any;


    /**
     * Get the current user id, or `null` if no user is logged in. A reactive data source.
     *
     * @locus Anywhere but publish functions
     */
    function userId():any;


    /**
     * A [Mongo.Collection](#collections) containing user documents.
     *
     * @locus Anywhere
     */
    var users:any;


    /**
     * Wrap a function that takes a callback function as its final parameter. The signature of the callback of the wrapped function should be `function(error, result){}`. On the server, the wrapped function can be used either synchronously (without passing a callback) or asynchronously (when a callback is passed). On the client, a callback is always required; errors will be logged if there is no callback. If a callback is provided, the environment captured when the original function was called will be restored in the callback.
     *
     * @locus Anywhere
     *
     * @param {function} func - <p>A function that takes a callback as its final parameter</p>
     * @param {Object} [context] - <p>Optional <code>this</code> object against which the original function will be invoked</p>
     */
    function wrapAsync(func:Function, context?:any):any;

    /**
     * This class represents a symbolic error thrown by a method.
     *
     * @locus Anywhere
     *
     * @param {String} error - <p>A string code uniquely identifying this kind of error.
     * This string should be used by callers of the method to determine the
     * appropriate action to take, instead of attempting to parse the reason
     * or details fields. For example:</p>
     * <pre class="prettyprint source"><code>// on the server, pick a code unique to this error
     * // the reason field should be a useful debug message
     * throw new Meteor.Error(&quot;logged-out&quot;,
     *   &quot;The user must be logged in to post a comment.&quot;);
     *
     * // on the client
     * Meteor.call(&quot;methodName&quot;, function (error) {
     *   // identify the error
     *   if (error && error.error === &quot;logged-out&quot;) {
     *     // show a nice error message
     *     Session.set(&quot;errorMessage&quot;, &quot;Please log in to post a comment.&quot;);
     *   }
     * });</code></pre><p>For legacy reasons, some built-in Meteor functions such as <code>check</code> throw
     * errors with a number in this field.</p>
     * @param {String} [reason] - <p>Optional.  A short human-readable summary of the
     * error, like 'Not Found'.</p>
     * @param {String} [details] - <p>Optional.  Additional information about the error,
     * like a textual stack trace.</p>
     */
    class Error {
        constructor(error:string, reason?:string, details?:string);

    }


}



declare module Mongo {




    /**
     * Create a Mongo-style `ObjectID`.  If you don't specify a `hexString`, the `ObjectID` will generated randomly (not using MongoDB's ID construction rules).
     *
     * @locus Anywhere
     *
     * @param {String} hexString - <p>Optional.  The 24-character hexadecimal contents of the ObjectID to create</p>
     */
    function ObjectID(hexString:string):ObjectID;

    class Collection {


        /**
         * Constructor for a Collection
         *
         * @locus Anywhere
         *
         * @param {String} name - <p>The name of the collection.  If null, creates an unmanaged (unsynchronized) local collection.</p>
         * @param {Options} [options]
         */
        constructor(name:string,
                            options?:{
                                connection?:any;
                                idGeneration?:string;
                                transform?:Function
                            });
        /**
         * Allow users to write directly to this collection from client code, subject to limitations you define.
         *
         * @locus Server
         *
         * @param {Options} options
         */
        allow(options:{
                  insert?:Function;
                  update?:Function;
                  remove?:Function;
                  fetch?:any;
                  transform?:Function
              }):any;


        /**
         * Override `allow` rules.
         *
         * @locus Server
         *
         * @param {Options} options
         */
        deny(options:{
                 insert?:Function;
                 update?:Function;
                 remove?:Function;
                 fetch?:any;
                 transform?:Function
             }):any;


        /**
         * Find the documents in a collection that match the selector.
         *
         * @locus Anywhere
         *
         * @param {MongoSelector} [selector] - <p>A query describing the documents to find</p>
         * @param {Options} [options]
         */
        find(selector?:any,
             options?:{
                 sort?:any;
                 skip?:Number;
                 limit?:Number;
                 fields?:any;
                 reactive?:boolean;
                 transform?:Function;
                 disableOplog?:boolean;
                 pollingIntervalMs?:Number;
                 pollingThrottleMs?:Number
             }):Cursor;


        /**
         * Finds the first document that matches the selector, as ordered by sort and skip options.
         *
         * @locus Anywhere
         *
         * @param {MongoSelector} [selector] - <p>A query describing the documents to find</p>
         * @param {Options} [options]
         */
        findOne(selector?:any,
                options?:{
                    sort?:any;
                    skip?:Number;
                    fields?:any;
                    reactive?:boolean;
                    transform?:Function
                }):any;


        /**
         * Insert a document in the collection.  Returns its unique _id.
         *
         * @locus Anywhere
         *
         * @param {Object} doc - <p>The document to insert. May not yet have an _id attribute, in which case Meteor will generate one for you.</p>
         * @param {function} [callback] - <p>Optional.  If present, called with an error object as the first argument and, if no error, the _id as the second.</p>
         */
        insert(doc:any, callback?:Function):any;


        /**
         * Returns the [`Collection`](http://mongodb.github.io/node-mongodb-native/1.4/api-generated/collection.html) object corresponding to this collection from the [npm `mongodb` driver module](https://www.npmjs.com/package/mongodb) which is wrapped by `Mongo.Collection`.
         *
         * @locus Server
         */
        rawCollection():any;


        /**
         * Returns the [`Db`](http://mongodb.github.io/node-mongodb-native/1.4/api-generated/db.html) object corresponding to this collection's database connection from the [npm `mongodb` driver module](https://www.npmjs.com/package/mongodb) which is wrapped by `Mongo.Collection`.
         *
         * @locus Server
         */
        rawDatabase():any;


        /**
         * Remove documents from the collection
         *
         * @locus Anywhere
         *
         * @param {MongoSelector} selector - <p>Specifies which documents to remove</p>
         * @param {function} [callback] - <p>Optional.  If present, called with an error object as its argument.</p>
         */
        remove(selector:any, callback?:Function):any;


        /**
         * Modify one or more documents in the collection. Returns the number of affected documents.
         *
         * @locus Anywhere
         *
         * @param {MongoSelector} selector - <p>Specifies which documents to modify</p>
         * @param {MongoModifier} modifier - <p>Specifies how to modify the documents</p>
         * @param {Options} [options]
         * @param {function} [callback] - <p>Optional.  If present, called with an error object as the first argument and, if no error, the number of affected documents as the second.</p>
         */
        update(selector:any,
               modifier:any,
               options?:{
                   multi?:boolean;
                   upsert?:boolean
               },
               callback?:Function):any;


        /**
         * Modify one or more documents in the collection, or insert one if no matching documents were found. Returns an object with keys `numberAffected` (the number of documents modified)  and `insertedId` (the unique _id of the document that was inserted, if any).
         *
         * @locus Anywhere
         *
         * @param {MongoSelector} selector - <p>Specifies which documents to modify</p>
         * @param {MongoModifier} modifier - <p>Specifies how to modify the documents</p>
         * @param {Options} [options]
         * @param {function} [callback] - <p>Optional.  If present, called with an error object as the first argument and, if no error, the number of affected documents as the second.</p>
         */
        upsert(selector:any,
               modifier:any,
               options?:{
                   multi?:boolean
               },
               callback?:Function):any;

    }


    interface Cursor {

        /**
         * Returns the number of documents that match a query.
         *
         * @locus Anywhere
         */
        count():any;


        /**
         * Return all matching documents as an Array.
         *
         * @locus Anywhere
         */
        fetch():any;


        /**
         * Call `callback` once for each matching document, sequentially and synchronously.
         *
         * @locus Anywhere
         *
         * @param {IterationCallback} callback - <p>Function to call. It will be called with three arguments: the document, a 0-based index, and <em>cursor</em> itself.</p>
         * @param {Any} [thisArg] - <p>An object which will be the value of <code>this</code> inside <code>callback</code>.</p>
         */
        forEach(callback:any, thisArg?:any):any;


        /**
         * Map callback over all matching documents.  Returns an Array.
         *
         * @locus Anywhere
         *
         * @param {IterationCallback} callback - <p>Function to call. It will be called with three arguments: the document, a 0-based index, and <em>cursor</em> itself.</p>
         * @param {Any} [thisArg] - <p>An object which will be the value of <code>this</code> inside <code>callback</code>.</p>
         */
        map(callback:any, thisArg?:any):any;


        /**
         * Watch a query.  Receive callbacks as the result set changes.
         *
         * @locus Anywhere
         *
         * @param {Object} callbacks - <p>Functions to call to deliver the result set as it changes</p>
         */
        observe(callbacks:any):any;


        /**
         * Watch a query.  Receive callbacks as the result set changes.  Only the differences between the old and new documents are passed to the callbacks.
         *
         * @locus Anywhere
         *
         * @param {Object} callbacks - <p>Functions to call to deliver the result set as it changes</p>
         */
        observeChanges(callbacks:any):any;

    }


    interface ObjectID {

    }


}



interface INpm {

    /**
     * Specify which [NPM](https://www.npmjs.org/) packages
     * your Meteor package depends on.
     *
     * @locus package.js
     *
     * @param {Object} dependencies - <p>An object where the keys are package
     * names and the values are one of:</p>
     * <ol>
     * <li>Version numbers in string form</li>
     * <li>Http(s) URLs to a git commit by SHA.   </li>
     * <li>Git URLs in the format described <a href="https://docs.npmjs.com/files/package.json#git-urls-as-dependencies">here</a></li>
     * </ol>
     * <p>Https URL example:</p>
     * <pre class="prettyprint source lang-js"><code>Npm.depends({
     *   moment: &quot;2.8.3&quot;,
     *   async: &quot;https://github.com/caolan/async/archive/71fa2638973dafd8761fa5457c472a312cc820fe.tar.gz&quot;
     * });</code></pre><p>Git URL example:</p>
     * <pre class="prettyprint source lang-js"><code>Npm.depends({
     *   moment: &quot;2.8.3&quot;,
     *   async: &quot;git+https://github.com/caolan/async#master&quot;
     * });</code></pre>
     */
    depends(dependencies:any):any;


    /**
     * Require a package that was specified using
     * `Npm.depends()`.
     *
     * @locus Server
     *
     * @param {String} name - <p>The name of the package to require.</p>
     */
    require(name:string):any;

}

declare var Npm:INpm;


interface IPackage {

    /**
     * Provide basic package information.
     *
     * @locus package.js
     *
     * @param {Options} options
     */
    describe(options:{
                 summary?:string;
                 version?:string;
                 name?:string;
                 git?:string;
                 documentation?:string;
                 debugOnly?:boolean;
                 prodOnly?:boolean;
                 testOnly?:boolean
             }):any;


    /**
     * Define dependencies and expose package methods for unit tests.
     *
     * @locus package.js
     *
     * @param {function} func - <p>A function that takes in the package control 'api' object, which keeps track of dependencies and exports.</p>
     */
    onTest(func:Function):any;


    /**
     * Define package dependencies and expose package methods.
     *
     * @locus package.js
     *
     * @param {function} func - <p>A function that takes in the package control <code>api</code> object, which keeps track of dependencies and exports.</p>
     */
    onUse(func:Function):any;


    /**
     * Define a build plugin. A build plugin extends the build
     * process for apps and packages that use this package. For example,
     * the `coffeescript` package uses a build plugin to compile CoffeeScript
     * source files into JavaScript.
     *
     * @locus package.js
     *
     * @param {Options} [options]
     */
    registerBuildPlugin(options?:{
                            name?:string;
                            use?:any;
                            sources?:any;
                            npmDependencies?:any
                        }):any;

}

declare var Package:IPackage;


interface PackageAPIMember {

    /**
     * Specify asset files for your package. They can be accessed via
     * the [Assets API](#assets) from the server, or at the URL
     * `/packages/username_package-name/file-name` from the client, depending on the
     * architecture passed.
     *
     * @locus package.js
     *
     * @param {String or Array.<String>} filenames - <p>Paths to the asset files.</p>
     * @param {String or Array.<String>} architecture - <p>Specify where this asset should be
     * available (e.g., 'server', 'client', 'web.browser', 'web.cordova'). You can
     * specify multiple architectures by passing in an array, for example
     * <code>['web.cordova', 'os.linux']</code>.</p>
     */
    addAssets(filenames:any, architecture:any):any;


    /**
     * Specify source code files for your package.
     *
     * @locus package.js
     *
     * @param {String or Array.<String>} filenames - <p>Paths to the source files.</p>
     * @param {String or Array.<String>} [architecture] - <p>If you only want to use the file
     * on the server (or the client), you can pass this argument
     * (e.g., 'server', 'client', 'web.browser', 'web.cordova') to specify
     * what architecture the file is used with. You can specify multiple
     * architectures by passing in an array, for example
     * <code>['web.cordova', 'os.linux']</code>. By default, the file will be loaded on both
     * server and client.</p>
     * @param {Options} [options]
     */
    addFiles(filenames:any,
             architecture?:any,
             options?:{
                 bare?:boolean
             }):any;


    /**
     * Export package-level variables in your package. The specified
     * variables (declared without `var` in the source code) will be available
     * to packages that use your package. If your package sets the `debugOnly`,
     * `prodOnly` or `testOnly` options to `true` when it calls
     * `Package.describe()`, then packages that use your package will need to use
     * `Package["package-name"].ExportedVariableName` to access the value of an
     * exported variable.
     *
     * @locus package.js
     *
     * @param {String or Array.<String>} exportedObjects - <p>Name of the object to export, or
     * an array of object names.</p>
     * @param {String or Array.<String>} [architecture] - <p>If you only want to export the
     * object on the server (or the client), you can pass in the second argument
     * (e.g., 'server', 'client', 'web.browser', 'web.cordova') to specify what
     * architecture the export is used with. You can specify multiple
     * architectures by passing in an array, for example <code>['web.cordova',
     * 'os.linux']</code>.</p>
     * @param {Object} [exportOptions]
     * @param {Boolean} [exportOptions.testOnly] - <p>If true, this symbol will only be
     * exported when running tests for this package.</p>
     */
    export(exportedObjects:any, architecture?:any, exportOptions?:any, testOnly?:boolean):any;


    /**
     * Give users of this package access to another package (by passing
     * in the string `packagename`) or a collection of packages (by passing in
     * an array of strings [`packagename1`, `packagename2`]
     *
     * @locus package.js
     *
     * @param {String or Array.<String>} packageNames - <p>Name of a package, or array of
     * package names, with an optional @version component for each.</p>
     * @param {String or Array.<String>} [architecture] - <p>If you only use the package on
     * the server (or the client), you can pass in the second argument (e.g.,
     * <code>'server'</code>, <code>'client'</code>, <code>'web.browser'</code>, <code>'web.cordova'</code>) to specify what
     * architecture the package is used with. You can specify multiple
     * architectures by passing in an array, for example <code>['web.cordova',
     * 'os.linux']</code>.</p>
     */
    imply(packageNames:any, architecture?:any):any;


    /**
     * Depend on package `packagename`.
     *
     * @locus package.js
     *
     * @param {String or Array.<String>} packageNames - <p>Packages being depended on.
     * Package names may be suffixed with an @version tag.</p>
     * <p>In general, you must specify a package's version (e.g.,
     * <code>'accounts@1.0.0'</code> to use version 1.0.0 or a higher
     * compatible version (ex: 1.0.1, 1.5.0, etc.)  of the
     * <code>accounts</code> package). If you are sourcing core
     * packages from a Meteor release with <code>versionsFrom</code>, you may leave
     * off version names for core packages. You may also specify constraints,
     * such as <code>my:forms@=1.0.0</code> (this package demands <code>my:forms</code> at <code>1.0.0</code> exactly),
     * or <code>my:forms@1.0.0 || =2.0.1</code> (<code>my:forms</code> at <code>1.x.y</code>, or exactly <code>2.0.1</code>).</p>
     * @param {String or Array.<String>} [architecture] - <p>If you only use the package on the
     * server (or the client), you can pass in the second argument (e.g.,
     * <code>'server'</code>, <code>'client'</code>, <code>'web.browser'</code>, <code>'web.cordova'</code>) to specify
     * what architecture the package is used with. You can specify multiple
     * architectures by passing in an array, for example <code>['web.cordova', 'os.linux']</code>.</p>
     * @param {Options} [options]
     */
    use(packageNames:any,
        architecture?:any,
        options?:{
            weak?:boolean;
            unordered?:boolean
        }):any;


    /**
     * Use versions of core packages from a release. Unless provided,
     * all packages will default to the versions released along with
     * `meteorRelease`. This will save you from having to figure out the exact
     * versions of the core packages you want to use. For example, if the newest
     * release of meteor is `METEOR@0.9.0` and it includes `jquery@1.0.0`, you
     * can write `api.versionsFrom('METEOR@0.9.0')` in your package, and when you
     * later write `api.use('jquery')`, it will be equivalent to
     * `api.use('jquery@1.0.0')`. You may specify an array of multiple releases,
     * in which case the default value for constraints will be the "or" of the
     * versions from each release: `api.versionsFrom(['METEOR@0.9.0',
     * 'METEOR@0.9.5'])` may cause `api.use('jquery')` to be interpreted as
     * `api.use('jquery@1.0.0 || 2.0.0')`.
     *
     * @locus package.js
     *
     * @param {String or Array.<String>} meteorRelease - <p>Specification of a release:
     * track@version. Just 'version' (e.g. <code>&quot;0.9.0&quot;</code>) is sufficient if using the
     * default release track <code>METEOR</code>. Can be an array of specifications.</p>
     */
    versionsFrom(meteorRelease:any):any;

}



interface IPlugin {

    /**
     * Inside a build plugin source file specified in
     * [Package.registerBuildPlugin](#Package-registerBuildPlugin),
     * add a compiler that will handle files with certain extensions or
     * filenames.
     *
     * @locus Build Plugin
     *
     * @param {Options} options
     * @param {function} factory - <p>A function that returns an instance
     * of a compiler class.</p>
     * <p>More detailed documentation for build plugins is available <a href="https://github.com/meteor/meteor/wiki/Build-Plugins-API">on the GitHub Wiki</a>.</p>
     */
    registerCompiler(options:{
                         extensions?:any;
                         filenames?:any
                     },
                     factory:Function):any;


    /**
     * Inside a build plugin source file specified in
     * [Package.registerBuildPlugin](#Package-registerBuildPlugin),
     * add a linter that will handle files with certain extensions or
     * filenames.
     *
     * @locus Build Plugin
     *
     * @param {Options} options
     * @param {function} factory - <p>A function that returns an instance
     * of a linter class.</p>
     * <p>More detailed documentation for build plugins is available <a href="https://github.com/meteor/meteor/wiki/Build-Plugins-API">on the GitHub Wiki</a>.</p>
     */
    registerLinter(options:{
                       extensions?:any
                   },
                   factory:Function):any;


    /**
     * Inside a build plugin source file specified in
     * [Package.registerBuildPlugin](#Package-registerBuildPlugin),
     * add a linter that will handle files with certain extensions or
     * filenames.
     *
     * @locus Build Plugin
     *
     * @param {Options} options
     * @param {function} factory - <p>A function that returns an instance
     * of a minifier class.</p>
     * <p>More detailed documentation for build plugins is available <a href="https://github.com/meteor/meteor/wiki/Build-Plugins-API">on the GitHub Wiki</a>.</p>
     */
    registerMinifier(options:{
                         extensions?:any;
                         filenames?:any
                     },
                     factory:Function):any;


    /**
     * Inside a build plugin source file specified in
     * [Package.registerBuildPlugin](#Package-registerBuildPlugin),
     * add a handler to compile files with a certain file extension.
     *
     * @locus Build Plugin
     *
     * @param {String} fileExtension - <p>The file extension that this plugin
     * should handle, without the first dot.
     * Examples: <code>&quot;coffee&quot;</code>, <code>&quot;coffee.md&quot;</code>.</p>
     * @param {function} handler - <p>A function that takes one argument,
     * a CompileStep object.</p>
     * <p>Documentation for CompileStep is available <a href="https://github.com/meteor/meteor/wiki/CompileStep-API-for-Build-Plugin-Source-Handlers">on the GitHub Wiki</a>.</p>
     */
    registerSourceHandler(fileExtension:string, handler:Function):any;

}

declare var Plugin:IPlugin;


interface ISession {

    /**
     * Test if a session variable is equal to a value. If inside a
     * [reactive computation](#reactivity), invalidate the computation the next
     * time the variable changes to or from the value.
     *
     * @locus Client
     *
     * @param {String} key - <p>The name of the session variable to test</p>
     * @param {String or Number or Boolean or null or undefined} value - <p>The value to
     * test against</p>
     */
    equals(key:string, value:any):any;


    /**
     * Get the value of a session variable. If inside a [reactive
     * computation](#reactivity), invalidate the computation the next time the
     * value of the variable is changed by [`Session.set`](#session_set). This
     * returns a clone of the session value, so if it's an object or an array,
     * mutating the returned value has no effect on the value stored in the
     * session.
     *
     * @locus Client
     *
     * @param {String} key - <p>The name of the session variable to return</p>
     */
    get(key:string):any;


    /**
     * Set a variable in the session. Notify any listeners that the value
     * has changed (eg: redraw templates, and rerun any
     * [`Tracker.autorun`](#tracker_autorun) computations, that called
     * [`Session.get`](#session_get) on this `key`.)
     *
     * @locus Client
     *
     * @param {String} key - <p>The key to set, eg, <code>selectedItem</code></p>
     * @param {EJSONable or undefined} value - <p>The new value for <code>key</code></p>
     */
    set(key:string, value:any):any;


    /**
     * Set a variable in the session if it hasn't been set before.
     * Otherwise works exactly the same as [`Session.set`](#session_set).
     *
     * @locus Client
     *
     * @param {String} key - <p>The key to set, eg, <code>selectedItem</code></p>
     * @param {EJSONable or undefined} value - <p>The new value for <code>key</code></p>
     */
    setDefault(key:string, value:any):any;

}

declare var Session:ISession;


interface SubscriptionMember {

    /**
     * Call inside the publish function.  Informs the subscriber that a document has been added to the record set.
     *
     * @locus Server
     *
     * @param {String} collection - <p>The name of the collection that contains the new document.</p>
     * @param {String} id - <p>The new document's ID.</p>
     * @param {Object} fields - <p>The fields in the new document.  If <code>_id</code> is present it is ignored.</p>
     */
    added(collection:string, id:string, fields:any):any;


    /**
     * Call inside the publish function.  Informs the subscriber that a document in the record set has been modified.
     *
     * @locus Server
     *
     * @param {String} collection - <p>The name of the collection that contains the changed document.</p>
     * @param {String} id - <p>The changed document's ID.</p>
     * @param {Object} fields - <p>The fields in the document that have changed, together with their new values.  If a field is not present in <code>fields</code> it was left unchanged; if it is present in <code>fields</code> and has a value of <code>undefined</code> it was removed from the document.  If <code>_id</code> is present it is ignored.</p>
     */
    changed(collection:string, id:string, fields:any):any;


    /**
     * Access inside the publish function. The incoming [connection](#meteor_onconnection) for this subscription.
     *
     * @locus Server
     */
    connection:any;


    /**
     * Call inside the publish function.  Stops this client's subscription, triggering a call on the client to the `onStop` callback passed to [`Meteor.subscribe`](#meteor_subscribe), if any. If `error` is not a [`Meteor.Error`](#meteor_error), it will be [sanitized](#meteor_error).
     *
     * @locus Server
     *
     * @param {Error} error - <p>The error to pass to the client.</p>
     */
    error(error:any):any;


    /**
     * Call inside the publish function.  Registers a callback function to run when the subscription is stopped.
     *
     * @locus Server
     *
     * @param {function} func - <p>The callback function</p>
     */
    onStop(func:Function):any;


    /**
     * Call inside the publish function.  Informs the subscriber that an initial, complete snapshot of the record set has been sent.  This will trigger a call on the client to the `onReady` callback passed to  [`Meteor.subscribe`](#meteor_subscribe), if any.
     *
     * @locus Server
     */
    ready():any;


    /**
     * Call inside the publish function.  Informs the subscriber that a document has been removed from the record set.
     *
     * @locus Server
     *
     * @param {String} collection - <p>The name of the collection that the document has been removed from.</p>
     * @param {String} id - <p>The ID of the document that has been removed.</p>
     */
    removed(collection:string, id:string):any;


    /**
     * Call inside the publish function.  Stops this client's subscription and invokes the client's `onStop` callback with no error.
     *
     * @locus Server
     */
    stop():any;


    /**
     * Access inside the publish function. The id of the logged-in user, or `null` if no user is logged in.
     *
     * @locus Server
     */
    userId:any;

}



interface TemplateMember {

    /**
     * Provide a callback when an instance of a template is created.
     *
     * @locus Client
     */
    created:any;


    /**
     * Provide a callback when an instance of a template is destroyed.
     *
     * @locus Client
     */
    destroyed:any;


    /**
     * Specify event handlers for this template.
     *
     * @locus Client
     *
     * @param {EventMap} eventMap - <p>Event handlers to associate with this template.</p>
     */
    events(eventMap:any):any;


    /**
     * Specify template helpers available to this template.
     *
     * @locus Client
     *
     * @param {Object} helpers - <p>Dictionary of helper functions by name.</p>
     */
    helpers(helpers:any):any;


    /**
     * Register a function to be called when an instance of this template is created.
     *
     * @locus Client
     */
    onCreated:any;


    /**
     * Register a function to be called when an instance of this template is removed from the DOM and destroyed.
     *
     * @locus Client
     */
    onDestroyed:any;


    /**
     * Register a function to be called when an instance of this template is inserted into the DOM.
     *
     * @locus Client
     */
    onRendered:any;


    /**
     * Provide a callback when an instance of a template is rendered.
     *
     * @locus Client
     */
    rendered:any;

}



declare module Tracker {

    /**
     * A Computation object represents code that is repeatedly rerun
     * in response to
     * reactive data changes. Computations don't have return values; they just
     * perform actions, such as rerendering a template on the screen. Computations
     * are created using Tracker.autorun. Use stop to prevent further rerunning of a
     * computation.
     *
     */
    function Computation():any;


    /**
     * A Dependency represents an atomic unit of reactive data that a
     * computation might depend on. Reactive data sources such as Session or
     * Minimongo internally create different Dependency objects for different
     * pieces of data, each of which may be depended on by multiple computations.
     * When the data changes, the computations are invalidated.
     *
     */
    function Dependency():Dependency;


    /**
     * True if there is a current computation, meaning that dependencies on reactive data sources will be tracked and potentially cause the current computation to be rerun.
     *
     * @locus Client
     */
    var active:boolean;


    /**
     * Schedules a function to be called during the next flush, or later in the current flush if one is in progress, after all invalidated computations have been rerun.  The function will be run once and not on subsequent flushes unless `afterFlush` is called again.
     *
     * @locus Client
     *
     * @param {function} callback - <p>A function to call at flush time.</p>
     */
    function afterFlush(callback:Function):any;


    /**
     * Run a function now and rerun it later whenever its dependencies
     * change. Returns a Computation object that can be used to stop or observe the
     * rerunning.
     *
     * @locus Client
     *
     * @param {Tracker.ComputationFunction} runFunc - <p>The function to run. It receives
     * one argument: the Computation object that will be returned.</p>
     * @param {Options} [options]
     */
    function autorun(runFunc:any,
            options?:{
                onError?:Function
            }):Computation;


    /**
     * The current computation, or `null` if there isn't one.  The current computation is the [`Tracker.Computation`](#tracker_computation) object created by the innermost active call to `Tracker.autorun`, and it's the computation that gains dependencies when reactive data sources are accessed.
     *
     * @locus Client
     */
    var currentComputation:any;


    /**
     * Process all reactive updates immediately and ensure that all invalidated computations are rerun.
     *
     * @locus Client
     */
    function flush():any;


    /**
     * Run a function without tracking dependencies.
     *
     * @locus Client
     *
     * @param {function} func - <p>A function to call immediately.</p>
     */
    function nonreactive(func:Function):any;


    /**
     * Registers a new [`onInvalidate`](#computation_oninvalidate) callback on the current computation (which must exist), to be called immediately when the current computation is invalidated or stopped.
     *
     * @locus Client
     *
     * @param {function} callback - <p>A callback function that will be invoked as <code>func(c)</code>, where <code>c</code> is the computation on which the callback is registered.</p>
     */
    function onInvalidate(callback:Function):any;

    interface Computation {

        /**
         * True during the initial run of the computation at the time `Tracker.autorun` is called, and false on subsequent reruns and at other times.
         *
         * @locus Client
         */
        firstRun:boolean;


        /**
         * Invalidates this computation so that it will be rerun.
         *
         * @locus Client
         */
        invalidate():any;


        /**
         * True if this computation has been invalidated (and not yet rerun), or if it has been stopped.
         *
         * @locus Client
         */
        invalidated:boolean;


        /**
         * Registers `callback` to run when this computation is next invalidated, or runs it immediately if the computation is already invalidated.  The callback is run exactly once and not upon future invalidations unless `onInvalidate` is called again after the computation becomes valid again.
         *
         * @locus Client
         *
         * @param {function} callback - <p>Function to be called on invalidation. Receives one argument, the computation that was invalidated.</p>
         */
        onInvalidate(callback:Function):any;


        /**
         * Registers `callback` to run when this computation is stopped, or runs it immediately if the computation is already stopped.  The callback is run after any `onInvalidate` callbacks.
         *
         * @locus Client
         *
         * @param {function} callback - <p>Function to be called on stop. Receives one argument, the computation that was stopped.</p>
         */
        onStop(callback:Function):any;


        /**
         * Prevents this computation from rerunning.
         *
         * @locus Client
         */
        stop():any;


        /**
         * True if this computation has been stopped.
         *
         * @locus Client
         */
        stopped:any;

    }


    interface Dependency {

        /**
         * Invalidate all dependent computations immediately and remove them as dependents.
         *
         * @locus Client
         */
        changed():any;


        /**
         * Declares that the current computation (or `fromComputation` if given) depends on `dependency`.  The computation will be invalidated the next time `dependency` changes.
         *
         * If there is no current computation and `depend()` is called with no arguments, it does nothing and returns false.
         *
         * Returns true if the computation is a new dependent of `dependency` rather than an existing one.
         *
         * @locus Client
         *
         * @param {Tracker.Computation} [fromComputation] - <p>An optional computation declared to depend on <code>dependency</code> instead of the current computation.</p>
         */
        depend(fromComputation?:any):any;


        /**
         * True if this Dependency has one or more dependent Computations, which would be invalidated if this Dependency were to change.
         *
         * @locus Client
         */
        hasDependents():any;

    }
}

declare module 'meteor/meteor' {
    export var Meteor;
}

declare module 'meteor/accounts-base' {
    export var Accounts;
}

declare module 'meteor/ddp-rate-limiter' {
    export var DDPRateLimiter;
}

declare module 'meteor/check' {
    export var check;
    export var Match;
}

declare module 'meteor/ddp-client' {
    export var DDP;
}


declare module 'meteor/mongo' {
    export var Mongo;
}

declare module 'meteor/session' {
    export var Session;
}

declare module 'meteor/templating' {
    export var Template;
}

declare module 'meteor/blaze' {
    export var Blaze;
}

declare module 'meteor/tracker' {
    export var Tracker;
}

declare module 'meteor/reactive-var' {
    export var ReactiveVar;
}

declare module 'meteor/ejson' {
    export var EJSON;
}

declare module 'meteor/http' {
    export var HTTP;
}

declare module 'meteor/email' {
    export var Email;
}

declare var userId;