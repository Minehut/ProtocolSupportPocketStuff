package protocolsupportpocketstuff.skin;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;
import protocolsupport.api.Connection;
import protocolsupport.api.events.PlayerPropertiesResolveEvent;
import protocolsupportpocketstuff.ProtocolSupportPocketStuff;
import protocolsupportpocketstuff.api.event.ComplexFormResponseEvent;
import protocolsupportpocketstuff.api.event.ModalResponseEvent;
import protocolsupportpocketstuff.api.event.ModalWindowResponseEvent;
import protocolsupportpocketstuff.api.event.PocketChangeSkinEvent;
import protocolsupportpocketstuff.api.event.SimpleFormResponseEvent;
import protocolsupportpocketstuff.api.modals.SimpleForm;
import protocolsupportpocketstuff.api.modals.elements.ModalImage;
import protocolsupportpocketstuff.api.modals.elements.ModalImage.ModalImageType;
import protocolsupportpocketstuff.api.modals.elements.simple.ModalButton;
import protocolsupportpocketstuff.api.util.PocketCon;
import protocolsupportpocketstuff.api.util.PocketUtils;
import protocolsupportpocketstuff.api.util.SkinUtils;

public class SkinListener implements Listener {
	
	private ProtocolSupportPocketStuff plugin;
	
	public SkinListener(ProtocolSupportPocketStuff plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerPropertiesResolve(PlayerPropertiesResolveEvent e) {
		Connection con = e.getConnection();
		if (PocketCon.isPocketConnection(con)) {
			if (con.hasMetadata("applySkinOnJoin")) {
				plugin.debug("Applying cached skin for " + e.getConnection() + "...");
				SkinUtils.SkinDataWrapper skinDataWrapper = (SkinUtils.SkinDataWrapper) con.getMetadata("applySkinOnJoin");
				e.addProperty(new PlayerPropertiesResolveEvent.ProfileProperty("textures", skinDataWrapper.getValue(), skinDataWrapper.getSignature()));
				con.removeMetadata("applySkinOnJoin");
			}
		}
	}

	//Test to send packet.
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if(e.getMessage().contains(".meep")) {
			e.getPlayer().sendMessage("Meep!");
			for(Connection con : PocketCon.getPocketConnections()) {
				e.getPlayer().sendMessage("MEEEEEP!!");
				PocketCon.sendModal(con, 
						new SimpleForm("hoi", "hallo")
							.addButton(new ModalButton("Magbot").setImage(new ModalImage(ModalImageType.EXTERNAL_IMAGE, "http://magbot.nl/img/MagBot.png")))
							.addButton(new ModalButton("Awesome").setImage(new ModalImage(ModalImageType.EXTERNAL_IMAGE, "http://yumamom.com/wp-content/uploads/2015/05/LEGO.jpg"))));
			}
		}
		if(e.getMessage().contains(".spawn")) {
			new BukkitRunnable() {
				public void run() {
					Entity entity = e.getPlayer().getWorld().spawnEntity(e.getPlayer().getLocation(), EntityType.PIG);
					e.getPlayer().sendMessage("this is your good old pig");
					new BukkitRunnable() {
						public void run() {
							PocketUtils.setScale(entity, 4);
							e.getPlayer().sendMessage("§c§eNOW HE IS BIG, BOI!!! §7§n(actually just 4x)");
						}
					}.runTaskLater(plugin, 60L);
				}
			}.runTask(plugin);
		}
	}
	
	//TODO: Still causes bugs, but what the hack is wrong with reading this..?
	@EventHandler
	public void onSkinChange(PocketChangeSkinEvent e) {
		plugin.debug("YAA: " + e.getUuid() + " changed skin to " + e.isSlim() + " width: " + e.getSkin().getWidth());
	}
	
	//:F
	@EventHandler
	public void onClientResponse(ModalResponseEvent e) {
		plugin.debug("ClientResponseEvent received ~ " + e.getClass().getSimpleName() + " ~ JSON: " + e.getModalJSON());

		PocketCon.handleModalResponse(e.getConnection(), e);
	}

	@EventHandler
	public void onModalWindowResponse(ModalWindowResponseEvent e) {
		plugin.debug("ModalWindowResponseEvent received ~ " + e.getResult());
	}

	@EventHandler
	public void onSimpleFormResponse(SimpleFormResponseEvent e) {
		plugin.debug("SimpleFormResponseEvent received ~ " + e.getClickedButton());
	}

	@EventHandler
	public void onComplexFormResponse(ComplexFormResponseEvent e) {
		plugin.debug("ComplexFormResponseEvent received ~ " + e.getJsonArray());
	}
	
	//Somehow this seems to mess with a PE client that also has a PC skin.
	//Since we will do PE -> PC skins in the future I do not really care(tm).
	@EventHandler(priority = EventPriority.MONITOR)
	public void propertyResolve(PlayerPropertiesResolveEvent e) {
		if(e.hasProperty(SkinUtils.skinPropertyName) && PocketCon.isPocketConnection(e.getConnection())) {
			e.removeProperty(SkinUtils.skinPropertyName);
		}
	}
	
}
