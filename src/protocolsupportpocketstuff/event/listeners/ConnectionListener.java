package protocolsupportpocketstuff.event.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import protocolsupport.api.Connection;
import protocolsupport.api.ProtocolVersion;
import protocolsupport.api.events.ConnectionHandshakeEvent;
import protocolsupportpocketstuff.ProtocolSupportPocketStuff;
import protocolsupportpocketstuff.util.packetlisteners.ItemFramePacketListener;

public class ConnectionListener implements Listener {

	private ProtocolSupportPocketStuff plugin;

	public ConnectionListener(ProtocolSupportPocketStuff plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onHandshake(ConnectionHandshakeEvent e) {
		Connection connection = e.getConnection();

		if (connection.getVersion() == ProtocolVersion.MINECRAFT_PE) {
			connection.addPacketListener(new ItemFramePacketListener(connection));
		}
	}
}
