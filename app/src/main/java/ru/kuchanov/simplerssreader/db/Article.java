package ru.kuchanov.simplerssreader.db;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Юрий on 13.02.2016 21:13.
 * For SimpleRSSReader.
 */
@DatabaseTable(tableName = "article")
public class Article implements Parcelable
{
    public static final String LOG = Article.class.getSimpleName();
    public static final String FIELD_URL = "url";
    public static final String FIELD_ID = "id";
    /**
     * Parcel implementation
     */
    public static final Parcelable.Creator<Article> CREATOR = new Parcelable.Creator<Article>()
    {

        @Override
        public Article createFromParcel(Parcel source)
        {
            return new Article(source);
        }

        @Override
        public Article[] newArray(int size)
        {
            return new Article[size];
        }
    };
    /**
     * need this for supporting ormListe in roboSpice
     */
    @DatabaseField(foreign = true)
    private ArticlesList result;
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true, columnName = FIELD_ID)
    private int id;
    @DatabaseField(canBeNull = false, columnName = FIELD_URL)
    private String url;
    @DatabaseField(canBeNull = false)
    private String title;
    @DatabaseField(dataType = DataType.DATE)
    private Date pubDate;
    /**
     * is "_!!!!_" divided array of urls of article
     */
    @DatabaseField
    private String imageUrls;
    @DatabaseField
    private String preview;
    @DatabaseField
    private String text;
    @DatabaseField
    private boolean isRead;
    /**
     * is "_!!!!_" divided array of authors of article
     */
    @DatabaseField
    private String authors;
    /**
     * is "_!!!!_" divided array of categories of article
     */
    @DatabaseField
    private String categories;

    @DatabaseField
    private String videoUrl;

    /**
     * Parcel implementation
     */
    private Article(Parcel in)
    {
        this.id = in.readInt();
        this.url = in.readString();
        this.title = in.readString();

        this.pubDate = new Date(in.readLong());

        this.imageUrls = in.readString();

        this.preview = in.readString();
        this.text = in.readString();
        this.isRead = in.readByte() != 0; //myBoolean == true if byte != 0

        this.authors = in.readString();
        this.categories = in.readString();
        this.videoUrl = in.readString();
    }

    /**
     * empty constructor
     */
    public Article()
    {

    }

    public static ArrayList<Article> writeArtsToDB(ArrayList<Article> loadedArts, MyRoboSpiceDataBaseHelper helper)
    {
        ArrayList<Article> artsInDB = new ArrayList<>();
        for (Article article : loadedArts)
        {
            try
            {
                artsInDB.add(helper.getDaoArticle().createIfNotExists(article));
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return artsInDB;
    }

    /**
     * @return Article or null if cant find it by url
     */
    public static Article getArticleByUrl(String url, MyRoboSpiceDataBaseHelper helper)
    {
        Article a = null;
        try
        {
            a = helper.getDaoArticle().queryBuilder().where().eq(FIELD_URL, url).queryForFirst();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return a;
    }

    /**
     * Parcel implementation
     */
    @Override
    public int describeContents()
    {
        return 0;
    }

    /**
     * Parcel implementation
     */
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(id);
        dest.writeString(url);
        dest.writeString(title);

        dest.writeLong(pubDate.getTime());

        dest.writeString(imageUrls);

        dest.writeString(preview);
        dest.writeString(text);
        dest.writeByte((byte) (isRead ? 1 : 0)); //if myBoolean == true, byte == 1

        dest.writeString(authors);
        dest.writeString(categories);
        dest.writeString(videoUrl);
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public ArticlesList getResult()
    {
        return result;
    }

    public void setResult(ArticlesList result)
    {
        this.result = result;
    }

    public int getId()
    {
        return this.id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getImageUrls()
    {
        return imageUrls;
    }

    public void setImageUrls(String imageUrl)
    {
        this.imageUrls = imageUrl;
    }

    public Date getPubDate()
    {
        return pubDate;
    }

    public void setPubDate(Date pubDate)
    {
        this.pubDate = pubDate;
    }

    public String getPreview()
    {
        return preview;
    }

    public void setPreview(String preview)
    {
        this.preview = preview;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public boolean isRead()
    {
        return isRead;
    }

    public void setIsRead(boolean isRead)
    {
        this.isRead = isRead;
    }

    public String getCategories()
    {
        return categories;
    }

    public void setCategories(String categories)
    {
        this.categories = categories;
    }

    public String getAuthors()
    {
        return authors;
    }

    public void setAuthors(String authors)
    {
        this.authors = authors;
    }

    public String getVideoUrl()
    {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl)
    {
        this.videoUrl = videoUrl;
    }

    /**
     * need it to sort articles by date as we get them as unsorted Collection from ArticlesList result of RoboSpice request
     */
    public static class PubDateComparator implements Comparator<Article>
    {
        @Override
        public int compare(Article o1, Article o2)
        {
            return o2.getPubDate().compareTo(o1.getPubDate());
        }
    }
}
