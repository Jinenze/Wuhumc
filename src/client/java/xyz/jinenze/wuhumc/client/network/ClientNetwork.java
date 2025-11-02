//package xyz.jinenze.wuhumc.client.network;
//
//import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//import xyz.jinenze.wuhumc.network.Packets;
//
//public class ClientNetwork {
//    public static void register() {
//        ClientPlayNetworking.registerGlobalReceiver(Packets.JumpYPowerSetRequest.ID, (client, handler, buf, responseSender) -> {
//            WuhumcClient.config.y = Packets.JumpYPowerSetRequest.read(buf);
//        });
//        ClientPlayNetworking.registerGlobalReceiver(Packets.JumpXZPowerSetRequest.ID, (client, handler, buf, responseSender) -> {
//            WuhumcClient.config.xz = Packets.JumpXZPowerSetRequest.read(buf);
//        });
//    }
//
//    public static void sendFireCallback() {
//        ClientPlayNetworking.getSender().sendPacket(Packets.FireCallback.ID, new Packets.FireCallback().write());
//    }
//
//    public static void sendParticlesRequest() {
//        ClientPlayNetworking.getSender().sendPacket(Packets.ParticlesRequest.ID, new Packets.ParticlesRequest().write());
//    }
//}
