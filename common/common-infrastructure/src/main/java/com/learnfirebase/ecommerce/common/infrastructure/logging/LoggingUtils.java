package com.learnfirebase.ecommerce.common.infrastructure.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingUtils {
    private LoggingUtils() {
    }

    public static Logger logger(Class<?> type) {
        return LoggerFactory.getLogger(type);
    }
}
