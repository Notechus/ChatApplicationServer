package com.chatapp.security.authentication;

import javax.security.auth.login.*;

public class UserAuthentication
{
	public void UserAuthentication(String name, char[] password)
	{
		System.getProperty("java.security.auth.login.config", "jaas.config");
		try
		{
			LoginContext lc = new LoginContext("UserAuthentication", new UserLoginCallback(name, password));
			lc.login();
		} catch (LoginException ex)
		{
			ex.printStackTrace();
		} finally
		{
			for (int i = 0; i < password.length; i++)
			{
				password[i] = ' ';
			}
		}

	}
}
