package com.piercey;

import org.slf4j.Logger;

public class ExecutionTimer implements AutoCloseable {
    private final long start = System.nanoTime();
    private final Logger logger;
    private final String prefix;
    private final String postfix;

    public ExecutionTimer(final Logger logger, final String prefix) {
        this.logger = logger;
        this.prefix = prefix;
        this.postfix = "";
    }

    public ExecutionTimer(final Logger logger, final String prefix, final String postfix) {
        this.logger = logger;
        this.prefix = prefix;
        this.postfix = postfix;
    }

    @Override
    public void close() {
        final long ns = System.nanoTime() - start;
        final long us = ns / 1000;
        final long ms = us / 1000;
        final long s = ms / 1000;

        String timing;
        if (s > 0) timing = String.format("%d s", s);
        else if (ms > 0) timing = String.format("%d ms", ms);
        else if (us > 0) timing = String.format("%d μs", us);
        else timing = String.format("%d ns", us);

        logger.info("{} in {} {}", prefix, timing, postfix);
    }
}