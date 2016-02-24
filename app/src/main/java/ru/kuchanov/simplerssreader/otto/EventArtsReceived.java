package ru.kuchanov.simplerssreader.otto;

import java.util.ArrayList;

import ru.kuchanov.simplerssreader.db.Article;

public class EventArtsReceived
{
    private ArrayList<Article> arts;
    private String rssChanelUrl;

    public EventArtsReceived(String rssChanelUrl, ArrayList<Article> arts)
    {
        this.arts = arts;
        this.rssChanelUrl = rssChanelUrl;
    }

    public ArrayList<Article> getArts()
    {
        return arts;
    }

    public String getRssChanelUrl()
    {
        return rssChanelUrl;
    }
}