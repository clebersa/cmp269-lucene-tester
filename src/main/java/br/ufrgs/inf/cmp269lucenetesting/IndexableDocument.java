package br.ufrgs.inf.cmp269lucenetesting;

/**
 *
 * @author cleber
 */
public class IndexableDocument {
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

    @Override
    public String toString() {
        return "ID: " + documentId
                + "\nTITLE: " + title
                + "\nCONTENT: " + content + "\n";
    }
}
