package xyz.jinenze.wuhumc.network;

import com.google.gson.reflect.TypeToken;
import com.ibm.icu.impl.Pair;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.config.ServerConfig;

import java.util.Map;

public class Payloads {
    public record ServerConfigC2SPayload(ServerConfig config) implements CustomPacketPayload {
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Wuhumc.MOD_ID, "send_server_config");
        public static final CustomPacketPayload.Type<ServerConfigC2SPayload> TYPE = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerConfigC2SPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ServerConfigC2SPayload::getJson, ServerConfigC2SPayload::getInstance);

        public static ServerConfigC2SPayload getInstance(String json) {
            return new ServerConfigC2SPayload(Wuhumc.gson.fromJson(json, ServerConfig.class));
        }

        public static String getJson(ServerConfigC2SPayload payload) {
            return Wuhumc.gson.toJson(payload.config);
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record ShowScoreBoardS2CPayload(Map<String, Pair<Integer, Integer>> scores) implements CustomPacketPayload {
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Wuhumc.MOD_ID, "show_scoreboard");
        public static final CustomPacketPayload.Type<ShowScoreBoardS2CPayload> TYPE = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, ShowScoreBoardS2CPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ShowScoreBoardS2CPayload::getJson, ShowScoreBoardS2CPayload::getInstance);
        private static final java.lang.reflect.Type jsonType = new TypeToken<Map<String, Pair<Integer, Integer>>>() {
        }.getType();

        public static ShowScoreBoardS2CPayload getInstance(String json) {
            return new ShowScoreBoardS2CPayload(Wuhumc.gson.fromJson(json, jsonType));
        }

        public static String getJson(ShowScoreBoardS2CPayload payload) {
            return Wuhumc.gson.toJson(payload.scores);
        }

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private Payloads() {
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ServerConfigC2SPayload.TYPE, ServerConfigC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShowScoreBoardS2CPayload.TYPE, ShowScoreBoardS2CPayload.CODEC);
    }
}
