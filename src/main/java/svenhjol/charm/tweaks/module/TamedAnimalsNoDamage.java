package svenhjol.charm.tweaks.module;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import svenhjol.charm.Charm;
import svenhjol.charm.base.CharmCategories;
import svenhjol.meson.MesonModule;
import svenhjol.meson.iface.Module;

@Module(mod = Charm.MOD_ID, category = CharmCategories.TWEAKS, hasSubscriptions = true,
    description = "Tamed animals no longer take direct damage from players.\n" +
        "They still suffer secondary effects, such as burning from a Fire Aspect sword.")
public class TamedAnimalsNoDamage extends MesonModule
{
    @SubscribeEvent
    public void onDamage(LivingHurtEvent event)
    {
        if (!event.isCanceled()
            && (!(event.getEntityLiving() instanceof PlayerEntity))
        ) {
            Entity attacker = event.getSource().getImmediateSource();
            Entity source = event.getSource().getTrueSource();

            if (source instanceof PlayerEntity || attacker instanceof PlayerEntity) {
                LivingEntity target = event.getEntityLiving();
                if (target instanceof TameableEntity
                    && ((TameableEntity)target).isTamed()
                ) {
                    event.setCanceled(true);
                }
            }
        }
    }
}