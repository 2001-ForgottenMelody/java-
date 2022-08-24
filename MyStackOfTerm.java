public class MyStackOfTerm {
    public Term[] termstack;
    public int top;

    public MyStackOfTerm()
    {
        this.termstack = new Term[5120];
        this.top = -1;
    }

    public void push(Term term)
    {
        termstack[++top] = term;
    }

    public Term pop()
    {
        return termstack[top--];
    }

    public Term peek()
    {
        return termstack[top];
    }

    public int size(){
        return top + 1;
    }

    public void changeSize(int top){
        this.top = top;
    }

    public Term get(int i)
    {
        return termstack[i];
    }
}
