package com.zorbatron.zbgt.common.metatileentities.storage;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_ACTIVE;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_IO_SPEED;

import java.util.Collection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IOpticalComputationProvider;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.texture.Textures;

public class MetaTileEntityCreativeComputationProvider extends MetaTileEntity
                                                       implements IOpticalComputationProvider, IControllable {

    private boolean isWorkingEnabled = true;
    private int maxCWUt = 0;

    private int lastRequestedCWUt;
    private int requestedCWUPerSec;

    public MetaTileEntityCreativeComputationProvider(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCreativeComputationProvider(metaTileEntityId);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 170, 95);
        builder.label(7, 7, getMetaFullName());

        builder.widget(new LabelWidget(7, 25, "zbgt.machine.creative_computation_provider.cwut"));
        builder.widget(new ImageWidget(7, 34, 126, 20, GuiTextures.DISPLAY));
        builder.widget(new TextFieldWidget2(9, 40, 122, 16, () -> String.valueOf(this.maxCWUt),
                value -> {
                    maxCWUt = Integer.parseInt(value);
                    markDirty();
                }).setNumbersOnly(0, Integer.MAX_VALUE).setMaxLength(11));

        builder.widget(new ImageCycleButtonWidget(140, 35, 18, 18, GuiTextures.BUTTON_POWER, this::isWorkingEnabled,
                this::setWorkingEnabled));

        builder.widget(new LabelWidget(7, 62, "zbgt.machine.creative_computation_provider.average"));
        builder.widget(new DynamicLabelWidget(7, 74, () -> String.valueOf(this.lastRequestedCWUt)));

        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void update() {
        super.update();

        if (getWorld().isRemote) return;
        if (getOffsetTimer() % 20 == 0) {
            this.lastRequestedCWUt = requestedCWUPerSec / 20;
            this.requestedCWUPerSec = 0;
            this.writeCustomData(UPDATE_IO_SPEED, packetBuffer -> packetBuffer.writeInt(lastRequestedCWUt));
        }
    }

    @Override
    public int requestCWUt(int CWUt, boolean simulate, @NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        int requestedCWUt = isWorkingEnabled ? Math.min(CWUt, maxCWUt) : 0;
        if (!simulate) {
            this.requestedCWUPerSec += requestedCWUt;
        }
        return requestedCWUt;
    }

    @Override
    public int getMaxCWUt(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return isWorkingEnabled ? maxCWUt : 0;
    }

    @Override
    public boolean canBridge(@NotNull Collection<IOpticalComputationProvider> seen) {
        seen.add(this);
        return true;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (side == getFrontFacing() && capability == GregtechTileCapabilities.CABABILITY_COMPUTATION_PROVIDER) {
            return GregtechTileCapabilities.CABABILITY_COMPUTATION_PROVIDER.cast(this);
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        IVertexOperation[] renderPipeline = ArrayUtils.add(pipeline,
                new ColourMultiplier(GTUtility.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        Textures.VOLTAGE_CASINGS[GTValues.MAX].render(renderState, translation, renderPipeline);
        Textures.OPTICAL_DATA_ACCESS_HATCH.renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public boolean isWorkingEnabled() {
        return this.isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        this.isWorkingEnabled = isWorkingAllowed;
        if (!getWorld().isRemote) {
            writeCustomData(GregtechDataCodes.UPDATE_ACTIVE, buf -> buf.writeBoolean(isWorkingAllowed));
        }
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_ACTIVE) {
            this.isWorkingEnabled = buf.readBoolean();
        } else if (dataId == UPDATE_IO_SPEED) {
            this.lastRequestedCWUt = buf.readInt();
        }
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isWorkingEnabled);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isWorkingEnabled = buf.readBoolean();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("MaxCWUt", this.maxCWUt);
        data.setBoolean("IsWorkingEnabled", this.isWorkingEnabled);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        this.maxCWUt = data.getInteger("MaxCWUt");
        this.isWorkingEnabled = data.getBoolean("IsWorkingEnabled");
        super.readFromNBT(data);
    }
}
