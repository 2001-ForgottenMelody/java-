public class MyStackOfSym {
    public Sym[] symstack;
    public int top;

    public MyStackOfSym()
    {
        this.symstack = new Sym[5120];
        this.top = -1;
    }

    public void push(Sym sym)
    {
        symstack[++top] = sym;
    }

    public Sym pop()
    {
        return symstack[top--];
    }

    public Sym peek()
    {
        return symstack[top];
    }

    public int size(){
        return top + 1;
    }

    public void changeSize(int top){
        this.top = top;
    }

    public Sym get(int i)
    {
        return symstack[i];
    }


}
