package com.hammy275.immersivemc.client.immersive;

import com.hammy275.immersivemc.Platform;
import com.hammy275.immersivemc.api.common.hitbox.BoundingBox;
import com.hammy275.immersivemc.api.common.hitbox.OBBFactory;
import com.hammy275.immersivemc.api.common.immersive.ImmersiveHandler;
import com.hammy275.immersivemc.client.ClientUtil;
import com.hammy275.immersivemc.client.immersive.info.AbstractPlayerAttachmentInfo;
import com.hammy275.immersivemc.client.immersive.info.ImmersiveHitboxesInfo;
import com.hammy275.immersivemc.common.config.ActiveConfig;
import com.hammy275.immersivemc.common.config.CommonConstants;
import com.hammy275.immersivemc.common.immersive.storage.network.impl.NullStorage;
import com.hammy275.immersivemc.common.vr.VRPlugin;
import com.hammy275.immersivemc.common.vr.VRPluginVerify;
import com.hammy275.immersivemc.common.vr.VRRumble;
import com.mojang.blaze3d.vertex.PoseStack;
import net.blf02.vrapi.api.data.IVRData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Used for hitboxes attached to the player
 */
public class ImmersiveHitboxes extends AbstractPlayerAttachmentImmersive<ImmersiveHitboxesInfo, NullStorage> {

    private static final Minecraft mc = Minecraft.getInstance();
    
    private static final double backpackHeight = 0.625;
    private static final Vec3 DOWN = new Vec3(0, -1, 0);
    private int backpackCooldown = 0;

    public ImmersiveHitboxes() {
        super(1);
        this.forceDisableItemGuide = true;
        this.forceTickEvenIfNoTrack = true;
    }

    @Override
    protected void renderTick(ImmersiveHitboxesInfo info, boolean isInVR) {
        super.renderTick(info, isInVR);
        if (ActiveConfig.active().reachBehindBagMode.usesBehindBack() && VRPluginVerify.clientInVR()) {
            // centerPos is the center of the back of the player
            IVRData hmdData = Platform.isDevelopmentEnvironment() ? null : VRPlugin.API.getRenderVRPlayer().getHMD();
            Vec3 centerPos = hmdData != null ?
                    hmdData.position().add(0, -0.5, 0).add(hmdData.getLookAngle().scale(-0.15)) :
                    mc.player.getEyePosition(mc.getTimer().getGameTimeDeltaTicks()).add(0, -0.5, 0).add(mc.player.getLookAngle().scale(-0.15));
            double yaw;
            Vec3 headLook;
            if (VRPluginVerify.clientInVR() && VRPlugin.API.playerInVR(mc.player)
                    && !Platform.isDevelopmentEnvironment()) {
                yaw = Math.toRadians(hmdData.getYaw());
                headLook = hmdData.getLookAngle();
            } else {
                // Yaw based on player's yaw for testing in dev
                yaw = Math.toRadians(mc.player.getYRot());
                headLook = mc.player.getLookAngle();
            }
            headLook = headLook.multiply(1, 0, 1).normalize(); // Ignore y rotation
            centerPos = centerPos.add(headLook.scale(-0.25));
            // Back is 0.5 blocks across from center, making size 0.35 longways (full back has funny accidental detections).
            // Since +Z is 0 yaw, we make the length across the back 0.35 on the X-axis.
            // Add 0.2 to have some sane minimum
            info.setHitbox(ImmersiveHitboxesInfo.BACKPACK_BACK_INDEX,
                    OBBFactory.instance().create(AABB.ofSize(centerPos, 0.35, backpackHeight, 0.2),
                            0, yaw, 0));
        } else {
            // In case setting changes mid-game
            info.setHitbox(ImmersiveHitboxesInfo.BACKPACK_BACK_INDEX, null);
        }

        if (ActiveConfig.active().reachBehindBagMode.usesOverShoulder() && VRPluginVerify.clientInVR()) {
            IVRData hmdData = Platform.isDevelopmentEnvironment() ?
                    VRPlugin.API.getVRPlayer(mc.player).getHMD() :
                    VRPlugin.API.getRenderVRPlayer().getHMD();
            IVRData c1Data = Platform.isDevelopmentEnvironment() ?
                    VRPlugin.API.getVRPlayer(mc.player).getController1() :
                    VRPlugin.API.getRenderVRPlayer().getController1();

            Vec3 hmdDir = hmdData.getLookAngle();
            Vec3 hmdPos = hmdData.position();
            Vec3 c1Dir = c1Data.getLookAngle();
            Vec3 c1Pos = c1Data.position();

            Vec3 c1ToHMDDir = c1Pos.subtract(hmdPos).normalize(); // Angle for c1 to "look at" HMD.

            double angleToDown = Math.acos(DOWN.dot(c1Dir)); // Angle in radians between straight down and the controller dir
            boolean pointingDown = angleToDown < Math.PI / 2d;
            double c1HMDAngleDiff = Math.acos(c1ToHMDDir.dot(hmdDir));
            boolean behindHMD = c1HMDAngleDiff > 2 * Math.PI / 3d;

            if (pointingDown && behindHMD) {
                doBagOpen(mc.player);
            }
        }
    }

