package xyz.jinenze.wuhumc.network;

import com.google.gson.reflect.TypeToken;
import com.ibm.icu.impl.Pair;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import xyz.jinenze.wuhumc.Wuhumc;
import xyz.jinenze.wuhumc.config.ServerConfig;

import java.util.Map;

public class Payloads {
    public record ServerConfigC2SPayload(ServerConfig config) implements CustomPacketPayload {
        public static final Identifier ID = Identifier.fromNamespaceAndPath(Wuhumc.MOD_ID, "client_to_server_server_config");
        public static final Type<ServerConfigC2SPayload> TYPE = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerConfigC2SPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ServerConfigC2SPayload::getJson, ServerConfigC2SPayload::getInstance);

        public static ServerConfigC2SPayload getInstance(String json) {
            return new ServerConfigC2SPayload(Wuhumc.gson.fromJson(json, ServerConfig.class));
        }

        public static String getJson(ServerConfigC2SPayload payload) {
            return Wuhumc.gson.toJson(payload.config);
        }

        @Override
        public Type<ServerConfigC2SPayload> type() {
            return TYPE;
        }
    }

    public record ServerConfigS2CPayload(ServerConfig config) implements CustomPacketPayload {
        public static final Identifier ID = Identifier.fromNamespaceAndPath(Wuhumc.MOD_ID, "server_to_client_server_config");
        public static final Type<ServerConfigS2CPayload> TYPE = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, ServerConfigS2CPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, ServerConfigS2CPayload::getJson, ServerConfigS2CPayload::getInstance);

        public static ServerConfigS2CPayload getInstance(String json) {
            return new ServerConfigS2CPayload(Wuhumc.gson.fromJson(json, ServerConfig.class));
        }

        public static String getJson(ServerConfigS2CPayload payload) {
            return Wuhumc.gson.toJson(payload.config);
        }


        @Override
        public Type<ServerConfigS2CPayload> type() {
            return TYPE;
        }
    }

    public record ShowScoreBoardS2CPayload(Map<String, Pair<Integer, Integer>> scores) implements CustomPacketPayload {
        public static final Identifier ID = Identifier.fromNamespaceAndPath(Wuhumc.MOD_ID, "show_scoreboard");
        public static final Type<ShowScoreBoardS2CPayload> TYPE = new CustomPacketPayload.Type<>(ID);
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
        public Type<ShowScoreBoardS2CPayload> type() {
            return TYPE;
        }
    }

    public record SetRespawnMusicS2CPayload(boolean bl) implements CustomPacketPayload {
        public static final Identifier ID = Identifier.fromNamespaceAndPath(Wuhumc.MOD_ID, "set_respawn_music");
        public static final Type<SetRespawnMusicS2CPayload> TYPE = new CustomPacketPayload.Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, SetRespawnMusicS2CPayload> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, SetRespawnMusicS2CPayload::bl, SetRespawnMusicS2CPayload::new);

        @Override
        public Type<SetRespawnMusicS2CPayload> type() {
            return TYPE;
        }
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ServerConfigC2SPayload.TYPE, ServerConfigC2SPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(ServerConfigS2CPayload.TYPE, ServerConfigS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShowScoreBoardS2CPayload.TYPE, ShowScoreBoardS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SetRespawnMusicS2CPayload.TYPE, SetRespawnMusicS2CPayload.CODEC);
    }
}
