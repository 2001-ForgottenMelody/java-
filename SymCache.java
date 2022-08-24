public class SymCache {
    private String token;
    private Compiler.Symbols symbol;

    public SymCache(String token, Compiler.Symbols symbol)
    {
        this.token = token;
        this.symbol = symbol;
    }
    //

    public void setSymbol(Compiler.Symbols symbol) {
        this.symbol = symbol;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public Compiler.Symbols getSymbol() {
        return symbol;
    }
}
