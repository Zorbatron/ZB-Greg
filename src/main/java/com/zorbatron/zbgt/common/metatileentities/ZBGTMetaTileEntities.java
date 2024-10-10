package com.zorbatron.zbgt.common.metatileentities;

import static com.zorbatron.zbgt.api.util.ZBGTUtility.zbgtId;
import static gregtech.common.metatileentities.MetaTileEntities.registerMetaTileEntity;

import com.zorbatron.zbgt.common.metatileentities.multi.MetaTileEntityYOTTank;
import com.zorbatron.zbgt.common.metatileentities.multi.electric.MetaTileEntityCoAL;
import com.zorbatron.zbgt.common.metatileentities.multi.electric.large.*;
import com.zorbatron.zbgt.common.metatileentities.multi.electric.mega.*;
import com.zorbatron.zbgt.common.metatileentities.multi.electric.quad.*;
import com.zorbatron.zbgt.common.metatileentities.multi.multiblockpart.*;
import com.zorbatron.zbgt.common.metatileentities.storage.MetaTileEntityCreativeComputationProvider;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityLaserHatch;

public class ZBGTMetaTileEntities {

    public static MetaTileEntityCreativeEnergyHatch CREATIVE_ENERGY_SOURCE;
    public static MetaTileEntityCreativeEnergyHatch CREATIVE_ENERGY_SINK;
    public static MetaTileEntityCreativeFluidHatch CREATIVE_RESERVOIR_HATCH;
    public static MetaTileEntityCreativeComputationProvider CREATIVE_COMPUTATION_PROVIDER;
    public static MetaTileEntityAirIntakeHatch AIR_INTAKE_HATCH;
    public static MetaTileEntityAirIntakeHatch EXTREME_AIR_INTAKE_HATCH;
    public static MetaTileEntitySingleItemInputBus SINGLE_ITEM_INPUT_BUS;
    public static MetaTileEntityCreativeItemBus CREATIVE_ITEM_BUS;
    public static MetaTileEntitySuperInputBus SUPER_INPUT_BUS;
    public static MetaTileEntityLargeParallelHatch[] ZBGT_PARALLEL_HATCHES = new MetaTileEntityLargeParallelHatch[7];
    public static MetaTileEntityYOTTankMEHatch YOTTANK_ME_HATCH;

    public static MetaTileEntityLaserHatch[] LASER_INPUT_HATCH_16384 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_INPUT_HATCH_65536 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_INPUT_HATCH_262144 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_INPUT_HATCH_1048576 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_INPUT_HATCH_4194304 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_OUTPUT_HATCH_16384 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_OUTPUT_HATCH_65536 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_OUTPUT_HATCH_262144 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_OUTPUT_HATCH_1048576 = new MetaTileEntityLaserHatch[10]; // IV+
    public static MetaTileEntityLaserHatch[] LASER_OUTPUT_HATCH_4194304 = new MetaTileEntityLaserHatch[10]; // IV+

    public static MetaTileEntityMegaEBF MEGA_EBF;
    public static MetaTileEntityMegaLCR MEGA_LCR;
    public static MetaTileEntityMegaVF MEGA_VF;
    public static MetaTileEntityMegaOCU MEGA_OCU;
    public static MetaTileEntityMegaABS MEGA_ABS;
    public static MetaTileEntityPreciseAssembler PRASS;
    public static MetaTileEntityMegaFusionReactor[] MEGA_FUSION = new MetaTileEntityMegaFusionReactor[3];

    public static MetaTileEntityCoAL CoAL;

    public static MetaTileEntityQueebf QUAD_EBF;
    public static MetaTileEntityQueezer QUEEZER;
    public static MetaTileEntityQuacker QUACKER;

    public static MetaTileEntityYOTTank YOTTANK;

    public static MetaTileEntityLargeRockBreaker LARGE_ROCK_BREAKER;
    public static MetaTileEntityLargeAirCollector LARGE_AIR_COLLECTOR;
    public static MetaTileEntityLargeAlloySmelter LARGE_ALLOY_SMELTER;

