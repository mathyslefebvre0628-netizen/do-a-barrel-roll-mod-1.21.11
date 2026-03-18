package com.rclphantom.barrelroll.client;

import com.rclphantom.barrelroll.BarrelRollMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = BarrelRollMod.MOD_ID, value = Dist.CLIENT)
public class ElytraFlightHandler {

    public static float currentRoll = 0f;

    private static final float YAW_SPEED   = 3.0f;
    private static final float BANK_FACTOR = 1.5f;
    private static final float ROLL_DECAY  = 0.88f;
    private static final float MAX_ROLL    = 180f;

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (isFlying()) {
            event.setRoll(currentRoll);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != player) return;

        if (!player.isFallFlying()) {
            currentRoll *= 0.75f;
            if (Math.abs(currentRoll) < 0.05f) currentRoll = 0f;
            return;
        }

        Options options = mc.options;
        boolean leftDown  = options.keyLeft.isDown();
        boolean rightDown = options.keyRight.isDown();

        if (leftDown && !rightDown) {
            player.setYRot(player.getYRot() - YAW_SPEED);
            currentRoll -= BANK_FACTOR;
        } else if (rightDown && !leftDown) {
            player.setYRot(player.getYRot() + YAW_SPEED);
            currentRoll += BANK_FACTOR;
        } else {
            currentRoll *= ROLL_DECAY;
            if (Math.abs(currentRoll) < 0.05f) currentRoll = 0f;
        }

        currentRoll = Mth.clamp(currentRoll, -MAX_ROLL, MAX_ROLL);
    }

    public static void handleMouseInput(LocalPlayer player, double rawYaw, double rawPitch) {
        currentRoll += (float) rawYaw;
        currentRoll = Mth.clamp(currentRoll, -MAX_ROLL, MAX_ROLL);

        float rollRad = (float) Math.toRadians(currentRoll);
        double worldPitch   = rawPitch * Math.cos(rollRad);
        double yawFromPitch = rawPitch * Math.sin(rollRad);

        player.turn(yawFromPitch, worldPitch);
    }

    public static boolean isFlying() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.isFallFlying();
    }
}
