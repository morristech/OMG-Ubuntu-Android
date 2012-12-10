package com.ohso.omgubuntu.data;

public class ArticleCategory extends BaseTableObject {

    public ArticleCategory() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void setSQL() {
        title = "article_category";
        addColumn(new Column("article_id", "TEXT", "article", "path"));
        addColumn(new Column("category_id", "TEXT"));
    }

}
