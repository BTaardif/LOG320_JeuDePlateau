public class Case {
    private String case_id;  // Identifiant de la case
    private Mark cMark;      // Marque (ex: EMPTY, X, O)

    // Constructeur de la classe Case
    public Case(String id, Mark mark) {
        this.case_id = id;
        this.cMark = mark;
    }

    // Getter pour case_id
    public String getCase_id() {
        return case_id;
    }

    // Setter pour case_id
    public void setCase_id(String case_id) {
        this.case_id = case_id;
    }

    // Getter pour cMark
    public Mark getCMark() {
        return cMark;
    }

    // Setter pour cMark
    public void setCMark(Mark cMark) {
        this.cMark = cMark;
    }
}

