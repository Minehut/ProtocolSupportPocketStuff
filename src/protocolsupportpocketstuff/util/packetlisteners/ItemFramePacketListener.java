package protocolsupportpocketstuff.util.packetlisteners;

import net.minecraft.server.v1_12_R1.DataWatcher;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_12_R1.PacketPlayOutSpawnEntity;
import protocolsupport.api.Connection;
import protocolsupportpocketstuff.util.ItemFrameWrapper;
import protocolsupportpocketstuff.util.ReflectionUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ItemFramePacketListener extends Connection.PacketListener {
	Connection connection;
	ConcurrentHashMap<Integer, ItemFrameWrapper> itemFrames = new ConcurrentHashMap<Integer, ItemFrameWrapper>();

	public ItemFramePacketListener(Connection connection) {
		this.connection = connection;
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		super.onPacketSending(event);

		// ===[ SPAWN ]===
		if (event.getPacket() instanceof PacketPlayOutSpawnEntity) {
			PacketPlayOutSpawnEntity packet = (PacketPlayOutSpawnEntity) event.getPacket();
			try {
				// Entity ID
				int entityId = ReflectionUtils.getInt("a", packet);
				// Entity Type ID
				int typeId = ReflectionUtils.getInt("k", packet);
				// Item Frame Facing Direction
				int facing = ReflectionUtils.getInt("l", packet);

				if (typeId == 71) {
					double x = ReflectionUtils.getDouble("c", packet);
					double y = ReflectionUtils.getDouble("d", packet);
					double z = ReflectionUtils.getDouble("e", packet);

					ItemFrameWrapper wrapper = new ItemFrameWrapper(x, y, z, facing);
					// Spawn the item frame!
					wrapper.spawn(connection);
					itemFrames.put(entityId, wrapper);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		// ===[ DESPAWN ]===
		if (event.getPacket() instanceof PacketPlayOutEntityDestroy) {
			PacketPlayOutEntityDestroy packet = (PacketPlayOutEntityDestroy) event.getPacket();
			try {
				int[] toRemove = (int[]) ReflectionUtils.get("a", packet);

				for (int i : toRemove) {
					if (itemFrames.containsKey(i)) {
						ItemFrameWrapper wrapper = itemFrames.get(i);
						// Despawn the item frame because the item frame was destroyed server side
						wrapper.despawn(connection);
						// If the item frame is removed, let's also remove it from our cache
						itemFrames.remove(i);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}

		if (event.getPacket() instanceof PacketPlayOutEntityMetadata) {
			PacketPlayOutEntityMetadata packet = (PacketPlayOutEntityMetadata) event.getPacket();
			try {
				int entityId = ReflectionUtils.getInt("a", packet);

				if (itemFrames.containsKey(entityId)) {
					List<DataWatcher.Item<?>> dataWatchers = (List<DataWatcher.Item<?>>) ReflectionUtils.get("b", packet);

					ItemFrameWrapper wrapper = itemFrames.get(entityId);

					wrapper.updateMetadata(connection, dataWatchers);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
	}
}