    public static void init() {
        // 18000-18049 (50) reserved for my multiblock parts
        CREATIVE_ENERGY_SOURCE = registerMetaTileEntity(18000,
                new MetaTileEntityCreativeEnergyHatch(zbgtId("creative_energy_source"), false));
        CREATIVE_ENERGY_SINK = registerMetaTileEntity(18001,
                new MetaTileEntityCreativeEnergyHatch(zbgtId("creative_energy_sink"), true));

        CREATIVE_RESERVOIR_HATCH = registerMetaTileEntity(18002,
                new MetaTileEntityCreativeFluidHatch(zbgtId("creative_reservoir_hatch"), false));

        CREATIVE_COMPUTATION_PROVIDER = registerMetaTileEntity(18003,
                new MetaTileEntityCreativeComputationProvider(zbgtId("creative_computation_provider")));

        AIR_INTAKE_HATCH = registerMetaTileEntity(18004,
                new MetaTileEntityAirIntakeHatch(zbgtId("air_intake_hatch"), GTValues.IV, 128_000, 1000));
        EXTREME_AIR_INTAKE_HATCH = registerMetaTileEntity(18005,
                new MetaTileEntityAirIntakeHatch(zbgtId("extreme_air_intake_hatch"), GTValues.LuV, 256_000,
                        8000));

        SINGLE_ITEM_INPUT_BUS = registerMetaTileEntity(18006,
                new MetaTileEntitySingleItemInputBus(zbgtId("single_item_input_bus")));

        CREATIVE_ITEM_BUS = registerMetaTileEntity(18008,
                new MetaTileEntityCreativeItemBus(zbgtId("creative_item_bus")));

        SUPER_INPUT_BUS = registerMetaTileEntity(18009,
                new MetaTileEntitySuperInputBus(zbgtId("super_input_bus")));

        ZBGT_PARALLEL_HATCHES[0] = registerMetaTileEntity(18010, new MetaTileEntityLargeParallelHatch(
                zbgtId(String.format("parallel_hatch.%s", GTValues.VN[GTValues.UEV])), GTValues.UHV, 1024));
        ZBGT_PARALLEL_HATCHES[1] = registerMetaTileEntity(18011, new MetaTileEntityLargeParallelHatch(
                zbgtId(String.format("parallel_hatch.%s", GTValues.VN[GTValues.UIV])), GTValues.UEV, 4096));
        ZBGT_PARALLEL_HATCHES[2] = registerMetaTileEntity(18012, new MetaTileEntityLargeParallelHatch(
                zbgtId(String.format("parallel_hatch.%s", GTValues.VN[GTValues.UXV])), GTValues.UIV, 16_384));
        ZBGT_PARALLEL_HATCHES[3] = registerMetaTileEntity(18013, new MetaTileEntityLargeParallelHatch(
                zbgtId(String.format("parallel_hatch.%s", GTValues.VN[GTValues.OpV])), GTValues.UXV, 65_536));
        ZBGT_PARALLEL_HATCHES[4] = registerMetaTileEntity(18014, new MetaTileEntityLargeParallelHatch(
                zbgtId(String.format("parallel_hatch.%s", GTValues.VN[GTValues.MAX])), GTValues.OpV, 262_144));
        ZBGT_PARALLEL_HATCHES[5] = registerMetaTileEntity(18015, new MetaTileEntityLargeParallelHatch(
                zbgtId(String.format("parallel_hatch.%s", GTValues.VN[GTValues.MAX]) + ".1"), GTValues.MAX,
                1_048_576));
        ZBGT_PARALLEL_HATCHES[6] = registerMetaTileEntity(18016, new MetaTileEntityLargeParallelHatch(
                zbgtId("parallel_hatch.final"), GTValues.MAX, Integer.MAX_VALUE));

        YOTTANK_ME_HATCH = registerMetaTileEntity(18017,
                new MetaTileEntityYOTTankMEHatch(zbgtId("yottank_me_hatch"), GTValues.IV));

        // 18050-18099 (50) reserved for multiblocks
        MEGA_EBF = registerMetaTileEntity(18050,
                new MetaTileEntityMegaEBF(zbgtId("mega_ebf")));

        MEGA_LCR = registerMetaTileEntity(18051,
                new MetaTileEntityMegaLCR(zbgtId("mega_lcr")));

        MEGA_VF = registerMetaTileEntity(18052,
                new MetaTileEntityMegaVF(zbgtId("mega_vf")));

        MEGA_OCU = registerMetaTileEntity(18053,
                new MetaTileEntityMegaOCU(zbgtId("mega_ocu")));

        MEGA_ABS = registerMetaTileEntity(18054,
                new MetaTileEntityMegaABS(zbgtId("mega_abs")));

        PRASS = registerMetaTileEntity(18055,
                new MetaTileEntityPreciseAssembler(zbgtId("precise_assembler")));

        CoAL = registerMetaTileEntity(18056,
                new MetaTileEntityCoAL(zbgtId("coal")));

        QUAD_EBF = registerMetaTileEntity(18057,
                new MetaTileEntityQueebf(zbgtId("quad_ebf")));
        QUEEZER = registerMetaTileEntity(18058,
                new MetaTileEntityQueezer(zbgtId("queezer")));

        YOTTANK = registerMetaTileEntity(18059,
                new MetaTileEntityYOTTank(zbgtId("yottank")));

        MEGA_FUSION[0] = registerMetaTileEntity(18060,
                new MetaTileEntityMegaFusionReactor(zbgtId("mega_fusion_1"), GTValues.LuV));
        MEGA_FUSION[1] = registerMetaTileEntity(18061,
                new MetaTileEntityMegaFusionReactor(zbgtId("mega_fusion_2"), GTValues.ZPM));
        MEGA_FUSION[2] = registerMetaTileEntity(18062,
                new MetaTileEntityMegaFusionReactor(zbgtId("mega_fusion_3"), GTValues.UV));
        // Reserve 18063 and 18064 for the mega fusion 4 and 5 if I ever do them

        LARGE_ROCK_BREAKER = registerMetaTileEntity(18065,
                new MetaTileEntityLargeRockBreaker(zbgtId("large_rock_breaker")));
        LARGE_AIR_COLLECTOR = registerMetaTileEntity(18066,
                new MetaTileEntityLargeAirCollector(zbgtId("large_air_collector")));
        LARGE_ALLOY_SMELTER = registerMetaTileEntity(18068,
                new MetaTileEntityLargeAlloySmelter(zbgtId("large_alloy_smelter")));

        QUACKER = registerMetaTileEntity(18069,
                new MetaTileEntityQuacker(zbgtId("quacker")));

        // Holy laser hatches batman
        int startingId = 18100;
        int endPos = GregTechAPI.isHighTier() ? LASER_INPUT_HATCH_16384.length - 1 :
                Math.min(LASER_INPUT_HATCH_16384.length - 1, GTValues.UHV - GTValues.IV);
        for (int i = 0; i < endPos; i++) {
            int v = i + GTValues.IV;
            String voltageName = GTValues.VN[v].toLowerCase();
            LASER_INPUT_HATCH_16384[i] = registerMetaTileEntity(startingId + i,
                    new MetaTileEntityLaserHatch(zbgtId("laser_hatch.target_16384a." + voltageName), false, v, 16_384));
            LASER_OUTPUT_HATCH_16384[i] = registerMetaTileEntity(startingId + 9 + i,
                    new MetaTileEntityLaserHatch(zbgtId("laser_hatch.source_16384a." + voltageName), true, v, 16_384));

            LASER_INPUT_HATCH_65536[i] = registerMetaTileEntity(startingId + 9 * 2 + i,
                    new MetaTileEntityLaserHatch(zbgtId("laser_hatch.source_65536a." + voltageName), false, v, 65_536));
            LASER_OUTPUT_HATCH_65536[i] = registerMetaTileEntity(startingId + 9 * 3 + i,
                    new MetaTileEntityLaserHatch(zbgtId("laser_hatch.target_65536a." + voltageName), true, v, 65_536));

            LASER_INPUT_HATCH_262144[i] = registerMetaTileEntity(startingId + 9 * 4 + i,
                    new MetaTileEntityLaserHatch(zbgtId("laser_hatch.target_262144a." + voltageName), false, v,
                            262_144));
            LASER_OUTPUT_HATCH_262144[i] = registerMetaTileEntity(startingId + 9 * 5 + i,
                    new MetaTileEntityLaserHatch(zbgtId("laser_hatch.source_262144a." + voltageName), true, v,
                            262_144));

            LASER_INPUT_HATCH_1048576[i] = registerMetaTileEntity(startingId + 9 * 6 + i,
                    new MetaTileEntityLaserHatch(zbgtId("laser_hatch.target_1048576a." + voltageName), false, v,
                            1_048_576));
            LASER_OUTPUT_HATCH_1048576[i] = registerMetaTileEntity(startingId + 9 * 7 + i,
                    new MetaTileEntityLaserHatch(zbgtId("laser_hatch.target_1048576a." + voltageName), true, v,
                            1_048_576));

            LASER_INPUT_HATCH_4194304[i] = registerMetaTileEntity(startingId + 9 * 8 + i,
                    new MetaTileEntityLaserHatch(zbgtId("laser_hatch.target_4194304a." + voltageName), false, v,
                            4_194_304));
            LASER_OUTPUT_HATCH_4194304[i] = registerMetaTileEntity(startingId + 9 * 9 + i,
                    new MetaTileEntityLaserHatch(zbgtId("laser_hatch.source_4194304a." + voltageName), true, v,
                            4_194_304));
        }
    }
}
