package net.blf02.immersivemc.client.subscribe;

import net.blf02.immersivemc.client.immersive.AbstractImmersive;
import net.blf02.immersivemc.client.immersive.Immersives;
import net.blf02.immersivemc.client.immersive.info.AbstractImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.AbstractTileEntityImmersiveInfo;
import net.blf02.immersivemc.client.immersive.info.CraftingInfo;
import net.blf02.immersivemc.client.swap.ClientSwap;
import net.blf02.immersivemc.common.network.Network;
import net.blf02.immersivemc.common.network.packet.SwapPacket;
import net.blf02.immersivemc.common.util.Util;
import net.blf02.immersivemc.common.vr.VRPluginVerify;
import net.blf02.vrapi.api.data.IVRData;
import net.blf02.vrapi.api.data.IVRPlayer;
import net.blf02.vrapi.event.VRPlayerTickEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

import java.util.Optional;

public class ClientVRSubscriber {

    // Global cooldown to prevent rapid-fire VR interactions
    protected int cooldown = 0;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void immersiveTickVR(VRPlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.side != LogicalSide.CLIENT) return;
        VRPluginVerify.isInVR = true;
        if (cooldown > 0) {
            cooldown--;
        } else {
            for (AbstractImmersive<? extends AbstractImmersiveInfo> singleton : Immersives.IMMERSIVES) {
                for (AbstractImmersiveInfo info : singleton.getTrackedObjects()) {
                    if (handleInfo(info, event.vrPlayer)) {
                        return;
                    }
                }
            }
        }
    }

    protected boolean handleInfo(AbstractImmersiveInfo info, IVRPlayer vrPlayer) {
        if (info.hasHitboxes()) {
            for (int c = 0; c <= 1; c++) {
                IVRData controller = vrPlayer.getController(c);
                Vector3d pos = controller.position();
                Optional<Integer> hit = Util.getFirstIntersect(pos, info.getAllHitboxes());
                if (hit.isPresent()) {
                    if (info instanceof AbstractTileEntityImmersiveInfo<?>) {
                        AbstractTileEntityImmersiveInfo<?> tInfo = (AbstractTileEntityImmersiveInfo<?>) info;
                        Network.INSTANCE.sendToServer(new SwapPacket(tInfo.getTileEntity().getBlockPos(),
                                hit.get(), c == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND));
                    } else if (info instanceof CraftingInfo) {
                        ClientSwap.craftingSwap(hit.get(), c == 0 ? Hand.MAIN_HAND : Hand.OFF_HAND);
                    }

                    cooldown = 25;
                    return true;
                }
            }
        }
        return false;
    }
}
