package me.matthewTest.pluginTest;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class asyncTest implements Runnable{

    private final PluginTest plugin;
    public asyncTest(PluginTest plugin){
        this.plugin=plugin;
    }

    @Override
    public void run() {
        this.plugin.getServer().broadcastMessage(("The current time is " + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME)));
    }


}
