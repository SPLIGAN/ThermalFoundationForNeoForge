package cofh.thermal.foundation.common.event;

import cofh.thermal.core.common.config.ThermalCoreConfig;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

import static cofh.core.util.helpers.ItemHelper.cloneStack;
import static cofh.thermal.core.ThermalCore.ITEMS;
import static cofh.thermal.foundation.init.registries.TFndIDs.ID_RUBBERWOOD_SAPLING;

public class TFndCommonSetupEvents {

    //    @SubscribeEvent
    //    public static void setupVillagerTrades(final VillagerTradesEvent event) {
    //
    //        if (!ThermalConfig.enableVillagerTrades.get()) {
    //            return;
    //        }
    //    }

    @SubscribeEvent
    public static void setupWandererTrades(final WandererTradesEvent event) {

        if (!ThermalCoreConfig.enableWandererTrades.get()) {
            return;
        }
        event.getRareTrades().add(new BasicItemListing(cloneStack(Items.EMERALD, 8), cloneStack(ITEMS.get(ID_RUBBERWOOD_SAPLING)), 8, 1, 0.05F));
    }

}
