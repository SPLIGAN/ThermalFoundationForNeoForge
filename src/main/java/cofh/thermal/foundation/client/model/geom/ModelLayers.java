package cofh.thermal.foundation.client.model.geom;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ModelLayers {

    private ModelLayers() {

    }

    public static final ModelLayerLocation RUBBERWOOD_BOAT_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thermal", "boat/rubberwood"), "main");
    public static final ModelLayerLocation RUBBERWOOD_CHEST_BOAT_LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("thermal", "chest_boat/rubberwood"), "main");

}