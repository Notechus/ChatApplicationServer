package com.chatapp.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UniqueIdentifier
{
	private static List<Integer> ids = new ArrayList<>();
	private static final int RANGE = 10000;

	private static int index = 0;

	static
	{
		for (int i = 0; i < RANGE; i++)
		{
			ids.add(i + 1); // we must ensure that every of ids gets number but
							// it must be !=0
		}
		Collections.shuffle(ids); // permutes ids
	}

	private UniqueIdentifier()
	{

	}

	public static int getIdentifier()
	{
		if (index > ids.size() - 1)
		{
			index = 0;
		}
		return ids.get(index++);
	}
}
