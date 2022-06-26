package net.blf02.immersivemc.common.network.packet;

import net.blf02.immersivemc.common.network.NetworkUtil;
import net.blf02.immersivemc.server.swap.Swap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Used when interacting with something that uses WorldStorage
 */
public class InteractPacket {

    public final BlockPos pos;
    public final int slot;
    public final Hand hand;

    public InteractPacket(BlockPos pos, int slot, Hand hand) {
        this.pos = pos;
        this.slot = slot;
        this.hand = hand;
    }

    public static void encode(InteractPacket packet, PacketBuffer buffer) {
        buffer.writeBlockPos(packet.pos).writeInt(packet.slot)
                .writeInt(packet.hand == Hand.MAIN_HAND ? 0 : 1);
    }

    public static InteractPacket decode(PacketBuffer buffer) {
        return new InteractPacket(buffer.readBlockPos(), buffer.readInt(),
                buffer.readInt() == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND);
    }

    public static void handle(final InteractPacket message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (NetworkUtil.safeToRun(message.pos, player)) {
                BlockState state = player.level.getBlockState(message.pos);
                if (state.getBlock() == Blocks.CRAFTING_TABLE) {
                    Swap.handleCraftingSwap(player, message.slot, message.hand, message.pos);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
