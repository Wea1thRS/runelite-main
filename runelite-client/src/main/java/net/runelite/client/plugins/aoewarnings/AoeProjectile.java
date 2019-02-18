
package net.runelite.client.plugins.aoewarnings;

import java.time.Instant;
import net.runelite.api.coords.LocalPoint;

class AoeProjectile
{
	private final Instant startTime;
	private final LocalPoint targetPoint;
	private final AoeProjectileInfo aoeProjectileInfo;

	AoeProjectile(Instant startTime, LocalPoint targetPoint, AoeProjectileInfo aoeProjectileInfo)
	{
		this.startTime = startTime;
		this.targetPoint = targetPoint;
		this.aoeProjectileInfo = aoeProjectileInfo;
	}

	Instant getStartTime()
	{
		return startTime;
	}

	LocalPoint getTargetPoint()
	{
		return targetPoint;
	}

	AoeProjectileInfo getAoeProjectileInfo()
	{
		return aoeProjectileInfo;
	}
}
