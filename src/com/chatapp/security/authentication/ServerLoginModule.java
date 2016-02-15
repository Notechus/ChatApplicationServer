package com.chatapp.security.authentication;

import java.io.IOException;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class ServerLoginModule implements LoginModule
{

	private Subject subject;
	private CallbackHandler callbackHandler;
	private Map sharedState;
	private Map options;

	private String username;
	private char[] password;

	private boolean succeeded = false;
	private boolean debug = false;

	public ServerLoginModule(String name, char[] password)
	{
		// this.name = name;
		// this.password = password;
		// log it
		System.out.println("Login Module - constructor called");
	}

	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
			Map<String, ?> options)
	{
		System.out.println("Login Module - initialize called");
		this.subject = subject;
		this.callbackHandler = callbackHandler;
		this.sharedState = sharedState;
		this.options = options;

		System.out.println("testOption value: " + (String) options.get("testOption"));

		succeeded = false;

	}

	@Override
	public boolean login() throws LoginException
	{
		System.out.println("Login Module - login called");
		if (callbackHandler == null)
		{
			throw new LoginException("Oops, callbackHandler is null");
		}

		Callback[] callbacks = new Callback[2];
		callbacks[0] = new NameCallback("user name:");
		callbacks[1] = new PasswordCallback("password:", false);
		try
		{
			callbackHandler.handle(callbacks);
			username = ((NameCallback) callbacks[0]).getName();
			char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
			if (tmpPassword == null)
			{
				// treat a NULL password as an empty password
				tmpPassword = new char[0];
			}
			password = new char[tmpPassword.length];
			System.arraycopy(tmpPassword, 0, password, 0, tmpPassword.length);
			((PasswordCallback) callbacks[1]).clearPassword();
		} catch (IOException e)
		{
			throw new LoginException("Oops, IOException calling handle on callbackHandler");
		} catch (UnsupportedCallbackException e)
		{
			throw new LoginException("Oops, UnsupportedCallbackException calling handle on callbackHandler");
		}

		// print debugging information
		if (debug)
		{
			System.out.println("\t\t[SampleLoginModule] " + "user entered user name: " + username);
			System.out.print("\t\t[SampleLoginModule] " + "user entered password: ");
			for (int i = 0; i < password.length; i++)
				System.out.print(password[i]);
			System.out.println();
		}

		// verify the username/password
		boolean usernameCorrect = false;
		boolean passwordCorrect = false;
		if (username.equals("testUser")) usernameCorrect = true;
		if (usernameCorrect && password.length == 12 && password[0] == 't' && password[1] == 'e' && password[2] == 's'
				&& password[3] == 't' && password[4] == 'P' && password[5] == 'a' && password[6] == 's'
				&& password[7] == 's' && password[8] == 'w' && password[9] == 'o' && password[10] == 'r'
				&& password[11] == 'd')
		{

			// authentication succeeded!!!
			passwordCorrect = true;
			if (debug) System.out.println("\t\t[SampleLoginModule] " + "authentication succeeded");
			succeeded = true;
			return true;
		} else
		{

			// authentication failed -- clean out state
			if (debug) System.out.println("\t\t[SampleLoginModule] " + "authentication failed");
			succeeded = false;
			username = null;
			for (int i = 0; i < password.length; i++)
				password[i] = ' ';
			password = null;
			if (!usernameCorrect)
			{
				throw new FailedLoginException("User Name Incorrect");
			} else
			{
				throw new FailedLoginException("Password Incorrect");
			}
		}

	}

	@Override
	public boolean commit() throws LoginException
	{
		System.out.println("Login Module - commit called");
		return succeeded;
	}

	@Override
	public boolean abort() throws LoginException
	{
		System.out.println("Login Module - abort called");
		return false;
	}

	@Override
	public boolean logout() throws LoginException
	{
		System.out.println("Login Module - logout called");
		return false;
	}

}
