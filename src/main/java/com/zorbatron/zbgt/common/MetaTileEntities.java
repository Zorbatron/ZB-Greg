package com.zorbatron.zbgt.common;

import static com.zorbatron.zbgt.ZBUtility.zbgtId;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

import com.zorbatron.zbgt.common.metatileentities.multi.multiblockpart.MetaTileEntityCreativeEnergyHatch;
import com.zorbatron.zbgt.common.metatileentities.multi.multiblockpart.MetaTileEntityCreativeReservoirHatch;

public class MetaTileEntities {

    public static MetaTileEntityCreativeEnergyHatch CREATIVE_ENERGY_HATCH;
    public static MetaTileEntityCreativeReservoirHatch CREATIVE_RESERVOIR_HATCH;

    public static int id = 18000;

    public static void init() {
        CREATIVE_ENERGY_HATCH = registerMetaTileEntity(id,
                new MetaTileEntityCreativeEnergyHatch(zbgtId("creative_energy_hatch"))); // 18000
        CREATIVE_RESERVOIR_HATCH = registerMetaTileEntity(++id,
                new MetaTileEntityCreativeReservoirHatch(zbgtId("creative_reservoir_hatch"))); // 18001
    }
}
