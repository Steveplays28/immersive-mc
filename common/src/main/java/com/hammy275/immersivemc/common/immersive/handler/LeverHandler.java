package com.hammy275.immersivemc.common.immersive.handler;

import com.hammy275.immersivemc.ImmersiveMC;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.api.server.ItemSwapAmount;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.immersive.ImmersiveCheckers;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class LeverHandler implements ImmersiveHandler<NullStorage> {
    @Override
    public NullStorage makeInventoryContents(ServerPlayer player, BlockPos pos) {
        return new NullStorage();
    }

    @Override
    public NullStorage getEmptyNetworkStorage() {
        return new NullStorage();
    }

    @Override
    public void swap(int slot, InteractionHand hand, BlockPos pos, ServerPlayer player, ItemSwapAmount amount) {
        // NO-OP.
    }

    @Override
    public boolean isDirtyForClientSync(ServerPlayer player, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isValidBlock(BlockPos pos, Level level) {
        return ImmersiveCheckers.isLever(pos, level);
    }

    @Override
    public boolean enabledInConfig(Player player) {
        return ActiveConfig.getActiveConfigCommon(player).useLeverImmersive;
    }

    @Override
    public boolean clientAuthoritative() {
        return true;
    }

    @Override
    public ResourceLocation getID() {
        return ResourceLocation.fromNamespaceAndPath(ImmersiveMC.MOD_ID, "lever");
    }
}
