// This file is copied and modified from the Shared Resources mod, with permission by the creators: enjarai and jacg.
// Their repository: https://github.com/enjarai/shared-resources
// Copied file: https://github.com/enjarai/shared-resources/blob/master/src/main/java/nl/enjarai/shared_resources/versioned/Versioned.java

package io.github.andyrusso.pvplegacyutils;

import com.mojang.authlib.GameProfile;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

public class Versioned {
    private static Method NAME_METHOD;
    private static Method ID_METHOD;

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
