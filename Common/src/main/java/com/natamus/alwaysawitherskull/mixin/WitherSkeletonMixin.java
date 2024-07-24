package com.natamus.alwaysawitherskull.mixin;

import com.natamus.collective.functions.TaskFunctions;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WitherSkeleton.class, priority = 1001)
public class WitherSkeletonMixin {
	@Inject(method = "dropCustomDeathLoot(Lnet/minecraft/world/damagesource/DamageSource;IZ)V", at = @At(value = "HEAD"))
	protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean b, CallbackInfo ci) {
		WitherSkeleton witherSkeleton = (WitherSkeleton)(Object)this;
		Level level = witherSkeleton.level();
		if (level.isClientSide) {
			return;
		}

		Vec3 witherVec = witherSkeleton.position();

		TaskFunctions.enqueueCollectiveTask(level.getServer(), () -> {
			boolean foundSkull = false;

			for (Entity entity : level.getEntities(null, new AABB(witherVec.x-1, witherVec.y-1, witherVec.z-1, witherVec.x+1, witherVec.y+1, witherVec.z+1))) {
				if (entity instanceof ItemEntity itemEntity) {
					if (itemEntity.getItem().getItem().equals(Items.WITHER_SKELETON_SKULL)) {
						if (itemEntity.getAge() < 10) {
							foundSkull = true;
							break;
						}
					}
				}
			}

			if (!foundSkull) {
				level.addFreshEntity(new ItemEntity(level, witherVec.x, witherVec.y, witherVec.z, new ItemStack(Items.WITHER_SKELETON_SKULL, 1)));
			}
		}, 1);
	}
}