package svenhjol.charm.world.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import svenhjol.charm.world.entity.EndermitePowderEntity;
import svenhjol.meson.MesonItem;
import svenhjol.meson.MesonModule;
import svenhjol.meson.helper.WorldHelper;

public class EndermitePowderItem extends MesonItem
{
    public EndermitePowderItem(MesonModule module)
    {
        super(module, "endermite_powder", new Item.Properties()
            .group(ItemGroup.MISC));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn)
    {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (WorldHelper.getDimensionId(worldIn) != 1) {
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }

        if (!playerIn.isCreative()) {
            stack.shrink(1);
        }

        // client
        if (worldIn.isRemote) {
            playerIn.swingArm(handIn);
        }

        // server
        if (!worldIn.isRemote) {
            BlockPos pos = ((ServerWorld)worldIn).findNearestStructure("EndCity", playerIn.getPosition(), 1500, true);
            if (pos != null) {
                EndermitePowderEntity entity = new EndermitePowderEntity(worldIn, pos.getX(), pos.getZ());
                Vec3d look = playerIn.getLookVec();
                entity.setPosition(playerIn.getPosX() + look.x * 2, playerIn.getPosY() + 0.5, playerIn.getPosZ() + look.z * 2);
                worldIn.addEntity(entity);
                worldIn.playSound(null, playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ(), SoundEvents.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }

        /* @todo Use stats */

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }
}
