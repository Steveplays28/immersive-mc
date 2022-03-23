package net.blf02.immersivemc.client.immersive.info;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public abstract class AbstractImmersiveInfo<T extends TileEntity> {

    protected int ticksLeft;
    protected int countdown = 10; // Used for transitions for the items
    protected T tileEntity;
    protected int minSlot;
    protected int maxSlot;

    public AbstractImmersiveInfo(T tileEntity, int minSlot, int maxSlot, int ticksToExist) {
        this.tileEntity = tileEntity;
        this.minSlot = minSlot;
        this.maxSlot = maxSlot;
        this.ticksLeft = ticksToExist;
    }

    public abstract AxisAlignedBB getHibtox(int slot);

    public abstract AxisAlignedBB[] getAllHitboxes();

    public abstract void setHitbox(int slot, AxisAlignedBB hitbox);

    public abstract boolean hasHitboxes();

    public int getTicksLeft() {
        return this.ticksLeft;
    }

    public void changeTicksLeft(int amount) {
        this.ticksLeft += amount;
    }

    public void setTicksLeft(int value) {
        this.ticksLeft = value;
    }

    public int getCountdown() {
        return this.countdown;
    }

    public void changeCountdown(int amount) {
        this.countdown += amount;
    }

    public T getTileEntity() {
        return this.tileEntity;
    }
}
