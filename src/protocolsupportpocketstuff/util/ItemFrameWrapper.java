package protocolsupportpocketstuff.util;

import net.minecraft.server.v1_12_R1.DataWatcher;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import protocolsupport.api.Connection;
import protocolsupport.protocol.typeremapper.pe.PEDataValues;
import protocolsupport.utils.IntTuple;
import protocolsupport.zplatform.ServerPlatform;
import protocolsupport.zplatform.itemstack.ItemStackWrapper;
import protocolsupport.zplatform.itemstack.NBTTagCompoundWrapper;
import protocolsupportpocketstuff.api.util.PocketCon;
import protocolsupportpocketstuff.packet.TileDataUpdate;
import protocolsupportpocketstuff.packet.UpdateBlockPacket;

import java.util.List;

public class ItemFrameWrapper {
	double x;
	double y;
	double z;
	int facing;

	private static final int flag_update_neighbors = 0b0001;
	private static final int flag_network = 0b0010;
	private static final int flag_priority = 0b1000;

	private static final int flags = (flag_update_neighbors | flag_network | flag_priority);

	private NBTTagCompoundWrapper spawnTag;

	public ItemFrameWrapper(double x, double y, double z, int facing) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.facing = facing;
	}

	public NBTTagCompoundWrapper getSpawnTag() {
		if (spawnTag == null) {
			spawnTag = ServerPlatform.get().getWrapperFactory().createEmptyNBTCompound();
			spawnTag.setInt("x", (int) x);
			spawnTag.setInt("y", (int) y);
			spawnTag.setInt("z", (int) z);

			spawnTag.setString("id", "ItemFrame");
		}
		return spawnTag;
	}

	public void spawn(Connection connection) {
		int peFacing = 0;

		switch (facing) {
			case 3:
				peFacing = 0;
				break;
			case 0:
				peFacing = 2;
				break;
			case 2:
				peFacing = 3;
				break;
			case 1:
				peFacing = 1;
				break;
			default:
				break;
		}
		// First we change the block type...
		// Item Frame block ID is 199
		UpdateBlockPacket updateBlockPacket = new UpdateBlockPacket((int) x, (int) y, (int) z, 199, peFacing, flags);

		PocketCon.sendPocketPacket(connection, updateBlockPacket);

		// Now we are going to set the item frame NBT tag
		NBTTagCompoundWrapper tag = getSpawnTag();
		tag.setByte("ItemRotation", 0);
		tag.setFloat("ItemDropChance", 1);

		TileDataUpdate tileDataUpdate = new TileDataUpdate((int) x, (int) y, (int) z, tag);

		PocketCon.sendPocketPacket(connection, tileDataUpdate);
	}

	public void despawn(Connection connection) {
		// We are going to set it to air because... well, there isn't too many other choices I guess *shrugs*
		UpdateBlockPacket updateBlockPacket = new UpdateBlockPacket((int) x, (int) y, (int) z, 0, 0, flags);
		PocketCon.sendPocketPacket(connection, updateBlockPacket);
	}

	public void updateMetadata(Connection connection, List<DataWatcher.Item<?>> dataWatchers) {
		NBTTagCompoundWrapper tag = getSpawnTag();

		for (DataWatcher.Item dw : dataWatchers) {
			// Obfuscation Helper: dw.a().a() = DataWatcher ID
			switch (dw.a().a()) {
				case 6:
					net.minecraft.server.v1_12_R1.ItemStack item = (net.minecraft.server.v1_12_R1.ItemStack) dw.b();
					ItemStack itemStack = CraftItemStack.asBukkitCopy(item);
					ItemStackWrapper wrapper = ServerPlatform.get().getWrapperFactory().createItemStack(itemStack.getType());
					wrapper.setData(itemStack.getDurability());
					NBTTagCompoundWrapper itemTag = ServerPlatform.get().getWrapperFactory().createEmptyNBTCompound();

					IntTuple itemAndData = PEDataValues.ITEM_ID.getRemap(wrapper.getTypeId(), wrapper.getData());

					if (itemAndData != null) {
						wrapper.setTypeId(itemAndData.getI1());

						if (itemAndData.getI2() != -1) {
							wrapper.setData(itemAndData.getI2());
						}
					}

					itemTag.setShort("id", wrapper.getTypeId());
					itemTag.setByte("Count", 1);
					itemTag.setShort("Damage", 0);

					tag.setCompound("Item", itemTag);
					break;
				case 7:
					int itemRotation = (int) dw.b();
					tag.setByte("ItemRotation", itemRotation);
					break;
				default:
					break;
			}
		}

		TileDataUpdate tileDataUpdate = new TileDataUpdate((int) x, (int) y, (int) z, tag);

		PocketCon.sendPocketPacket(connection, tileDataUpdate);
	}
}
