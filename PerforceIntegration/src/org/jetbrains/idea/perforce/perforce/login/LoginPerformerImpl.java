package org.jetbrains.idea.perforce.perforce.login;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.idea.perforce.perforce.ExecResult;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.PerforceTimeoutException;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.P4ParametersConnection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManagerI;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginPerformerImpl implements LoginPerformer {
  private final static Logger LOG = Logger.getInstance(LoginPerformerImpl.class);
  @NonNls private static final String LOGGED_IN_MESSAGE = "logged in";
  @NonNls private static final String NAVIGATE_MESSAGE = "Navigate to URL";
  @NonNls private final static String CONNECT_FAILED = "Connect to server failed; check $P4PORT.";
  @NonNls private final static String CONNECTION_REFUSED = "Connection refused";
  @NonNls private final static String SSO_REQUIRED = "ssoAuth required";
  @NonNls private final static String SSO_OPTIONAL = "ssoAuth optional";

  private final P4Connection myConnection;
  private final PerforceSettings mySettings;
  private final static Pattern ourTicketExpiresPattern =
    Pattern.compile("User (.*) ticket expires in\\s+((\\d*)\\s+hours\\s+)?(\\d+)\\s+minutes.*", Pattern.DOTALL);
  private final PerforceConnectionManagerI myConnectionManager;
  // used for single connection case
  private String myRecentCredentials;

  public LoginPerformerImpl(final Project project, final P4Connection connection, PerforceConnectionManagerI connectionManagerI) {
    mySettings = PerforceSettings.getSettings(project);
    myConnectionManager = connectionManagerI;
    myConnection = connection;
  }

  @Override
  public LoginState getLoginState() {
    try {
      final ExecResult result = myConnection.runP4CommandLine(mySettings, new String[]{"login", "-s"}, null);
      if (result.getExitCode() != 0) {
        final String stdErr = result.getStderr();
        if (stdErr.contains(CONNECT_FAILED) || stdErr.contains(CONNECTION_REFUSED) || stdErr.contains("No route to host")) {
          return new LoginState(false, -1, stdErr);
        }
        if (StringUtil.isEmpty(stdErr)) {
          Throwable exception = result.getException();
          if (exception != null) {
            return new LoginState(false, -1, exception.getMessage());
          }
        }
        return new LoginState(false, -1, null);
      }

      final String stdOut = result.getStdout();
      return tryParseTicketExpiresTime(stdOut);
    } catch (VcsException e) {
      if (e.getCause() instanceof PerforceTimeoutException) {
        return new LoginState(false, -1, e.getMessage());
      }
      return new LoginState(false, -1, null);
    }
  }

  @NotNull
  private static LoginState tryParseTicketExpiresTime(@NotNull String stdOut) {
    final Matcher matcher = ourTicketExpiresPattern.matcher(stdOut);
    if (matcher.matches()) {
      final String hours = matcher.group(3);
      final String minutes = matcher.group(4);
      if (minutes != null) {
        try {
          final long hoursInt = hours != null ? Integer.parseInt(hours) : 0;
          final int minutesInt = Integer.parseInt(minutes);
          return new LoginState(true, ((hoursInt * 60) + minutesInt) * 60 * 1000, null);
        } catch (NumberFormatException e) {
          //
        }
      }
    }
    return new LoginState(true, -1, null);
  }

  @TestOnly
  @NotNull
  public static LoginState parseExpirationTicket(@NotNull String stdOut) {
    return tryParseTicketExpiresTime(stdOut);
  }

  @Override
  public boolean isCredentialsChanged() {
    if (! mySettings.useP4CONFIG) return true;
    if (!Objects.equals(mySettings.getPasswd(), myRecentCredentials)) {
      myRecentCredentials = mySettings.getPasswd();
      return true;
    }
    return false;
  }

  @Override
  public LoginState login(@NotNull final String password) {
    try {
      final StringBuffer data = new StringBuffer();
      data.append(password);
      final ExecResult loginResult = myConnection.runP4CommandLine(mySettings, new String[]{"login"}, data);
      String stdOut = loginResult.getStdout();
      String stdErr = loginResult.getStderr();
      if ((stdErr.length() > 0 && !stdErr.contains(NAVIGATE_MESSAGE)) || !stdOut.contains(LOGGED_IN_MESSAGE)) {
        String message = !stdOut.isEmpty() && !stdErr.isEmpty() ? stdOut + "\n" + stdErr : stdOut + stdErr;
        if (StringUtil.isEmptyOrSpaces(message) && loginResult.getException() != null) {
          message = loginResult.getException().getMessage();
        }
        LOG.debug("Login failed, message: " + message);
        return new LoginState(false, -1, message);
      }
      myConnectionManager.updateConnections();
      return new LoginState(true, -1, null);
    } catch (VcsException e) {
      if (e.getCause() instanceof PerforceTimeoutException) {
        return new LoginState(false, -1, e.getMessage());
      }
      return new LoginState(false, -1, null);
    }
  }

  @Override
  public LoginState loginWithStoredPassword() {
    if (isSSOAuthRequired()) {
      // no password required for SSO
      return login("");
    }
    String password = myConnection instanceof P4ParametersConnection
                      ? ((P4ParametersConnection)myConnection).getParameters().getPassword()
                      : mySettings.getPasswd();
    if (StringUtil.isNotEmpty(password)) {
      return login(password);
    }
    return new LoginState(false, -1, null);
  }

  @Override
  public P4Connection getMyConnection() {
    return myConnection;
  }

  private boolean isSSOAuthRequired() {
    try {
      final ExecResult infoResult = myConnection.runP4CommandLine(mySettings, new String[]{"-ztag"}, new String[]{"info"}, null);
      String infoStr = infoResult.getStdout();
      if (infoStr.contains(SSO_REQUIRED) || infoStr.contains(SSO_OPTIONAL)) {
        LOG.debug("Login: ssoAuth enabled");
        return true;
      }
    } catch (VcsException e) {
      LOG.warn("Failed to fetch ssoAuth status from 'p4 info': " + e.getMessage());
      return false;
    }
    return false;
  }
}
