//package xyz.jinenze.wuhumc.network;
//
//import com.jez.wuhumc.Wuhumc;
//import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
//import net.minecraft.network.PacketByteBuf;
//import net.minecraft.util.Identifier;
//
//public class Packets {
//    public static final class FireCallback {
//        public static final Identifier ID = new Identifier(Wuhumc.MOD_ID, "fire_callback");
//
//        public PacketByteBuf write() {
//            return PacketByteBufs.empty();
//        }
//    }
//
//    public static final class ParticlesRequest {
//        public static final Identifier ID = new Identifier(Wuhumc.MOD_ID, "particles_request");
//
//        public PacketByteBuf write() {
//            return PacketByteBufs.empty();
//        }
//    }
//
//    public record JumpYPowerSetRequest(double power) {
//        public static final Identifier ID = new Identifier(Wuhumc.MOD_ID, "y_power_set");
//
//        public PacketByteBuf write() {
//            return new PacketByteBuf(PacketByteBufs.create().writeDouble(power));
//        }
//
//        public static double read(PacketByteBuf buf) {
//            return buf.readDouble();
//        }
//    }
//
//    public record JumpXZPowerSetRequest(double power) {
//        public static final Identifier ID = new Identifier(Wuhumc.MOD_ID, "xz_power_set");
//
//        public PacketByteBuf write() {
//            return new PacketByteBuf(PacketByteBufs.create().writeDouble(power));
//        }
//
//        public static double read(PacketByteBuf buf) {
//            return buf.readDouble();
//        }
//    }
//}
