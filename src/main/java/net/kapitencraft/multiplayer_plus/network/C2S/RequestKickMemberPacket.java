package net.kapitencraft.multiplayer_plus.network.C2S;

import net.kapitencraft.multiplayer_plus.MultiplayerPlusMod;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class RequestKickMemberPacket implements CustomPacketPayload {
    public static final Type<RequestKickMemberPacket> TYPE = new Type<>(MultiplayerPlusMod.res("guild/request/kick_member"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return null;
    }
}
