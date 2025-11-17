package xyz.jinenze.wuhumc.network;

import com.google.gson.Gson;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.config.ServerConfig;

public class Payloads {
    public record ServerConfigC2SPayload(ServerConfig config) implements CustomPacketPayload {
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Wuhumc.MOD_ID, "send_server_config");
        public static final CustomPacketPayload.Type<ServerConfigC2SPayload> TYPE = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerConfigC2SPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ServerConfigC2SPayload::getJson, ServerConfigC2SPayload::getInstance);

        public static ServerConfigC2SPayload getInstance(String json) {
            return new ServerConfigC2SPayload(new Gson().fromJson(json, ServerConfig.class));
        }

        public static String getJson(ServerConfigC2SPayload payload) {
            return Wuhumc.gson.toJson(payload.config());
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ServerConfigC2SPayload.TYPE, ServerConfigC2SPayload.CODEC);
    }
}
