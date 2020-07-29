package com.saman.transaction;

import java.util.concurrent.atomic.AtomicInteger;

public final class Sequence {

    private static final AtomicInteger modelSequence = new AtomicInteger(1);

    private Sequence() {
    }

    public static int nextModelId() {
        return modelSequence.getAndIncrement();
    }
}