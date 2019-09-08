package net.runelite.client.plugins.inferno.displaymodes;

import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PACKAGE)
public enum SafespotDisplayMode
{
	OFF("Off"),
	INDIVIDUAL_TILES("Individual tiles"),
	AREA("Area (low fps)");

	final private String name;

	SafespotDisplayMode(String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return this.name;
	}
}
