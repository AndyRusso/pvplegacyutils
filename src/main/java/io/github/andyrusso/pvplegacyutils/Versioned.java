// This file is copied and modified from the Shared Resources mod, with permission by the creators: enjarai and jacg.
// Their repository: https://github.com/enjarai/shared-resources
// Copied file: https://github.com/enjarai/shared-resources/blob/master/src/main/java/nl/enjarai/shared_resources/versioned/Versioned.java

package io.github.andyrusso.pvplegacyutils;

import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.sound.SoundEvent;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Versioned {
    private static Method NAME_METHOD;
    private static Method ID_METHOD;
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

    public static ScoreboardObjective getObjectiveForSlot(Scoreboard scoreboard, int slot) {
        isLoaded();
        return entryPoint.getObjectiveForSlot(scoreboard, slot);
    }

    public static Collection<?> getAllScoreHolders(Scoreboard scoreboard, ScoreboardObjective scoreboardObjective) {
        isLoaded();
        return entryPoint.getAllScoreHolders(scoreboard, scoreboardObjective);
    }

    public static String getScoreHolderName(Object scoreHolder) {
        isLoaded();
        return entryPoint.getScoreHolderName(scoreHolder);
    }

    public static void addFireworkParticle(double x, double y, double z) {
        isLoaded();
        entryPoint.addFireworkParticle(x, y, z);
    }

    public static String getProfileName(GameProfile profile) {
        if (NAME_METHOD == null) NAME_METHOD = findAccessor("getName", "name");

        try {
            return (String) NAME_METHOD.invoke(profile);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("PvP Legacy Utils failed to invoke name accessor on GameProfile", e);
        }
    }

    public static UUID getProfileId(GameProfile profile) {
        if (ID_METHOD == null) ID_METHOD = findAccessor("getId", "id");

        try {
            return (UUID) ID_METHOD.invoke(profile);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("PvP Legacy Utils failed to invoke id accessor on GameProfile", e);
        }
    }

    private static Method findAccessor(String name1, String name2) {
        return Arrays.stream(GameProfile.class.getDeclaredMethods())
                .filter(m -> name1.equals(m.getName()) || name2.equals(m.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "PvP Legacy Utils broke something when reflecting GameProfile accessor: " +
                                "neither '" + name1 + "()' nor '" + name2 + "()' found. " +
                                "Please report this to the developer with your Minecraft version."));
    }
}
