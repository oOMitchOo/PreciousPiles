package oomitchoo.preciouspiles.item;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import oomitchoo.preciouspiles.PreciousPiles;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PreciousPiles.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
