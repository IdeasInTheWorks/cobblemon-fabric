/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.client.model;

/** ARGB interpolation retaining the original transparent-edge color rule and floor rounding. */
public final class TextureInterpolation {
    private TextureInterpolation() { }

    public static int blend(int first, int second, float fraction) {
        float a = ((first >>> 24) & 255) / 255F;
        float b = ((second >>> 24) & 255) / 255F;
        int color = ((int) Math.floor((a + (b - a) * fraction) * 255)) << 24;
        for (int shift : new int[] {16, 8, 0}) {
            float x = ((first >>> shift) & 255) / 255F;
            float y = ((second >>> shift) & 255) / 255F;
            int channel = (int) Math.floor((a == 0F && b == 1F ? y
                    : a == 1F && b == 0F ? x : x + (y - x) * fraction) * 255);
            color |= channel << shift;
        }
        return color;
    }
}
