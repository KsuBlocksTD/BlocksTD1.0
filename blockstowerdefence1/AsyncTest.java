package ksucapproj.blockstowerdefence1;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class AsyncTest implements Runnable{

    private final BlocksTowerDefence1 plugin;
    public AsyncTest(BlocksTowerDefence1 plugin){
        this.plugin=plugin;
    }

    @Override
    public void run() {
        this.plugin.getServer().broadcastMessage(("The current time is " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME)));
    }


}
