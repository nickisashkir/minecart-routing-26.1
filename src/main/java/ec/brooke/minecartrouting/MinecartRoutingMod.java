package ec.brooke.minecartrouting;

import ec.brooke.minecartrouting.feature.Assigner;
import ec.brooke.minecartrouting.store.FilterAttachment;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinecartRoutingMod implements ModInitializer {
    public static final String MOD_ID = "minecart_routing";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        FilterAttachment.register();
        Assigner.register();
        LOGGER.info("Minecart Routing initialized");
    }
}
