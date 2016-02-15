package com.chatapp.security.authentication;

import java.io.IOException;

import javax.security.auth.callback.*;

public class UserLoginCallback implements CallbackHandler
{
	private String name;
	private char[] password;

	public UserLoginCallback(String name, char[] password)
	{
		this.name = name;
		this.password = password;
	}

	@Override
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
	{
		for (int i = 0; i < callbacks.length; i++)
		{
			if (callbacks[i] instanceof NameCallback)
			{
				NameCallback nameCallback = (NameCallback) callbacks[i];
				nameCallback.setName(name);
			} else if (callbacks[i] instanceof PasswordCallback)
			{
				PasswordCallback passwordCallback = (PasswordCallback) callbacks[i];
				passwordCallback.setPassword(password);
				for (int j = 0; j < password.length; j++)
				{
					password[j] = ' ';
				}
			} else
			{
				throw new UnsupportedCallbackException(callbacks[i], "The submitted Callback is unsupported");
			}
		}
	}

}
