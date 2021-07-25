package dev.sebastianb.chatformatter.mixin;

import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {

    @Final
    @Shadow
    private MinecraftServer server;
    @Final
    @Shadow
    private List<ServerPlayerEntity> players;

    @Inject(method = "broadcastChatMessage", at = @At("HEAD"), cancellable = true)
    private void onBroadcastChat(Text message, MessageType type, UUID sender, CallbackInfo ci) {
        System.out.println("test");
        if (!type.equals(MessageType.CHAT)) {
            return; // returns to prevent game join coloring and such from being messed with
        }
        Text newMessage = Text.of("<" + message + ">");
        this.server.sendSystemMessage(newMessage, sender);
        for (ServerPlayerEntity serverPlayerEntity : this.players) {
            serverPlayerEntity.sendMessage(newMessage, type, sender);
        }
        ci.cancel();
    }
}
