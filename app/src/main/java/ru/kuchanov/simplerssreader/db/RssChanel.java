package ru.kuchanov.simplerssreader.db;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Юрий on 13.02.2016 22:11.
 * For SimpleRSSReader.
 */
@DatabaseTable(tableName = "rss_chanel")
public class RssChanel implements Parcelable
{
    public static final String LOG = RssChanel.class.getSimpleName();
    public static final String FIELD_URL = "url";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_REFRESHED = "refreshed";
    //    Parcel implementation/////////////////////////////
    public static final Parcelable.Creator<RssChanel> CREATOR = new Parcelable.Creator<RssChanel>()
    {
        @Override
        public RssChanel createFromParcel(Parcel source)
        {
            return new RssChanel(source);
        }

        @Override
        public RssChanel[] newArray(int size)
        {
            return new RssChanel[size];
        }
    };
    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(canBeNull = false, columnName = FIELD_URL)
    private String url;
    @DatabaseField(canBeNull = false, columnName = FIELD_TITLE)
    private String title;
    @DatabaseField(canBeNull = false, columnName = FIELD_REFRESHED)
    private Date refreshed = new Date(0);

    //    Parcel implementation/////////////////////////////
    private RssChanel(Parcel in)
    {
        this.id = in.readInt();
        this.url = in.readString();
        this.title = in.readString();

        this.refreshed = new Date(in.readLong());
    }

    /**
     * empty constructor
     */
    public RssChanel()
    {

    }

    public static RssChanel getRssChanelByUrl(String url, MyRoboSpiceDataBaseHelper helper)
    {
        RssChanel rssChanel = null;

        try
        {
            ArrayList<RssChanel> rssChanels = (ArrayList<RssChanel>) helper.getDaoRssChanel().queryForEq(FIELD_URL, url);
            if (rssChanels.size() != 0)
            {
                rssChanel = rssChanels.get(0);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return rssChanel;
    }

    public static RssChanel getRssChanelByTitle(String title, MyRoboSpiceDataBaseHelper helper)
    {
        RssChanel rssChanel = null;

        try
        {
            ArrayList<RssChanel> rssChanels = (ArrayList<RssChanel>) helper.getDaoRssChanel().queryForEq(FIELD_TITLE, title);
            if (rssChanels.size() != 0)
            {
                rssChanel = rssChanels.get(0);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return rssChanel;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Date getRefreshed()
    {
        return refreshed;
    }

    //    Parcel implementation/////////////////////////////
    public void setRefreshed(Date refreshed)
    {
        this.refreshed = refreshed;
    }

    //    Parcel implementation/////////////////////////////
    @Override
    public int describeContents()
    {
        return 0;
    }

    //    Parcel implementation/////////////////////////////
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(id);
        dest.writeString(url);
        dest.writeString(title);

        dest.writeLong(refreshed.getTime());
    }

    /**
     * need this to check if parsed list contains rows from db
     *
     * @param o category to check for equality
     * @return true, if urls of categories are equal
     */
    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof RssChanel))
        {
            return false;
        }

        RssChanel givenCat = (RssChanel) o;

        return this.url.equals(givenCat.getUrl());
    }
}