package xyz.jinenze.wuhumc.action;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public record ServerActionContext(List<PlayerProcessor<ServerPlayerEntity>> processors) {
}
