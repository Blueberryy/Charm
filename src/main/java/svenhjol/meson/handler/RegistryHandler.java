package svenhjol.meson.handler;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.particles.ParticleType;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import svenhjol.meson.Meson;
import svenhjol.meson.iface.IMesonBlock;

import java.util.*;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD;

@Mod.EventBusSubscriber(bus = MOD)
public class RegistryHandler
{
    public static Map<Class<?>, List<IForgeRegistryEntry<?>>> objects = new HashMap();

    public static void registerBlock(Block block, ResourceLocation res)
    {
        addRegisterable(block, res);

        // also register the block item
        if (block instanceof IMesonBlock) {
            addRegisterable(((IMesonBlock)block).getBlockItem(), res);
        }
    }

    public static void registerBrewingRecipe(Potion input, Item reagant, Potion output)
    {
        PotionBrewing.addMix(input, reagant, output);
    }

    public static void registerContainer(ContainerType<?> container, ResourceLocation res)
    {
        addRegisterable(container, res);
    }

    public static void registerEffect(Effect effect, ResourceLocation res)
    {
        addRegisterable(effect, res);
    }

    public static void registerEnchantment(Enchantment enchantment, ResourceLocation res)
    {
        addRegisterable(enchantment, res);
    }

    public static void registerEntity(EntityType<?> entity, ResourceLocation res)
    {
        addRegisterable(entity, res);
    }

    public static void registerFeature(Feature<?> feature, ResourceLocation res)
    {
        addRegisterable(feature, res);
//        Registry.register(Registry.FEATURE, res.toString(), feature);
    }

    public static void registerFeature(Feature<?> feature, Placement<?> placement, ResourceLocation res)
    {
        registerFeature(feature, res);
        addRegisterable(placement, res);
//        Registry.register(Registry.DECORATOR, res.toString(), placement);
    }

    public static void registerItem(Item item, ResourceLocation res)
    {
        addRegisterable(item, res);
    }

    public static void registerParticleType(ParticleType<?> type, ResourceLocation res)
    {
        addRegisterable(type, res);
    }

    public static void registerPotion(Potion potion, ResourceLocation res)
    {
        addRegisterable(potion, res);
    }

    public static void registerSound(SoundEvent sound)
    {
        addRegisterable(sound, sound.getRegistryName());
    }

    public static void registerStructure(Structure<?> structure, ResourceLocation res)
    {
//        Registry.register(Registry.FEATURE, res, structure);
//        GameData.getStructureMap().put(res.getPath().toLowerCase(Locale.ROOT), structure);
        addRegisterable(structure, res);
    }

    public static void registerStructurePiece(IStructurePieceType piece, ResourceLocation res)
    {
        Registry.register(Registry.STRUCTURE_PIECE, res, piece);
//        addRegisterable(piece, res);
    }

    public static void registerTile(TileEntityType<?> tile, ResourceLocation res)
    {
        addRegisterable(tile, res);
    }

    public static void registerVillager(VillagerProfession profession, ResourceLocation res)
    {
        addRegisterable(profession, res);
    }

    public static void registerVillagerPointOfInterest(PointOfInterestType type, ResourceLocation res)
    {
        addRegisterable(type, res);
        PointOfInterestType.func_221052_a(type);
    }

    @SubscribeEvent
    public static void onRegister(final Register event)
    {
        IForgeRegistry registry = event.getRegistry();
        Class<?> type = registry.getRegistrySuperType();

        if (objects.containsKey(type)) {
            objects.get(type).forEach(e -> {
                Meson.debug("Registering entry " + e.getRegistryName());
                registry.register(e);
            });
            objects.remove(type);
        }
    }

    public static void addRegisterable(IForgeRegistryEntry<?> obj, ResourceLocation res)
    {
        Class<?> type = obj.getRegistryType();
        if (!objects.containsKey(type)) objects.put(type, new ArrayList<>());

        if (!(objects.containsKey(type) && objects.get(type).contains(obj))) {
            if (res == null && obj.getRegistryName() == null) {
                Meson.debug("Not registring object because registry name is empty", obj);
                return; // can't set it
            }

            if (obj.getRegistryName() == null && res != null) {
                obj.setRegistryName(GameData.checkPrefix(res.toString(), false));
            }
            objects.get(type).add(obj);
            Meson.debug("Registered object " + obj.getRegistryName() + " with type " + type);

        } else {
            Meson.debug("Object already registered", obj);
        }
    }
}
