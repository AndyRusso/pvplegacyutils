// This file is copied and modified from the Shared Resources mod, with permission by the creators: enjarai and jacg.
// Their repository: https://github.com/enjarai/shared-resources
// Copied file: https://github.com/enjarai/shared-resources/blob/master/shared-resources-mc19-3/src/main/java/nl/enjarai/shared_resources/mc19_3/SREntryPoint.java

package io.github.andyrusso.pvplegacyutils.versions.mc1_19_3;

import io.github.andyrusso.pvplegacyutils.VersionedInterface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public class PvPLegacyUtilsVersioned implements VersionedInterface {
    @Override
    public String getVersion() {
        return "1.19.3";
    }

    @Override
    public void sendChatMessage(String message) {
        if (MinecraftClient.getInstance().getNetworkHandler() != null) {
            MinecraftClient.getInstance().getNetworkHandler().sendChatMessage(message);
        }
    }

    @Override
    public void sendCommand(String command) {
        if (MinecraftClient.getInstance().getNetworkHandler() != null) {
            MinecraftClient.getInstance().getNetworkHandler().sendChatCommand(command);
        }
    }

    @Override
    public SoundEvent getNoteBlockBell() {
        return SoundEvents.BLOCK_NOTE_BLOCK_BELL.value();
    }
}
