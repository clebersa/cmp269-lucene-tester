package br.ufrgs.inf.cmp269.luceneTester;

/**
 * Defines some the object of a indexable document for the system.
 *
 * @author cleber
 */
public class IndexableDocument {

    public static final String ID_FIELD = "id";
    public static final String TITLE_FIELD = "path";
    public static final String CONTENT_FIELD = "contents";

    private String id;
    private String title;
    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
