package com.ohso.omgubuntu.sqlite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.ohso.omgubuntu.R;

public class ArticleDataSource extends BaseDataSource {
    private Article articleSpec = new Article();
    private CategoryDataSource categorySource;
    private Context mContext;
    public ArticleDataSource(Context context) {
        super(context);
        mContext = context;
        categorySource = new CategoryDataSource(context);
    }

    public Article getArticle(String path, boolean withContent) {
        Article article = new Article();
        Cursor cursor = database.query("article", articleSpec.getColumnNames(), "path = '" + path + "'", null, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            article = cursorToArticle(cursor, withContent);
        } else article = null;
        cursor.close();
        return article;
    }

    public Articles getArticles(boolean withContent) {
        Cursor cursor = database.query("article", articleSpec.getColumnNames(),
                null, null, null, null, "date DESC", "15");
        if (cursor.getCount() < 0) {
            cursor.close();
            return null;
        }
        Articles latestArticles = new Articles();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            latestArticles.add(cursorToArticle(cursor, withContent));
            cursor.moveToNext();
        }
        cursor.close();
        return latestArticles;
    }

    public Articles getArticlesSince(String path, boolean includingId, boolean unreadOnly, boolean withContent) {
        Article sinceArticle = getArticle(path, false);
        Articles articles = new Articles();
        long lessThanDate = includingId ? sinceArticle.getDate() - 1 : sinceArticle.getDate();
        // SELECT * FROM article WHERE unread = 1 AND date > 1353075996999
        String whereClause = (unreadOnly ? "unread = 1 AND " : "") + "date > " + String.valueOf(lessThanDate);
        Cursor cursor = database.query("article", articleSpec.getColumnNames(),
                whereClause, null, null, null, "date DESC");
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                articles.add(cursorToArticle(cursor, withContent));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return articles;
    }

    public Articles getArticlesWithCategory(String categoryName, boolean withContent) {
        Articles articles = new Articles();
        String rawQuery = "SELECT * FROM article a INNER JOIN article_category ac ON ac.article_id=a.path WHERE ac.category_id = ? ORDER BY date DESC LIMIT 15";
        Cursor cursor = database.rawQuery(rawQuery, new String[] {categoryName});
        if (cursor.getCount() < 0) {
            cursor.close();
            return articles;
        }
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            articles.add(cursorToArticle(cursor, withContent));
            cursor.moveToNext();
        }
        cursor.close();
        return articles;
    }

    public Articles getStarredArticles(boolean withContent) {
        Cursor cursor = database.query("article", articleSpec.getColumnNames(),
                "starred = 1", null, null, null, "date DESC", "15");
        if (cursor.getCount() < 0) {
            cursor.close();
            return null;
        }
        Articles latestArticles = new Articles();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            latestArticles.add(cursorToArticle(cursor, withContent));
            cursor.moveToNext();
        }
        cursor.close();
        return latestArticles;
    }


    /**
     * Adds articles to the database from an Articles object.
     *
     * @param articles The Articles object from which articles are added/updated
     * @param updateArticleIfLessThanADayOld Whether to update the article if it's less than a day old
     * @param replaceThumbIfNull If not less than a day old, should replace the thumb anyway if it's null
     */
    public void createArticles(Articles articles, boolean updateArticleIfLessThanADayOld, boolean replaceThumbIfNull) {
        for(Article article : articles) {
            if (!createArticle(article, updateArticleIfLessThanADayOld, replaceThumbIfNull)) break;
        }
    }

    /**
     * Adds articles to the database from an Articles object.
     *
     * @param articles The Articles object from which articles are added/updated
     * @param updateArticleIfLessThanADayOld whether to update the article if it's less than a day old
     */
    public void createArticles(Articles articles, boolean updateArticleIfLessThanADayOld) {
        for(Article article : articles) {
            if (!createArticle(article, updateArticleIfLessThanADayOld, true)) break;
        }
    }

    public void setArticleToUnread(boolean unread, String path) {
        ContentValues values = new ContentValues();
        values.put("path", path);
        values.put("unread", unread ? 1 : 0);
        database.update("article", values, "path = '" + path + "'", null);
    }

    public void setArticleToStarred(boolean starred, String path) {
        ContentValues values = new ContentValues();
        values.put("path", path);
        values.put("starred", starred ? 1 : 0);
        database.update("article", values, "path = '" + path + "'", null);
}

    public Article updateArticle(Article article) {
        Cursor cursor = database.query("article", articleSpec.getColumnNames(),
                "path = '" + article.getPath() + "'", null, null, null, null);
        if (cursor.getCount() < 0) {
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        Article existing = cursorToArticle(cursor, false);
        ContentValues values = new ContentValues();
        values.put("title", article.getTitle());
        values.put("content", article.getContent());
        values.put("created_at", new Date().getTime());
        values.put("starred", article.getStarred());
        values.put("unread", article.getUnread());
        database.update("article", values, "path = '"+ existing.getPath() + "'" , null);
        cursor.close();
        cursor= database.query("article", articleSpec.getColumnNames(),
                "path = '" + article.getPath() + "'", null, null, null, null);
        cursor.moveToFirst();
        Article updatedArticle = cursorToArticle(cursor, true);
        cursor.close();
        return updatedArticle;
    }

    /**
     * Creates the article.
     *
     * @param article The article to be added/updated in the database
     * @param updateArticleIfLessThanADayOld Whether to update the article if it's less than a day old
     * @param replaceThumbIfNull Whether to replace the thumbnail if it's null and regardless of age
     * @return true, if successful; false if duplicates and neither boolean is set.
     */
    public boolean createArticle(Article article, boolean updateArticleIfLessThanADayOld, boolean replaceThumbIfNull) {
        // TODO index paths
        //Log.i("OMG!", "Creating article " + article.getPath());
        Cursor cursor = database.query("article", articleSpec.getColumnNames(),
                "path = '" + article.getPath() + "'", null, null, null, null);
        if (cursor.getCount() > 0) {
            //Log.i("OMG!", "Article " + article.getTitle() + " exists.");
            if (updateArticleIfLessThanADayOld) {
                cursor.moveToFirst();
                Article existing = cursorToArticle(cursor, false);
                if (existing.getDate() > (new Date().getTime() - 86400000)) {
                    ContentValues values = new ContentValues();
                    values.put("title", article.getTitle());
                    values.put("thumb", article.getThumb());
                    values.put("summary", article.getSummary());
                    values.put("content", article.getContent());
                    values.put("created_at", new Date().getTime());
                    database.update("article", values, "path = '"+ existing.getPath() + "'" , null);
                    //Log.i("OMG!", "Less than a day old, so updated title, thumb, summary, content");
                }
                cursor.close();
                return true;
            }
            else if (replaceThumbIfNull) { //Check if the thumbnail is null and replace if so
                cursor.moveToFirst(); //We should only ever have one returned
                Article existing = cursorToArticle(cursor, false);
                if (existing.getThumb() == null && article.getThumb() != null) {
                    ContentValues values = new ContentValues();
                    values.put("thumb", article.getThumb());
                    values.put("summary", article.getSummary());
                    values.put("content", article.getContent());
                    values.put("created_at", new Date().getTime());
                    database.update("article", values, "path = '"+ existing.getPath() + "'" , null);
                    //Log.i("OMG!", "Replaced null thumb with: "+article.getThumb());
                }
                cursor.close();
                return true; //We need to check all entries for null thumbs
            }
            cursor.close();
            return false; //We're not checking thumbs, so stop as soon as we have an entry.
        }
        cursor.close();
        ContentValues values = new ContentValues();
        values.put("title", article.getTitle());
        values.put("path", article.getPath());
        values.put("author", article.getAuthor());
        values.put("thumb", article.getThumb());
        values.put("date", article.getDate());
        values.put("starred", article.getStarred());
        values.put("unread", article.getUnread());
        values.put("summary", article.getSummary());
        values.put("content", article.getContent());
        values.put("created_at", new Date().getTime());
        String articleGuid = null;
        try {
            Uri identifierUri = Uri.parse(article.getIdentifier());
            articleGuid = identifierUri.getQueryParameter("p");
        } catch (Exception e) {}
        values.put("identifier", articleGuid);
        if(database.insert("article", null, values) > 0) {
            try {
                categorySource.open();
                categorySource.setCategories(article.getPath(), article.getCategories());
            } finally {
               categorySource.close();
            }
        }
        return true;
    }

    public boolean isThumbSet(Cursor cursor) {
        if (cursor.getString(3) == null) return false;
        return true;
    }

    public List<Category> getArticleCategories(String articleId) {
        List<Category> categories = new ArrayList<Category>();
        String rawQuery = "SELECT name, title FROM article_category ac INNER JOIN category c ON ac.category_id=c.name WHERE ac.article_id = ?";
        Cursor cursor = database.rawQuery(rawQuery, new String[] {articleId});
        if(cursor.getCount() < 0) {
            cursor.close();
            return categories;
        }
        cursor.moveToFirst();
        categorySource.open();
        while (cursor.isAfterLast() == false) {
            categories.add(categorySource.cursorToCategory(cursor, false));
            cursor.moveToNext();
        }
        categorySource.close();
        return categories;
    }

    private Article cursorToArticle(Cursor cursor, boolean withContent) {
        Article article = new Article();
        article.setPath(cursor.getString(0));
        article.setTitle(cursor.getString(1));
        article.setAuthor(cursor.getString(2));
        article.setThumb(cursor.getString(3));
        article.setDate(cursor.getLong(4));
        article.setStarred(cursor.getInt(5));
        article.setUnread(cursor.getInt(6));
        article.setSummary(cursor.getString(7));
        if (withContent) article.setContent(cursor.getString(8));
        article.setCreatedAt(cursor.getLong(9));
        article.setIdentifier(cursor.getString(10));
        return article;
    }

    public void clearArticlesOverNumberOfEntries() {
        String rawQuery = "SELECT path, created_at FROM article WHERE starred = 1 ORDER BY created_at DESC LIMIT 100 OFFSET "
                + mContext.getResources().getString(R.string.clear_articles_over);
        Cursor cursor = database.rawQuery(rawQuery, null);
        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                //Log.i("OMG!", "Removing article " + cursor.getString(0));
                database.delete("article", "path = '"+ cursor.getString(0) + "'", null);
                cursor.moveToNext();
            }
        }
        cursor.close();
    }

}
