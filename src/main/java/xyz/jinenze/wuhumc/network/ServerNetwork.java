//package xyz.jinenze.wuhumc.network;
//
//import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//import net.minecraft.particle.ParticleTypes;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.sound.SoundCategory;
//import net.minecraft.sound.SoundEvents;
//
//public class ServerNetwork {
//    public static void register() {
//        ServerPlayNetworking.registerGlobalReceiver(Packets.FireCallback.ID, (server, player, handler, buf, responseSender) -> {
//            player.playSound(SoundEvents.ENTITY_BAT_TAKEOFF, SoundCategory.PLAYERS, 0.4f, 0.5f);
//        });
//        ServerPlayNetworking.registerGlobalReceiver(Packets.ParticlesRequest.ID, (server, player, handler, buf, responseSender) -> {
//            player.getServerWorld().spawnParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1, player.getZ(), 100, 0.2d, 0.5d, 0.2d, 0);
//        });
//    }
//
//    public static void sendJumpYPowerSetRequest(ServerPlayer player, Double power) {
//        ServerPlayNetworking.getSender(player).sendPacket(Packets.JumpYPowerSetRequest.ID, new Packets.JumpYPowerSetRequest(power).write());
//    }
//
//    public static void sendJumpXZPowerSetRequest(ServerPlayer player, Double power) {
//        ServerPlayNetworking.getSender(player).sendPacket(Packets.JumpXZPowerSetRequest.ID, new Packets.JumpXZPowerSetRequest(power).write());
//    }
//}
