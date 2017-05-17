package br.ufrgs.inf.cmp269lucenetesting;

/**
 * Defines some the object of a indexable document for the system.
 *
 * @author cleber
 */
public class IndexableDocument {

    public static final String ID_FIELD = "id";
    public static final String PATH_FIELD = "path";
    public static final String CONTENT_FIELD = "contents";

    private String documentId;
    private String title;
    private String content;

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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
