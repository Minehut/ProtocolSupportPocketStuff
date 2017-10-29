package protocolsupportpocketstuff.api.util;

import org.bukkit.entity.Entity;
import protocolsupport.api.Connection;
import protocolsupport.protocol.utils.datawatcher.DataWatcherObject;
import protocolsupport.protocol.utils.datawatcher.objects.DataWatcherObjectFloatLe;
import protocolsupport.utils.CollectionsUtils;
import protocolsupportpocketstuff.packet.play.EntityDataPacket;

import java.util.concurrent.ConcurrentHashMap;

public class PocketUtils {
	private static final ConcurrentHashMap<Integer, Float> entityScales = new ConcurrentHashMap<>();

	public static void setScale(Entity entity, float scale) {
		setScale(entity.getEntityId(), scale);
	}

	public static void setScale(int entityId, float scale) {
		if (scale == 1) {
			entityScales.remove(entityId);
		} else {
			entityScales.put(entityId, scale);
		}
		// Update to all players
		CollectionsUtils.ArrayMap<DataWatcherObject<?>> metadata = new CollectionsUtils.ArrayMap<>(76);
		metadata.put(39, new DataWatcherObjectFloatLe(scale));
		EntityDataPacket packet = new EntityDataPacket(entityId, metadata);
		for (Connection connection : PocketCon.getPocketConnections()) {
			PocketCon.sendPocketPacket(connection, packet);
			System.out.println("Sending packet...");
		}
	}

	public static boolean hasCustomScale(int entityId) {
		return entityScales.containsKey(entityId);
	}

	public static float getCustomScale(int entityId) {
		return entityScales.get(entityId);
	}
}
