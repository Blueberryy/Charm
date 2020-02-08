package svenhjol.charm.world.module;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStage.Decoration;
import net.minecraft.world.gen.blockplacer.SimpleBlockPlacer;
import net.minecraft.world.gen.blockstateprovider.SimpleBlockStateProvider;
import net.minecraft.world.gen.feature.BlockClusterFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmCategories;
import svenhjol.charm.world.block.FumaroleBlock;
import svenhjol.charm.world.gen.feature.FumaroleFeature;
import svenhjol.charm.world.gen.placement.FumarolePlacement;
import svenhjol.meson.MesonModule;
import svenhjol.meson.handler.RegistryHandler;
import svenhjol.meson.iface.Module;

import static net.minecraft.block.Blocks.NETHERRACK;

@Module(mod = Charm.MOD_ID, category = CharmCategories.WORLD,
    description = "Fumaroles are small columns of hot steam rising from the nether floor.\n" +
        "Sometimes they erupt, sending entities that are placed on them high into the air.")
public class Fumaroles extends MesonModule
{
    public static FumaroleBlock block;
    public static Feature<NoFeatureConfig> feature = null;
    public static Placement<FrequencyConfig> placement = null;
    public static BlockClusterFeatureConfig config = null;

    @Override
    public void init()
    {
        block = new FumaroleBlock(this);
        BlockState state = block.getDefaultState();

        feature = new FumaroleFeature(NoFeatureConfig::deserialize);
        placement = new FumarolePlacement(FrequencyConfig::deserialize);
        config = (new BlockClusterFeatureConfig.Builder(
            new SimpleBlockStateProvider(state),
            new SimpleBlockPlacer())).func_227315_a_(64).func_227316_a_(ImmutableSet.of(NETHERRACK.getBlock())).func_227317_b_().func_227322_d_();

        ResourceLocation ID = new ResourceLocation(Charm.MOD_ID, "fumarole");
        RegistryHandler.registerFeature(feature, placement, ID);
    }

    @Override
    public void setup(FMLCommonSetupEvent event)
    {
        Biomes.NETHER.addFeature(Decoration.UNDERGROUND_DECORATION,
            Feature.RANDOM_PATCH.withConfiguration(config)
                .withPlacement(placement.func_227446_a_(new FrequencyConfig(8))));
    }
}
