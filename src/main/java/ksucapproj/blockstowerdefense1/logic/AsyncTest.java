package ksucapproj.blockstowerdefense1.logic;

import ksucapproj.blockstowerdefense1.BlocksTowerDefense1;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AsyncTest implements Runnable{

    private final BlocksTowerDefense1 plugin;
    public AsyncTest(BlocksTowerDefense1 plugin){
        this.plugin=plugin;
    }

    @Override
    public void run() {
        this.plugin.getServer().broadcastMessage(("The current time is " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME)));
    }


}
