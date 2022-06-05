package me.wurgo.antiresourcereload.mixin;

import me.wurgo.antiresourcereload.AntiResourceReload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.*;
import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    private CompletableFuture<ServerResourceManager> managerProvider;

    @Redirect(method = "method_29604", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ServerResourceManager;reload(Ljava/util/List;Lnet/minecraft/server/command/CommandManager$RegistrationEnvironment;ILjava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<ServerResourceManager> antiresourcereload_cachedReload(List<ResourcePack> dataPacks, CommandManager.RegistrationEnvironment registrationEnvironment, int i, Executor executor, Executor executor2) {
        if (dataPacks.size() != 1) { AntiResourceReload.log("Using data-packs, reloading."); }
        else if (this.managerProvider == null) { AntiResourceReload.log("Cached resources unavailable, reloading & caching."); }
        else {
            AntiResourceReload.log("Using cached server resources.");
            return this.managerProvider;
        }

        CompletableFuture<ServerResourceManager> reloaded = ServerResourceManager.reload(dataPacks, registrationEnvironment, i, executor, executor2);
        
        if (dataPacks.size() == 1) {
            this.managerProvider = reloaded;
        }

        return reloaded;
    }
}
