package protocolsupportpocketstuff.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.typeremapper.pe.PEPacketIDs;
import protocolsupportpocketstuff.ProtocolSupportPocketStuff;
import protocolsupportpocketstuff.api.util.PocketUtils;

import java.util.UUID;

public class EntitySpawnListener extends Connection.PacketListener {
	private ProtocolSupportPocketStuff plugin;
	private Connection con;

	public EntitySpawnListener(ProtocolSupportPocketStuff plugin, Connection con) {
		this.plugin = plugin;
		this.con = con;
	}

	@Override
	public void onRawPacketSending(RawPacketEvent event) {
		super.onRawPacketSending(event);

		ByteBuf data = event.getData();

		int packetId = VarNumberSerializer.readVarInt(data);

		if (packetId != PEPacketIDs.SPAWN_PLAYER && packetId != PEPacketIDs.SPAWN_ENTITY)
			return;

		data.readByte();
		data.readByte();

		UUID uuid = null;
		String name = null;

		if (packetId == PEPacketIDs.SPAWN_PLAYER) {
			// UUID and stuff... we don't care about this but we need to read them anyway
			uuid = MiscSerializer.readUUID(data);
			name = StringSerializer.readString(data, ProtocolVersion.MINECRAFT_PE);
		}

		long entityId = VarNumberSerializer.readSVarLong(data);

		if (!PocketUtils.hasCustomScale((int) entityId)) // Does this entity has custom scale?
			return; // No? Then whatever, I'm outta here!

		ByteBuf serializer = Unpooled.buffer(); // Let's start rewriting our own:tm: packet
		VarNumberSerializer.writeVarInt(serializer, packetId);
		serializer.writeByte(0);
		serializer.writeByte(0);
		if (packetId == PEPacketIDs.SPAWN_PLAYER) {
			MiscSerializer.writeUUID(serializer, uuid);
			StringSerializer.writeString(serializer, ProtocolVersion.MINECRAFT_PE, name);
		}
		VarNumberSerializer.writeSVarLong(serializer, entityId);
		VarNumberSerializer.writeVarLong(serializer, VarNumberSerializer.readVarLong(data)); // runtime ID

		if (packetId == PEPacketIDs.SPAWN_ENTITY) {
			VarNumberSerializer.writeVarInt(serializer, VarNumberSerializer.readVarInt(data)); // type
		}
		MiscSerializer.writeLFloat(serializer, MiscSerializer.readLFloat(serializer)); // position
		MiscSerializer.writeLFloat(serializer, MiscSerializer.readLFloat(serializer)); // position
		MiscSerializer.writeLFloat(serializer, MiscSerializer.readLFloat(serializer)); // position
		MiscSerializer.writeLFloat(serializer, MiscSerializer.readLFloat(serializer)); // motion
		MiscSerializer.writeLFloat(serializer, MiscSerializer.readLFloat(serializer)); // motion
		MiscSerializer.writeLFloat(serializer, MiscSerializer.readLFloat(serializer)); // motion
		MiscSerializer.writeLFloat(serializer, MiscSerializer.readLFloat(serializer)); // pitch
		MiscSerializer.writeLFloat(serializer, MiscSerializer.readLFloat(serializer)); // yaw (or head yaw)
		if (packetId == PEPacketIDs.SPAWN_PLAYER) {
			MiscSerializer.writeLFloat(serializer, MiscSerializer.readLFloat(serializer)); // yaw
			VarNumberSerializer.writeSVarInt(serializer, VarNumberSerializer.readSVarInt(serializer)); // slot
		}
		// TODO: Metadata
		// now comes the cool stuff:tm:
		/* int entries = VarNumberSerializer.readVarInt(serializer);
		VarNumberSerializer.writeVarInt(serializer, entries); // current metadata + scale

		HashMap<Integer, DataWatcherObject> dataWatchers =  new HashMap<Integer, DataWatcherObject>();
		dataWatchers.put(0, new DataWatcherObjectByte());
		dataWatchers.put(1, new DataWatcherObjectShortLe());
		dataWatchers.put(2, new DataWatcherObjectSVarInt());
		dataWatchers.put(3, new DataWatcherObjectFloatLe());
		dataWatchers.put(4, new DataWatcherObjectString());
		dataWatchers.put(5, new DataWatcherObjectItemStack());
		dataWatchers.put(6, new DataWatcherObjectVector3vi());
		dataWatchers.put(7, new DataWatcherObjectSVarLong());
		dataWatchers.put(8, new DataWatcherObjectVector3fLe());

		for (int idx = 0; entries > idx; idx++) {
			int metaType = VarNumberSerializer.readVarInt(serializer);
			DataWatcherObject dw = dataWatchers.get(metaType);
			dw.readFromStream(data, ProtocolVersion.MINECRAFT_PE, I18NData.DEFAULT_LOCALE);
			dw.writeToStream(serializer, ProtocolVersion.MINECRAFT_PE, I18NData.DEFAULT_LOCALE);
		} */
		// and fuck the rest
		serializer.writeBytes(MiscSerializer.readBytes(data, data.readableBytes() - data.readerIndex()));
		event.setData(serializer);
	}
}