    @Override
    public @Nullable ImmersiveHandler getHandler() {
        return null;
    }

    @Override
    protected void doTick(ImmersiveHitboxesInfo info, boolean isInVR) {
        super.doTick(info, isInVR);
        if (backpackCooldown > 0) {
            backpackCooldown--;
        }
    }

    @Override
    public boolean shouldRender(ImmersiveHitboxesInfo info, boolean isInVR) {
        return true;
    }

    @Override
    protected void render(ImmersiveHitboxesInfo info, PoseStack stack, boolean isInVR) {
        BoundingBox backpackHitbox = info.getHitbox(ImmersiveHitboxesInfo.BACKPACK_BACK_INDEX);
        if (backpackHitbox != null) {
            renderHitbox(stack, backpackHitbox);
            if (VRPluginVerify.hasAPI && VRPlugin.API.playerInVR(mc.player)
            && mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
                IVRData c1 = VRPlugin.API.getVRPlayer(mc.player).getController1();
                if (BoundingBox.contains(backpackHitbox, c1.position())) {
                    renderHitbox(stack, AABB.ofSize(c1.position(), 0.25, 0.25, 0.25),
                            true,
                            0f, 1f, 0f);
                }
            }
        }
    }

    @Override
    public boolean enabledInConfig() {
        return true; // We always have this enabled in config
    }

    @Override
    protected boolean inputSlotShouldRenderHelpHitbox(ImmersiveHitboxesInfo info, int slotNum) {
        return false; // No help hitboxes
    }

    @Override
    public boolean shouldTrack(BlockPos pos, Level level) {
        return true; // Prevents info instances from being removed. Okay to do since trackObject() is a no-op.
    }

    @Override
    public ImmersiveHitboxesInfo refreshOrTrackObject(BlockPos pos, Level level) {
        // Return null. Never tracking any objects.
        return null;
    }

    @Override
    public boolean shouldBlockClickIfEnabled(AbstractPlayerAttachmentInfo info) {
        return false; // Doesn't really matter, never hooked into a block anyways
    }

    @Override
    protected void initInfo(ImmersiveHitboxesInfo info) {
        // No need to init, all init things are done in doTick, which needs to run every tick anyways
    }

    @Override
    public void handleRightClick(AbstractPlayerAttachmentInfo info, Player player, int closest, InteractionHand hand) {
        if (info instanceof ImmersiveHitboxesInfo hInfo) {
            if (closest == ImmersiveHitboxesInfo.BACKPACK_BACK_INDEX && hand == InteractionHand.OFF_HAND) {
                doBagOpen(player);
            }
        }

    }

    @Override
    public void processStorageFromNetwork(AbstractPlayerAttachmentInfo info, NullStorage storage) {
        // Intentional NO-OP
    }

    @Override
    public BlockPos getLightPos(ImmersiveHitboxesInfo info) {
        return info.getBlockPosition();
    }

    public void initImmersiveIfNeeded() {
        if (this.infos.isEmpty()) {
            this.infos.add(new ImmersiveHitboxesInfo());
        }
    }

    private void doBagOpen(Player player) {
        if (backpackCooldown <= 0) {
            VRRumble.rumbleIfVR(mc.player, 1, CommonConstants.vibrationTimePlayerActionAlert);
            ClientUtil.openBag(player);
            backpackCooldown = 50;
        }
    }
}
