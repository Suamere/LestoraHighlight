package com.lestora.highlight;

import net.minecraft.core.BlockPos;

import java.util.UUID;

public class ColorUtils {

    static HighlightColor generateColorFromUUID(UUID uuid, float alpha) {
        // Use bits from the most and least significant parts of the UUID
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();

        // Extract 8 bits for each channel
        int r = (int)((msb >> 16) & 0xFF);
        int b = (int)((lsb >> 48) & 0xFF);

        // Scale to [0,1] range for floats.
        float rf = r / 255.0f;
        float bf = b / 255.0f;

        return new HighlightColor(rf, 0, bf, alpha);
    }

    static HighlightColor generateColorFromPos(BlockPos pos, float alpha) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        // Determine chunk coordinates and local coordinates.
        int chunkX = Math.floorDiv(x, 16);
        int chunkZ = Math.floorDiv(z, 16);
        int localX = Math.floorMod(x, 16);
        int localZ = Math.floorMod(z, 16);

        // Map the block's coordinates into a "super-chunk" area (3x3 chunks, range 0-47).
        int superX = (((chunkX % 3) + 3) % 3) * 16 + localX;
        int superZ = (((chunkZ % 3) + 3) % 3) * 16 + localZ;

        // Adjust X by shifting it by the Z-ordinal (superZ) and wrap within 0-47.
        int newX = (superX + superZ) % 48;
        // For every odd superZ, reverse the X ordering.
        if (superZ % 2 == 1) {
            newX = 47 - newX;
        }

        // Adjust Z by shifting it by the X-ordinal (superX) and wrap.
        int newZ = (superZ + superX) % 48;
        // For every odd superX, reverse the Z ordering.
        if (superX % 2 == 1) {
            newZ = 47 - newZ;
        }

        // Determine the green value from Y mod 3.
        int modY = Math.floorMod(y, 3);
        float green;
        switch (modY) {
            case 0: green = 0.2f; break;
            case 1: green = 0.5f; break;
            case 2: green = 0.8f; break;
            default: green = 0.0f; break;
        }

        // Scale newX and newZ from [0,47] to [0.0f,1.0f]
        float red = newX / 47.0f;
        float blue = newZ / 47.0f;

        return new HighlightColor(red, green, blue, alpha);
    }
}
