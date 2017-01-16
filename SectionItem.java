package com.gammazero.signalrocket;

/**
 * Created by Jamie on 1/2/2017.
 */

public class SectionItem implements Item{

    private final String title;

    public SectionItem(String title) {
        this.title = title;
    }

    public String getTitle(){
        return title;
    }

    @Override
    public boolean isSection() {
        return true;
    }

}
