package com.automation.talkspiritbot.utils;

import com.automation.talkspiritbot.service.TalkSpiritScrollService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

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


    public static void sleepRandomBetween(int msMin , int msMax){


        int randomNum = ThreadLocalRandom.current().nextInt(Math.min(msMin,msMax), Math.max(msMin, msMax) + 1); // inclusif
        sleep(randomNum);
    }
}
