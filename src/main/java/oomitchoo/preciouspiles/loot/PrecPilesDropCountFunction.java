package oomitchoo.preciouspiles.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import oomitchoo.preciouspiles.PreciousPiles;
import oomitchoo.preciouspiles.block.StackedIngotsBlock;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;

public class PrecPilesDropCountFunction implements LootItemFunction {

    public static final MapCodec<PrecPilesDropCountFunction> CODEC =
            RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Codec.BOOL.optionalFieldOf("dummy", false)
                            .forGetter(f -> false)
            ).apply(inst, (ignored) -> new PrecPilesDropCountFunction()));


    @Override
    public LootItemFunctionType<? extends LootItemFunction> getType() {
        return PreciousPiles.PREC_PILES_LOOT_FCT.get();
    }

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        int itemCount = 0;
        try {
            BlockState blockState = lootContext.getParameter(LootContextParams.BLOCK_STATE);
            if (blockState.getBlock() instanceof StackedIngotsBlock) {
                if (blockState.getValue(StackedIngotsBlock.DOWN_FILLED)) itemCount = 32;
                if (blockState.getValue(StackedIngotsBlock.INGOTS_AT_NW)) itemCount = itemCount + 8;
                if (blockState.getValue(StackedIngotsBlock.INGOTS_AT_NE)) itemCount = itemCount + 8;
                if (blockState.getValue(StackedIngotsBlock.INGOTS_AT_SE)) itemCount = itemCount + 8;
                if (blockState.getValue(StackedIngotsBlock.INGOTS_AT_SW)) itemCount = itemCount + 8;

                itemStack.setCount(itemCount);
                return itemStack;
            } else {
                PreciousPiles.LOGGER.error("A block loot_table tried to call a function, which is only for PreciousPiles Blocks.");
            }
        } catch (CommandLine.MissingParameterException e) {
            PreciousPiles.LOGGER.error("A loot_table tried to call a function, which is only for PreciousPiles Blocks.");
            return itemStack;
        }
        return itemStack;
    }
}
