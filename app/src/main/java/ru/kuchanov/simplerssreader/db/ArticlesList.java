package ru.kuchanov.simplerssreader.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Юрий on 13.02.2016 21:51.
 * For SimpleRSSReader.
 */
@DatabaseTable(tableName = "articles_list")
public class ArticlesList
{
    public static final String LOG = ArticlesList.class.getSimpleName();

    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @ForeignCollectionField(eager = false)
    private Collection<Article> result;


    @DatabaseField
    private boolean containsBottomArt = false;
    /**
     * can be
     * (-1) - initial loading
     * (0) - no new
     * (1-9) exact quont of new arts
     * (10) - 10 or more new arts;
     * <p/>
     * or
     * -2 if is not setted
     */
    @DatabaseField
    private int numOfNewArts = -2;

    public static void deleteAllEntries(MyRoboSpiceDatabaseHelper h)
    {
        try
        {
            ArrayList<ArticlesList> articles = (ArrayList<ArticlesList>) h.getDao(ArticlesList.class).queryForAll();
            h.getDao(ArticlesList.class).delete(articles);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public Collection<Article> getResult()
    {
        return result;
    }

    public void setResult(Collection<Article> result)
    {
        this.result = result;
    }

    public boolean isContainsBottomArt()
    {
        return containsBottomArt;
    }

    public void setContainsBottomArt(boolean containsBottomArt)
    {
        this.containsBottomArt = containsBottomArt;
    }

    public int getNumOfNewArts()
    {
        return numOfNewArts;
    }

    public void setNumOfNewArts(int numOfNewArts)
    {
        this.numOfNewArts = numOfNewArts;
    }

}