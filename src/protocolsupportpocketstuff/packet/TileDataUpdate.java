package protocolsupportpocketstuff.packet;

import io.netty.buffer.ByteBuf;
import protocolsupport.api.Connection;
import protocolsupport.protocol.serializer.ItemStackSerializer;
import protocolsupport.protocol.serializer.PositionSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.typeremapper.pe.PEPacketIDs;
import protocolsupport.protocol.typeremapper.tileentity.TileNBTRemapper;
import protocolsupport.protocol.utils.minecraftdata.MinecraftData;
import protocolsupport.protocol.utils.types.Position;
import protocolsupport.zplatform.itemstack.NBTTagCompoundWrapper;

public class TileDataUpdate extends PEPacket {

	private int x;
	private int y;
	private int z;

	private NBTTagCompoundWrapper tag;

	public TileDataUpdate() { }

	public TileDataUpdate(int x, int y, int z, NBTTagCompoundWrapper tag) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.tag = tag;
	}

	@Override
	public int getPacketId() {
		return PEPacketIDs.TILE_DATA_UPDATE;
	}

	@Override
	public void toData(Connection connection, ByteBuf serializer) {
		PositionSerializer.writePEPosition(serializer, new Position(x, y, z));
		ItemStackSerializer.writeTag(serializer, true, connection.getVersion(), TileNBTRemapper.remap(connection.getVersion(), tag));
	}

	@Override
	public void readFromClientData(Connection connection, ByteBuf clientData) {
		throw new UnsupportedOperationException();
	}
}
