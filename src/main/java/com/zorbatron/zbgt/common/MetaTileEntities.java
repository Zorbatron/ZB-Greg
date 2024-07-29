package com.zorbatron.zbgt.common;

import static com.zorbatron.zbgt.ZBUtility.zbgtId;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

import com.zorbatron.zbgt.common.metatileentities.multi.multiblockpart.*;
import com.zorbatron.zbgt.common.metatileentities.storage.*;

public class MetaTileEntities {

    public static MetaTileEntityCreativeEnergyHatch CREATIVE_ENERGY_SOURCE;
    public static MetaTileEntityCreativeEnergyHatch CREATIVE_ENERGY_SINK;
    public static MetaTileEntityCreativeReservoirHatch CREATIVE_RESERVOIR_HATCH;
    public static MetaTileEntityCreativeComputationProvider CREATIVE_COMPUTATION_PROVIDER;
    public static MetaTileEntityBudgetCRIB BUDGET_CRIB;

    public static int id = 18000;

    public static void init() {
        CREATIVE_ENERGY_SOURCE = registerMetaTileEntity(id,
                new MetaTileEntityCreativeEnergyHatch(zbgtId("creative_energy_source"), false)); // 18000
        CREATIVE_ENERGY_SINK = registerMetaTileEntity(++id,
                new MetaTileEntityCreativeEnergyHatch(zbgtId("creative_energy_sink"), true)); // 18001
        CREATIVE_RESERVOIR_HATCH = registerMetaTileEntity(++id,
                new MetaTileEntityCreativeReservoirHatch(zbgtId("creative_reservoir_hatch"))); // 18002
        CREATIVE_COMPUTATION_PROVIDER = registerMetaTileEntity(++id,
                new MetaTileEntityCreativeComputationProvider(zbgtId("creative_computation_provider"))); // 18003
        BUDGET_CRIB = registerMetaTileEntity(++id, new MetaTileEntityBudgetCRIB(zbgtId("budget_crib"))); // 18004
    }
}
