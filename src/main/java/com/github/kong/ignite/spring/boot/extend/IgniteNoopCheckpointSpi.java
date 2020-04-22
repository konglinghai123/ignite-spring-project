package com.github.kong.ignite.spring.boot.extend;

import org.apache.ignite.spi.IgniteSpiException;
import org.apache.ignite.spi.checkpoint.noop.NoopCheckpointSpi;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 避免一些warn日志
 */
public class IgniteNoopCheckpointSpi extends NoopCheckpointSpi{

    private Logger logger = LoggerFactory.getLogger(IgniteNoopCheckpointSpi.class);


    @Override
    public void spiStart(@Nullable String igniteInstanceName) throws IgniteSpiException {
        logger.info("Checkpoints are disabled (to enable configure any GridCheckpointSpi implementation)");
    }
}
