/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
// Additional compatibility work Copyright (C) 2026 Cobblemon Contributors and IdeasInTheWorks.

package com.cobblemon.mod.common.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Keeps transient textures alive through drawing, then hands cleanup to the GPU fence queue. */
public final class FrameResources {
    private static List<Runnable> pending = new ArrayList<>();

    private FrameResources() { }

    public static synchronized void retain(Runnable release) {
        pending.add(release);
    }

    public static void finishFrame(Consumer<Runnable> fence) {
        List<Runnable> finished;
        synchronized (FrameResources.class) {
            finished = pending;
            pending = new ArrayList<>();
        }
        if (!finished.isEmpty()) fence.accept(() -> {
            RuntimeException failure = null;
            for (Runnable release : finished) {
                try { release.run(); }
                catch (RuntimeException error) {
                    if (failure == null) failure = error;
                    else failure.addSuppressed(error);
                }
            }
            if (failure != null) throw failure;
        });
    }
}
