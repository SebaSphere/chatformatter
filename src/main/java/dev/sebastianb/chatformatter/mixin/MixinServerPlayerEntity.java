package dev.sebastianb.chatformatter.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {

    @Shadow public ServerPlayNetworkHandler networkHandler;

    @Shadow private ChatVisibility clientChatVisibility;

    @Final @Shadow public MinecraftServer server;

    @Inject(method = "sendMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V", at = @At("HEAD"), cancellable = true)
    private void sendMessage(Text message, MessageType type, UUID sender, CallbackInfo ci) {
        String playerName = server.getPlayerManager().getPlayer(sender).getName().getString();
        String playerMessage = message.getString().replaceAll("<" + playerName + "> ", "");
        Text newMessage = Text.of("<" + playerName + ">:<" + playerMessage + ">");
        if (this.acceptsMessage(type)) {
            this.networkHandler.sendPacket(new GameMessageS2CPacket(newMessage, type, sender), (future) -> {
                if (!future.isSuccess() && (type == MessageType.GAME_INFO || type == MessageType.SYSTEM) && this.acceptsMessage(MessageType.SYSTEM)) {
                    String string = newMessage.asTruncatedString(256);
                    Text text2 = (new LiteralText(string)).formatted(Formatting.YELLOW);
                    this.networkHandler.sendPacket(new GameMessageS2CPacket((new TranslatableText("multiplayer.message_not_delivered", new Object[]{text2})).formatted(Formatting.RED), MessageType.SYSTEM, sender));
                }

            });
        }
        ci.cancel();
    }

    private boolean acceptsMessage(MessageType type) {
        return switch (this.clientChatVisibility) {
            case HIDDEN -> type == MessageType.GAME_INFO;
            case SYSTEM -> type == MessageType.SYSTEM || type == MessageType.GAME_INFO;
            default -> true;
        };
    }
}
