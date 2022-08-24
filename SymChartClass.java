import java.util.HashMap;

public class SymChartClass {
    private HashMap<String, Compiler.Symbols> SymChart = new HashMap<>();

    public SymChartClass() {
        init_SymChart();
    }

    public Compiler.Symbols find(String item) {
        return SymChart.getOrDefault(item, Compiler.Symbols.NOP);
    }

    private void init_SymChart() {
        SymChart.put("Ident", Compiler.Symbols.IDENFR);
        SymChart.put("IntConst", Compiler.Symbols.INTCON);
        SymChart.put("FormatString", Compiler.Symbols.STRCON);
        SymChart.put("main", Compiler.Symbols.MAINTK);
        SymChart.put("const", Compiler.Symbols.CONSTTK);
        SymChart.put("int", Compiler.Symbols.INTTK);
        SymChart.put("break", Compiler.Symbols.BREAKTK);
        SymChart.put("continue", Compiler.Symbols.CONTINUETK);
        SymChart.put("if", Compiler.Symbols.IFTK);
        SymChart.put("else", Compiler.Symbols.ELSETK);
        SymChart.put("!", Compiler.Symbols.NOT);
        SymChart.put("&&", Compiler.Symbols.AND);
        SymChart.put("||", Compiler.Symbols.OR);
        SymChart.put("while", Compiler.Symbols.WHILETK);
        SymChart.put("getint", Compiler.Symbols.GETINTTK);
        SymChart.put("printf", Compiler.Symbols.PRINTFTK);
        SymChart.put("return", Compiler.Symbols.RETURNTK);
        SymChart.put("+", Compiler.Symbols.PLUS);
        SymChart.put("-", Compiler.Symbols.MINU);
        SymChart.put("void", Compiler.Symbols.VOIDTK);
        SymChart.put("*", Compiler.Symbols.MULT);
        SymChart.put("/", Compiler.Symbols.DIV);
        SymChart.put("%", Compiler.Symbols.MOD);
        SymChart.put("<", Compiler.Symbols.LSS);
        SymChart.put("<=", Compiler.Symbols.LEQ);
        SymChart.put(">", Compiler.Symbols.GRE);
        SymChart.put(">=", Compiler.Symbols.GEQ);
        SymChart.put("==", Compiler.Symbols.EQL);
        SymChart.put("!=", Compiler.Symbols.NEQ);
        SymChart.put("=", Compiler.Symbols.ASSIGN);
        SymChart.put(";", Compiler.Symbols.SEMICN);
        SymChart.put(",", Compiler.Symbols.COMMA);
        SymChart.put("(", Compiler.Symbols.LPARENT);
        SymChart.put(")", Compiler.Symbols.RPARENT);
        SymChart.put("[", Compiler.Symbols.LBRACK);
        SymChart.put("]", Compiler.Symbols.RBRACK);
        SymChart.put("{", Compiler.Symbols.LBRACE);
        SymChart.put("}", Compiler.Symbols.RBRACE);
    }

    public void setSymChart(HashMap<String, Compiler.Symbols> symChart) {
        SymChart = symChart;
    }

    public HashMap<String, Compiler.Symbols> getSymChart() {
        return SymChart;
    }


}
