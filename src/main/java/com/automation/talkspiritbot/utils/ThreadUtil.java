package com.automation.talkspiritbot.utils;

import com.automation.talkspiritbot.service.TalkSpiritScrollService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtil {

    private static final Logger logger = LoggerFactory.getLogger(ThreadUtil.class);


    public static void sleep(int ms){
        try {
            logger.info("Sleep {} ms", ms);
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            logger.error("Sleep not completed ");
            throw new RuntimeException(e);
        }
    }
}
