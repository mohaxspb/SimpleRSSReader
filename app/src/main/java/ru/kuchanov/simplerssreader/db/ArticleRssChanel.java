package ru.kuchanov.simplerssreader.db;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

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