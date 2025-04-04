package com.hammy275.immersivemc.common.network.packet;

import com.hammy275.immersivemc.client.SafeClientUtil;
import com.hammy275.immersivemc.common.config.PlacementMode;
import com.hammy275.immersivemc.server.api_impl.ItemSwapAmountImpl;
import com.hammy275.immersivemc.server.storage.world.ImmersiveMCPlayerStorages;
import com.hammy275.immersivemc.server.swap.Swap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class BackpackInteractPacket {

    public final int slot;
    public final InteractionHand hand;
    public final PlacementMode placementMode;

    public BackpackInteractPacket(int slot, InteractionHand hand) {
        this(slot, hand, SafeClientUtil.getPlacementMode(true));
    }

    public BackpackInteractPacket(int slot, InteractionHand hand, PlacementMode placementMode) {
        this.slot = slot;
        this.hand = hand;
        this.placementMode = placementMode;
    }

    public static void encode(BackpackInteractPacket packet, RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(packet.slot);
        buffer.writeInt(packet.hand == InteractionHand.MAIN_HAND ? 0 : 1);
        buffer.writeEnum(packet.placementMode);
    }

    public static BackpackInteractPacket decode(RegistryFriendlyByteBuf buffer) {
        return new BackpackInteractPacket(buffer.readInt(),
                buffer.readInt() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                buffer.readEnum(PlacementMode.class));
    }

    public static void handle(final BackpackInteractPacket message, ServerPlayer player) {
        if (player != null) {
            // -27 below since 0-26 are inventory slots
            Swap.handleBackpackCraftingSwap(message.slot - 27, message.hand,
                    ImmersiveMCPlayerStorages.getBackpackCraftingStorage(player), player, new ItemSwapAmountImpl(message.placementMode));
        }
        
    }

}
