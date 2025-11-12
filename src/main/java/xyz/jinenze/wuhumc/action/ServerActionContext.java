package xyz.jinenze.wuhumc.action;


import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record ServerActionContext(List<PlayerProcessor<ServerPlayer>> processors) {
}
