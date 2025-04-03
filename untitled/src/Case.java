public class Case {
    private String case_id;  // Identifiant de la case
    private Mark cMark;      // Marque (ex: EMPTY, X, O)
    private int index_plateau_x;
    private int index_plateau_y;

    // Constructeur de la classe Case
    public Case(String id, Mark mark,int TicToeX, int TicToeY) {
        this.case_id = id;
        this.cMark = mark;
        this.index_plateau_x = TicToeX;
        this.index_plateau_y = TicToeY;
    }


    public Case(Case case1) {
		this.case_id = case1.case_id;
        this.cMark = case1.cMark;
        this.index_plateau_x = case1.index_plateau_x;
        this.index_plateau_y = case1.index_plateau_y;
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

    public int getIndex_X(){
        return index_plateau_x;
    }

    public int getIndex_Y(){
        return index_plateau_y;
    }
}

