package protocolsupportpocketstuff.hacks.teams;

import io.netty.buffer.ByteBuf;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam;
import protocolsupport.api.Connection;
import protocolsupport.protocol.serializer.MiscSerializer;
import protocolsupport.protocol.serializer.StringSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.typeremapper.pe.PEPacketIDs;
import protocolsupport.protocol.utils.datawatcher.DataWatcherObject;
import protocolsupport.protocol.utils.datawatcher.objects.DataWatcherObjectString;
import protocolsupport.utils.CollectionsUtils;
import protocolsupportpocketstuff.ProtocolSupportPocketStuff;
import protocolsupportpocketstuff.api.util.PocketCon;
import protocolsupportpocketstuff.packet.play.EntityDataPacket;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TeamsPacketListener extends Connection.PacketListener {
	private ProtocolSupportPocketStuff plugin;
	private Connection con;
	private HashMap<String, CachedUser> cachedUsers = new HashMap<String, CachedUser>();
	private HashMap<String, CachedTeam> cachedTeams = new HashMap<String, CachedTeam>();

	public TeamsPacketListener(ProtocolSupportPocketStuff plugin, Connection con) {
		this.plugin = plugin;
		this.con = con;
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		super.onPacketSending(event);

		if (!(event.getPacket() instanceof PacketPlayOutScoreboardTeam))
			return;

		PacketPlayOutScoreboardTeam packet = (PacketPlayOutScoreboardTeam) event.getPacket();

		String teamName = (String) get(packet, "a");
		int mode = getInt(packet, "i");

		if (mode == 0) { // create
			String prefix = (String) get(packet, "c");
			String suffix = (String) get(packet, "d");
			Collection<String> entities = (Collection<String>) get(packet, "h");

			CachedTeam team = new CachedTeam(prefix, suffix);

			for (String entity : entities) {
				team.getPlayers().add(entity);
			}

			cachedTeams.put(teamName, team);
			team.updatePlayers(this);
			return;
		}
		if (mode == 1) { // delete
			if (cachedTeams.containsKey(teamName)) {
				CachedTeam team = cachedTeams.get(teamName);
				team.removePlayers(team.players, this);
				cachedTeams.remove(team);
			}
		}
		if (mode == 2) { // update
			if (cachedTeams.containsKey(teamName)) {
				CachedTeam team = cachedTeams.get(teamName);
				String prefix = (String) get(packet, "c");
				String suffix = (String) get(packet, "d");
				team.setPrefix(prefix);
				team.setSuffix(suffix);
				team.updatePlayers(this);
			}
		}
		if (mode == 3) { // add players
			if (cachedTeams.containsKey(teamName)) {
				Collection<String> entities = (Collection<String>) get(packet, "h");
				CachedTeam team = cachedTeams.get(teamName);

				for (String entity : entities) {
					team.getPlayers().add(entity);
				}
				team.updatePlayers(this);
			}
		}
		if (mode == 4) { // remove players
			if (cachedTeams.containsKey(teamName)) {
				Collection<String> entities = (Collection<String>) get(packet, "h");
				CachedTeam team = cachedTeams.get(teamName);
				team.removePlayers(entities, this);
			}
		}
	}

	@Override
	public void onRawPacketSending(RawPacketEvent event) {
		super.onRawPacketSending(event);

		ByteBuf data = event.getData();
		int packetId = VarNumberSerializer.readVarInt(data);

		if (packetId != PEPacketIDs.SPAWN_PLAYER)
			return;

		data.readByte();
		data.readByte();

		UUID uuid = MiscSerializer.readUUID(data);
		String name = StringSerializer.readString(data, con.getVersion());
		long entityId = VarNumberSerializer.readSVarLong(data);

		cachedUsers.put(name, new CachedUser(uuid, entityId));
	}

	public static Object get(Object source, String fieldName) {
		try {
			Field field = source.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(source);
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Something went wrong!");
	}

	public static int getInt(Object source, String fieldName) {
		try {
			Field field = source.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.getInt(source);
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Something went wrong!");
	}

	static class CachedUser {
		private UUID uuid;
		private long entityId;

		public CachedUser(UUID uuid, long entityId) {
			this.uuid = uuid;
			this.entityId = entityId;
		}

		public UUID getUuid() {
			return uuid;
		}

		public long getEntityId() {
			return entityId;
		}
	}

	static class CachedTeam {
		private String prefix;
		private String suffix;
		private List<String> players = new ArrayList<String>();

		public CachedTeam(String prefix, String suffix) {
			this.prefix = prefix;
			this.suffix = suffix;
		}

		public void updatePlayers(TeamsPacketListener listener) {
			for (String player : players) {
				if (listener.cachedUsers.containsKey(player)) {
					CachedUser cachedUser = listener.cachedUsers.get(player);
					CollectionsUtils.ArrayMap<DataWatcherObject<?>> metadata = new CollectionsUtils.ArrayMap<>(76);
					metadata.put(4, new DataWatcherObjectString(prefix + player + suffix));
					EntityDataPacket packet = new EntityDataPacket(cachedUser.getEntityId(), metadata);
					PocketCon.sendPocketPacket(listener.con, packet);
				}
			}
		}

		public void removePlayers(Collection<String> players, TeamsPacketListener listener) {
			for (String player : players) {
				this.players.remove(player);

				if (listener.cachedUsers.containsKey(player)) {
					CachedUser cachedUser = listener.cachedUsers.get(player);
					CollectionsUtils.ArrayMap<DataWatcherObject<?>> metadata = new CollectionsUtils.ArrayMap<>(76);
					metadata.put(4, new DataWatcherObjectString(player));
					EntityDataPacket packet = new EntityDataPacket(cachedUser.getEntityId(), metadata);
					PocketCon.sendPocketPacket(listener.con, packet);
				}
			}
		}

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public String getSuffix() {
			return suffix;
		}

		public void setSuffix(String suffix) {
			this.suffix = suffix;
		}

		public List<String> getPlayers() {
			return players;
		}
	}
}
