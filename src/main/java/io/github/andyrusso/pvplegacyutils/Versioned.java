// This file is copied and modified from the Shared Resources mod, with permission by the creators: enjarai and jacg.
// Their repository: https://github.com/enjarai/shared-resources
// Copied file: https://github.com/enjarai/shared-resources/blob/master/src/main/java/nl/enjarai/shared_resources/versioned/Versioned.java

package io.github.andyrusso.pvplegacyutils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Objects;

public class Versioned {
    private static VersionedInterface entryPoint;
    public static void load() {
        List<VersionedInterface> entryPoints = FabricLoader.getInstance()
                .getEntrypoints("pvplegacyutils-versioned", VersionedInterface.class);

        // A hacky way to work around the Quilt Loader bug, it loads the 1.19 implementations on 1.19.3,
        // but not the other way around, so this should only use the 1.19.3 implementation if 2 are present.
        if (entryPoints.size() == 1) {
            entryPoint = entryPoints.get(0);
        } else if (entryPoints.size() == 2) {
            for (VersionedInterface entryPoint1: entryPoints) {
                if (Objects.equals(entryPoint1.getVersion(), "1.19.3")) {
                    entryPoint = entryPoint1;
                    break;
                }
            }
        }

        if (entryPoint == null) {
            throw new RuntimeException(
                    "Could not find versioned entrypoint of pvplegacyutils." +
                            "Something went very wrong." +
                            "Are you using an unsupported version of Minecraft?"
            );
        }
    }

    private static void isLoaded() {
        if (entryPoint == null) throw new RuntimeException("entryPoint is null, have you called the load() method?");
    }

    public static void sendChatMessage(String message) {
        isLoaded();
        entryPoint.sendChatMessage(message);
    }

    public static void sendCommand(String command) {
        isLoaded();
        entryPoint.sendCommand(command);
    }

    public static SoundEvent getNoteBlockBell() {
        isLoaded();
        return entryPoint.getNoteBlockBell();
    }
}
