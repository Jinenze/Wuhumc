package xyz.jinenze.wuhumc.action;


import java.util.List;

public record ServerActionContext(List<ServerPlayerProcessor> processors) {
}
