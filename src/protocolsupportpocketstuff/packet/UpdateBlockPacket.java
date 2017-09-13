package protocolsupportpocketstuff.packet;

import io.netty.buffer.ByteBuf;
import protocolsupport.api.Connection;
import protocolsupport.protocol.serializer.PositionSerializer;
import protocolsupport.protocol.serializer.VarNumberSerializer;
import protocolsupport.protocol.typeremapper.pe.PEPacketIDs;
import protocolsupport.protocol.utils.minecraftdata.MinecraftData;
import protocolsupport.protocol.utils.types.Position;

public class UpdateBlockPacket extends PEPacket {

	private int x;
	private int y;
	private int z;

	private int blockId;
	private int state;

	private int flags;

	public UpdateBlockPacket() { }

	public UpdateBlockPacket(int x, int y, int z, int blockId, int state, int flags) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.blockId = blockId;
		this.state = state;
		this.flags = flags;
	}

	@Override
	public int getPacketId() {
		return PEPacketIDs.UPDATE_BLOCK;
	}

	@Override
	public void toData(Connection connection, ByteBuf serializer) {
		PositionSerializer.writePEPosition(serializer, new Position(x, y, z));
		VarNumberSerializer.writeVarInt(serializer, blockId);
		VarNumberSerializer.writeVarInt(serializer, (flags << 4) | MinecraftData.getBlockDataFromState(state));
	}

	@Override
	public void readFromClientData(Connection connection, ByteBuf clientData) {
		throw new UnsupportedOperationException();
	}
}
