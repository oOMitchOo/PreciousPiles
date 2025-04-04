package oomitchoo.preciouspiles.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import oomitchoo.preciouspiles.PreciousPiles;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class MyDataGenerator {

    /* Alles weggenommen, weil ich keine Anleitung finde, wie ich es für die neue NeoForge-Version realisiere. Solange ist MyItemTagsProvider.java ebenfalls nutzlos.
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();

        generator.addProvider(true) // fehlen weitere Argumente
    }
    */
}
