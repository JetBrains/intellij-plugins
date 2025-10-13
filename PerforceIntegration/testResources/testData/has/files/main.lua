--[[
  Mocked Authentication extensions SSO

  Copyright 2020 Perforce Software
]]--

local cjson = require "cjson"
local curl = require "cURL.safe"
package.path = Helix.Core.Server.GetArchDirFileName( "?.lua" )

function GlobalConfigFields()
  return {
    -- The leading ellipsis is used to indicate values that have not been
    -- changed from their default (documentation) values, since it is a wildcard
    -- in Perforce and cannot be used for anything else.
    [ "Service-URL" ] = "... The authentication service base URL.",
    [ "Auth-Protocol" ] = "... Authentication protocol, such as 'saml' or 'oidc'."
  }
end

function InstanceConfigFields()
  return {
    -- The leading ellipsis is used to indicate values that have not been
    -- changed from their default (documentation) values, since it is a wildcard
    -- in Perforce and cannot be used for anything else.
    [ "non-sso-users" ] = "... Those users who will not be using SSO.",
    [ "non-sso-groups" ] = "... Those groups whose members will not be using SSO.",
    [ "user-identifier" ] = "... Trigger variable used as unique user identifier.",
    [ "name-identifier" ] = "... Field within IdP response containing unique user identifer.",
    [ "enable-logging" ] = "... Extension will write debug messages to a log if 'true'."
  }
end

function InstanceConfigEvents()
  return {
    [ "auth-pre-sso" ] = "auth",
    [ "auth-check-sso" ] = "auth"
  }
end

function AuthPreSSO()
  local user = Helix.Core.Server.GetVar( "user" )

  -- only check SSO for users starting with j...
  if (user:find("^j") ~= nil) then
    Helix.Core.Server.log( { [ "AuthPreSSO" ] = "info: user " .. user } )
    return true, "unused", "http://perforce.com", false
  end

  return true, "unused", "http://perforce.com", true
end

function AuthCheckSSO()
  local user = Helix.Core.Server.GetVar( "user" )

  -- fail for john, pass for others
  if user == "john" then
    return false
  end

  return true
end

