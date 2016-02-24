package ru.kuchanov.simplerssreader.db;


import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Юрий on 14.02.2016 19:09.
 * For SimpleRSSReader.
 */
@DatabaseTable(tableName = "article_category")
public class ArticleRssChanel
{
    public static final String LOG = ArticleRssChanel.class.getSimpleName();
    public static final String FIELD_ARTICLE_ID = "articleId";
    public static final String FIELD_CATEGORY_ID = "categoryId";
    public static final String FIELD_NEXT_ARTICLE_ID = "nextArticleId";
    public static final String FIELD_PREVIOUS_ARTICLE_ID = "previousArticleId";
    public static final String FIELD_IS_INITIAL_IN_CATEGORY = "isInitialInCategory";
    public static final String FIELD_IS_TOP_IN_CATEGORY = "isTopInCategory";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = FIELD_ARTICLE_ID)
    private int articleId;

    @DatabaseField(columnName = FIELD_CATEGORY_ID)
    private int categoryId;

    public static ArrayList<ArticleRssChanel> getArtRssByRssChanel(RssChanel rssChanel, MyRoboSpiceDatabaseHelper1 helper)
    {
        ArrayList<ArticleRssChanel> articleRssChanels = null;

        try
        {
            articleRssChanels = (ArrayList<ArticleRssChanel>) helper.getDaoArtRssChanel().queryBuilder()
                    .where().eq(FIELD_CATEGORY_ID, rssChanel.getId()).query();
            if (articleRssChanels.size() != 0)
            {
                return articleRssChanels;
            }

            else
            {
                return null;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteSomeArts(int numOfArtsToDelete, String rssUrl, MyRoboSpiceDatabaseHelper1 helper)
    {
        try
        {
            RssChanel rssChanel = RssChanel.getRssChanelByUrl(rssUrl, helper);
            ArrayList<ArticleRssChanel> articleRssChanels = (ArrayList<ArticleRssChanel>) helper.getDaoArtRssChanel().queryBuilder().
                    where().eq(FIELD_CATEGORY_ID, rssChanel.getId()).query();
            ArrayList<Article> articles = getArticlesFromArtRss(articleRssChanels, helper);
            Collections.sort(articles, new Article.PubDateComparator());

            for (int i = 0; i < numOfArtsToDelete; i++)
            {
                Article article = articles.get(i);
                ArticleRssChanel articleRssChanelToDelete = getArtRssByArticleAndRssChanelIds(article.getId(), rssChanel.getId(), helper);
                helper.getDaoArtRssChanel().delete(articleRssChanelToDelete);
                Log.d(LOG, "delete: " + article.getTitle());
                helper.getDaoArticle().delete(article);

            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static ArticleRssChanel getArtRssByArticleAndRssChanelIds(int articleId, int rssChanelId, MyRoboSpiceDatabaseHelper1 helper)
    {
        ArticleRssChanel articleRssChanel = null;

        try
        {
            articleRssChanel = helper.getDaoArtRssChanel().queryBuilder()
                    .where().eq(FIELD_ARTICLE_ID, articleId).and().eq(FIELD_CATEGORY_ID, rssChanelId).queryForFirst();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return articleRssChanel;
    }

    public static ArrayList<Article> getArticlesFromArtRss(ArrayList<ArticleRssChanel> articleRssChanels, MyRoboSpiceDatabaseHelper1 helper)
    {
        ArrayList<Article> articles = new ArrayList<>();

        for (ArticleRssChanel articleRssChanel : articleRssChanels)
        {
            try
            {
                Article article = helper.getDaoArticle().queryForId(articleRssChanel.getArticleId());
                articles.add(article);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        return articles;
    }

    /**
     * @return num of new arts
     */
    public static int writeToArtRssFeedTable(ArrayList<Article> articles, RssChanel rssChanel, MyRoboSpiceDatabaseHelper1 helper)
    {
        int numOfNewArts = 0;

        boolean isFirstLoading = !artByRssChanelExists(rssChanel, helper);

        for (int i = 0; i < articles.size(); i++)
        {
            Article article = articles.get(i);
            ArticleRssChanel inDBObj = getArtRssByArtAndRss(article, rssChanel, helper);
            if (inDBObj == null)
            {
                try
                {
                    inDBObj = new ArticleRssChanel();
                    inDBObj.setArticleId(article.getId());
                    inDBObj.setCategoryId(rssChanel.getId());
                    helper.getDaoArtRssChanel().create(inDBObj);
                    numOfNewArts++;
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }

        if (isFirstLoading)
        {
            return -1;
        }

        return numOfNewArts;
    }

    public static ArticleRssChanel getArtRssByArtAndRss(Article article, RssChanel rssChanel, MyRoboSpiceDatabaseHelper1 helper)
    {
        ArticleRssChanel articleRssChanel = null;

        try
        {
            articleRssChanel = helper.getDaoArtRssChanel().queryBuilder().where().eq(FIELD_ARTICLE_ID, article.getId()).and().eq(FIELD_CATEGORY_ID, rssChanel.getId()).queryForFirst();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return articleRssChanel;
    }

    public static boolean artByRssChanelExists(RssChanel rssChanel, MyRoboSpiceDatabaseHelper1 helper)
    {
        boolean exists = false;
        try
        {
            ArticleRssChanel articleRssChanel = helper.getDaoArtRssChanel().queryBuilder().where().eq(FIELD_CATEGORY_ID, rssChanel.getId()).queryForFirst();
            if (articleRssChanel != null)
            {
                exists = true;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return exists;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getArticleId()
    {
        return articleId;
    }

    public void setArticleId(int articleId)
    {
        this.articleId = articleId;
    }

    public int getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(int categoryId)
    {
        this.categoryId = categoryId;
    }
}