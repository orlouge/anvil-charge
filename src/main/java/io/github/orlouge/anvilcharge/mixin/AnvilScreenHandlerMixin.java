package io.github.orlouge.anvilcharge.mixin;

import io.github.orlouge.anvilcharge.AnvilChargeMod;
import io.github.orlouge.anvilcharge.XpToEnergyUtil;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.tag.BlockTags;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    @Shadow @Final private Property levelCost;
    @Shadow private String newItemName;
    private Integer anvilChargeXpCost = null;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(method = "updateResult()V", at = @At("HEAD"), cancellable = true)
    public void onUpdateResult(CallbackInfo ci) {
        this.anvilChargeXpCost = null;

        ItemStack inputStack1 = this.input.getStack(0);
        ItemStack inputStack2 = this.input.getStack(1);
        if (
                XpToEnergyUtil.isBattery(inputStack1) &&
                inputStack2.isEmpty() &&
                this.player.totalExperience > 0 &&
                this.newItemName.isBlank()
        ) {
            long energy = XpToEnergyUtil.getChargeAmount(inputStack1, this.player.totalExperience);
            if (energy > 0) {
                this.anvilChargeXpCost = XpToEnergyUtil.getXpCost(energy);
                this.levelCost.set(Math.max(1, XpToEnergyUtil.getLevelCost(this.anvilChargeXpCost, this.player.experienceLevel, this.player.totalExperience)));
                ItemStack outputStack = inputStack1.copy();
                XpToEnergyUtil.addEnergy(outputStack, energy);
                this.output.setStack(0, outputStack);
                this.sendContentUpdates();
            }
        }

        if (this.anvilChargeXpCost != null) {
            ci.cancel();
        }
    }

    @Inject(method = "onTakeOutput(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"), cancellable = true)
    public void onOnTakeOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (this.anvilChargeXpCost != null) {
            if (!player.getAbilities().creativeMode) {
                player.addExperience(- this.anvilChargeXpCost);
                player.addScore(this.anvilChargeXpCost);
            }

            this.input.setStack(0, ItemStack.EMPTY);
            this.input.setStack(1, ItemStack.EMPTY);
            this.levelCost.set(0);

            if (AnvilChargeMod.CONSUME_ANVIL) {
                this.context.run((world, pos) -> {
                    BlockState blockState = world.getBlockState(pos);
                    if (!player.getAbilities().creativeMode && blockState.isIn(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12F) {
                        BlockState blockState2 = AnvilBlock.getLandingState(blockState);
                        if (blockState2 == null) {
                            world.removeBlock(pos, false);
                            world.syncWorldEvent(1029, pos, 0);
                        } else {
                            world.setBlockState(pos, blockState2, 2);
                            world.syncWorldEvent(1030, pos, 0);
                        }
                    } else {
                        world.syncWorldEvent(1030, pos, 0);
                    }

                });
            }

            ci.cancel();
        }
    }

    @Inject(method = "setNewItemName(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    public void onSetNewItemName(String newItemName, CallbackInfo ci) {
        if (anvilChargeXpCost != null) ci.cancel();
    }
}
