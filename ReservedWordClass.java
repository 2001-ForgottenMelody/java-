import java.util.HashMap;

public class ReservedWordClass {
    private HashMap<String, Compiler.Symbols> reWo = new HashMap<>();

    public ReservedWordClass() {
        init_ReWo();
    }

    /*
    init_ReWo初始化保留字表
     */
    private void init_ReWo() {
        reWo.put("main", Compiler.Symbols.MAINTK);
        reWo.put("const", Compiler.Symbols.CONSTTK );
        reWo.put("int", Compiler.Symbols.INTTK);
        reWo.put("break", Compiler.Symbols.BREAKTK);
        reWo.put("continue", Compiler.Symbols.CONTINUETK);
        reWo.put("if", Compiler.Symbols.IFTK);
        reWo.put("else", Compiler.Symbols.ELSETK);
        reWo.put("while", Compiler.Symbols.WHILETK);
        reWo.put("getint", Compiler.Symbols.GETINTTK);
        reWo.put("printf", Compiler.Symbols.PRINTFTK);
        reWo.put("return", Compiler.Symbols.RETURNTK);
        reWo.put("void", Compiler.Symbols.VOIDTK);
    }

    public Compiler.Symbols reserve(String item) {
        return reWo.getOrDefault(item, Compiler.Symbols.IDENFR);
    }
}
