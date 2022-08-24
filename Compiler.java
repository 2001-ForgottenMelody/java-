import jdk.nashorn.internal.ir.Block;

import javax.print.attribute.standard.MediaSize;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;

public class Compiler {
    public static char Char;
    public static String token;
    public static int num;

    public static enum Symbols {
        IDENFR, INTCON, STRCON, MAINTK, CONSTTK, INTTK, BREAKTK, CONTINUETK, IFTK,
        ELSETK, NOT, AND, OR, WHILETK, GETINTTK, PRINTFTK, RETURNTK, PLUS, MINU, VOIDTK,
        MULT, DIV, MOD, LSS, LEQ, GRE, GEQ, EQL, NEQ, ASSIGN, SEMICN, COMMA, LPARENT, RPARENT,
        LBRACK, RBRACK, LBRACE, RBRACE, NOP
    }

    public static Symbols symbol;

    public static SymChartClass syms = new SymChartClass();
    public static ReservedWordClass reservedWordClass = new ReservedWordClass();
    public static LinkedList<SymCache> cache = new LinkedList<>();
    public static ArrayList<SymCache> pre = new ArrayList<>();
    public static int g = 0;
    public static int $loop = 0;
    public static int $if = 0;
    public static PcodeExecutor pcodeExecutor = new PcodeExecutor();

    public static void main(String[] args)
    {
        try
        {

            BufferedWriter out = new BufferedWriter(new FileWriter("output.txt"));
            PushbackReader in = new PushbackReader(new FileReader("testfile.txt"));
            int temp, j, k;
            gETSYM(in);
            CompUnit(in, out);
            /*while(!(isEOF(in) && cache.isEmpty()))
            {
                Stmt(in,out);
            }*/

            out.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }pcodeExecutor.initLabelMap();
        token = "12345";
        pcodeExecutor.start();
        /*
        for (PcodeInstr pcodeInstr : pcodeExecutor.instrSet)
        {
            System.out.println(pcodeInstr.instrName + " " + pcodeInstr.instrObj);
        }
        System.out.println("------------------------");

        for(Map.Entry<String,Integer> entry:pcodeExecutor.labelMap.entrySet())
        {
            System.out.println(entry.getKey()+ "\t\t\t" + entry.getValue());
        }
         */

    }

    public static void CompUnit(PushbackReader in, BufferedWriter out)
    {
        int typ;
        while ((typ = preRead(in)) != 0)
        {
            if (typ == 1 || typ == 2)
            {
                FuncDef(in, out);
            } else if (typ == 3 || typ == 4)
            {
                Decl(in, out);
            } else
            {
                error();
            }
        }
        MainFuncDef(in, out);
        //printSynAna("CompUnit", out);
    }

    public static boolean isEOF(PushbackReader in)
    {
        try
        {
            int o;
            if ((o = in.read()) == -1)
            {
                return true;
            } else
            {
                in.unread(o);
                return false;
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static void FuncDef(PushbackReader in, BufferedWriter out)
    {
        FuncType(in, out);
        //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.func, token));
        pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.func, token));
        Ident(in, out);
        OtherOp(in, out);//(
        if (!token.equals(")"))
        {
            FuncFParams(in, out);
        }
        OtherOp(in, out);//)
        //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.paraGet, PcodeExecutor.specialEnum.nothing));
        pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.paraGet, 0));
        Block_Ana(in, out);
        //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.Return, PcodeExecutor.specialEnum.nothing));
        pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.Return, 0));
        //printSynAna("FuncDef", out);
    }

    private static void FuncType(PushbackReader in, BufferedWriter out)
    {
        printWord(out);//void|int
        //printSynAna("FuncType", out);
        gETSYM(in);
    }

    public static void MainFuncDef(PushbackReader in, BufferedWriter out)
    {
        //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.mainStart, PcodeExecutor.specialEnum.nothing));
        pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.$__mainStart, 0));
        OtherOp(in, out);//int
        OtherOp(in, out);//main
        OtherOp(in, out);//(
        OtherOp(in, out);//)
        Block_Ana(in, out);
        //printSynAna("MainFuncDef", out);
        //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.mainEnd, PcodeExecutor.specialEnum.nothing));
        pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.$__mainEnd, 0));
    }

    /*
    Stmt语句:
     */
    public static void Stmt(PushbackReader in, BufferedWriter out)
    {
        if (token.equals("if"))
        {
            int ifnum = $if;
            $if++;
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName._ifStart_, ifnum));
            OtherOp(in, out);//if
            OtherOp(in, out);//(
            Cond(in, out);
            OtherOp(in, out);//)
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.bez, "else" + ifnum));
            Stmt(in, out);
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.j, "ifend" + ifnum));
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.$label, "else" + ifnum));
            if (token.equals("else"))
            {
                OtherOp(in, out);//else
                Stmt(in, out);
            }
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.$label, "ifend" + ifnum));
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName._ifEnd_, ifnum));
        } else if (token.equals("while"))
        {
            int loop = $loop;
            $loop++;
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName._whileStart_, loop));
            OtherOp(in, out);//while
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.$label, "loopStart" + loop));
            OtherOp(in, out);//(
            Cond(in, out);
            OtherOp(in, out);//)
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.bez, "loopEnd" + loop));
            Stmt(in, out);
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.jmp, "loopStart" + loop));
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.$label, "loopEnd" + loop));
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName._whileEnd_, loop));

        } else if (token.equals("break"))
        {
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName._break_,0));
            OtherOp(in, out);
            OtherOp(in, out);
        } else if (token.equals("continue"))
        {
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName._continue_,0));
            OtherOp(in, out);
            OtherOp(in, out);
        } else if (token.equals("return"))
        {
            OtherOp(in, out);//return
            if (!token.equals(";"))
            {
                Exp(in, out);
                //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.Return,"~"));
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.Return, "~"));
            } else
            {
                //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.Return,PcodeExecutor.specialEnum.nothing));
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.Return, 0));
            }
            OtherOp(in, out);
        } else if (token.equals("printf"))
        {
            OtherOp(in, out);
            OtherOp(in, out);
            String fs = token;
            FormatString(in, out);
            while (token.equals(","))
            {
                OtherOp(in, out);
                Exp(in, out);
            }
            OtherOp(in, out);//)
            OtherOp(in, out);//;
            //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.printf, fs));
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.printf, fs));
        } else if (token.equals("{"))//必须先判断Block,再讨论Lval
        {
            //todo:
            Block_Ana(in, out);
        } else if (judgeLVal(in))
        {
            String p = token;
            leftLVal(in, out);
            OtherOp(in, out);//=
            if (token.equals("getint"))
            {
                OtherOp(in, out);//getint
                OtherOp(in, out);//(
                OtherOp(in, out);//)
                OtherOp(in, out);//;
                //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.getint, PcodeExecutor.specialEnum.nothing));
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.getint, 0));
            } else
            {
                Exp(in, out);
                OtherOp(in, out);
            }
            //System.out.println("POP " + p);
            if (isNum(p))
            {
                int y = Integer.parseInt(p);
                //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.pop, y));
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.pop, y));
            } else
                //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.pop, p));
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.pop, p));
        } else//exp
        {
            while (!token.equals(";"))
            {
                Exp(in, out);
            }
            OtherOp(in, out);
        }
        //printSynAna("Stmt", out);

    }

    private static void leftLVal(PushbackReader in, BufferedWriter out)
    {
        Ident(in, out);
        if (token.equals("["))
        {
            while (token.equals("["))
            {
                OtherOp(in, out);
                Exp(in, out);
                OtherOp(in, out);
            }
        }
        //printSynAna("LVal", out);
    }

    private static void Block_Ana(PushbackReader in, BufferedWriter out)
    {
        //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.bst,PcodeExecutor.specialEnum.nothing));
        pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.bst, 0));
        OtherOp(in, out);//{
        while (!token.equals("}"))
        {
            BlockItem(in, out);
        }

        OtherOp(in, out);//}
        //printSynAna("Block", out);
        //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.bnd, PcodeExecutor.specialEnum.nothing));
        pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.bnd, 0));
    }

    private static void BlockItem(PushbackReader in, BufferedWriter out)
    {
        if (token.equals("const") || token.equals("int"))
        {
            Decl(in, out);
        } else
        {
            Stmt(in, out);
        }


        /*try {
            out.write("<BlockItem>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public static void Decl(PushbackReader in, BufferedWriter out)
    {
        if (token.equals("const"))
        {
            ConstDecl(in, out);
        } else
        {
            VarDecl(in, out);
        }
        /*try {
            out.write("<Decl>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private static void VarDecl(PushbackReader in, BufferedWriter out)
    {
        BType(in, out);
        VarDef(in, out);
        while (token.equals(","))
        {
            OtherOp(in, out);
            VarDef(in, out);
        }
        OtherOp(in, out);//;
        //printSynAna("VarDecl", out);
    }

    private static void VarDef(PushbackReader in, BufferedWriter out)
    {
        //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.var, token));
        pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.var, token));
        String p = token;
        Ident(in, out);
        int v = 0;
        while (token.equals("["))
        {
            v++;
            OtherOp(in, out);
            ConstExp(in, out);
            OtherOp(in, out);
        }
        /*if (v == 1) System.out.println("array:1");
        else if (v == 2) System.out.println("array:2");*/
        if (v == 1)
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.array, 1));//pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.array, 1));
        else if (v == 2)
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.array, 2));//pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.array, 2));
        boolean hasINIT = false;
        if (token.equals("="))
        {
            hasINIT = true;
            OtherOp(in, out);
            InitVal(in, out);
        }
        if (hasINIT)
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.init, p));//pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.init, p));//System.out.println("INIT " + p);
        //printSynAna("VarDef", out);
    }

    private static void InitVal(PushbackReader in, BufferedWriter out)
    {
        if (!token.equals("{"))
        {
            Exp(in, out);
        } else
        {
            OtherOp(in, out);
            if (!token.equals("}"))
            {
                InitVal(in, out);
                while (token.equals(","))
                {
                    OtherOp(in, out);
                    InitVal(in, out);
                }
            }
            OtherOp(in, out);//}
        }
        //printSynAna("InitVal", out);
    }

    private static void ConstDecl(PushbackReader in, BufferedWriter out)
    {
        OtherOp(in, out);//const
        BType(in, out);//int
        ConstDef(in, out);
        while (token.equals(","))
        {
            OtherOp(in, out);
            ConstDef(in, out);
        }
        OtherOp(in, out);//;
        //printSynAna("ConstDecl", out);
    }

    private static void ConstDef(PushbackReader in, BufferedWriter out)
    {
        //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.con, token));
        pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.con, token));
        String p = token;
        Ident(in, out);
        int v = 0;
        while (token.equals("["))
        {
            v++;
            OtherOp(in, out);
            ConstExp(in, out);
            OtherOp(in, out);
        }
        if (v == 1)
            //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.array, 1));//System.out.println("array:1");
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.array, 1));
        else if (v == 2)
            //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.array, 2));//System.out.println("array:2");
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.array, 2));
        OtherOp(in, out);//=
        ConstInitVal(in, out);
        //printSynAna("ConstDef", out);
        //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.init, p));//System.out.println("INIT " + p);
        pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.init, p));
    }

    private static void ConstInitVal(PushbackReader in, BufferedWriter out)
    {
        if (!token.equals("{"))
        {
            ConstExp(in, out);
        } else
        {
            OtherOp(in, out);
            if (!token.equals("}"))
            {
                ConstInitVal(in, out);
                while (token.equals(","))
                {
                    OtherOp(in, out);
                    ConstInitVal(in, out);
                }
            }
            OtherOp(in, out);//}
        }
        //printSynAna("ConstInitVal", out);
    }


    public static boolean judgeLVal(PushbackReader in)
    {
        String cur_token;
        Symbols cur_symbol;
        cur_symbol = symbol;
        cur_token = token;
        pre.clear();
        pre.add(new SymCache(token, symbol));
        int size = cache.size(), i = 1;
        while (true)
        {
            if (pre.get(0).getToken().equals(";"))
            {
                return false;
            }
            if (i <= size)
            {
                pre.add(cache.get(i - 1));
            } else
            {
                getSYM(in);
                pre.add(new SymCache(token, symbol));
                cache.add(new SymCache(token, symbol));
            }
            if (pre.get(i).getToken().equals(";"))
                break;
            i++;
        }
        symbol = cur_symbol;
        token = cur_token;
        for (SymCache item : pre)
        {
            if (item.getToken().equals("="))
                return true;
        }
        return false;
    }

    private static void FormatString(PushbackReader in, BufferedWriter out)
    {
        printWord(out);
        /*try {
            out.write("<FormatString>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        gETSYM(in);
    }

    private static void Cond(PushbackReader in, BufferedWriter out)
    {
        LOrExp(in, out);
        //printSynAna("Cond", out);
    }


    /*
    waiting for debug:preRead
     */
    public static int preRead(PushbackReader in)
    {
        //预读并判断是不是Decl或者FunDecl
        //todo:
        preGet(in, 2);

        if (token.equals("int") && pre.get(0).getToken().equals("main"))
        {
            //说明没有Decl和FunDecl
            return 0;//主函数返回0
        } else if (token.equals("void") && pre.get(0).getSymbol().equals(Symbols.IDENFR))
        {
            //void型函数.返回1
            return 1;
        } else if (token.equals("int") && pre.get(0).getSymbol().equals(Symbols.IDENFR) && pre.get(1).getToken().equals("("))
        {
            //int型函数,但不是主函数,返回2
            return 2;
        } else if (token.equals("const") && pre.get(0).getToken().equals("int"))
        {
            //常量定义
            return 3;
        } else if (token.equals("int"))
        {
            //全局变量定义
            return 4;
        } else
        {
            error();
            return -1;
        }


    }


    /*
    函数形参表FuncFParams系列
     */
    public static void FuncFParams(PushbackReader in, BufferedWriter out)
    {
        FuncFParam(in, out);
        while (token.equals(","))
        {
            OtherOp(in, out);
            FuncFParam(in, out);
        }
        //printSynAna("FuncFParams", out);
    }

    public static void FuncFParam(PushbackReader in, BufferedWriter out)
    {
        BType(in, out);
        //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.arg, token));
        pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.arg, token));
        Ident(in, out);
        if (token.equals("["))
        {
            OtherOp(in, out);//[
            OtherOp(in, out);//]
            if (token.equals("["))
            {
                OtherOp(in, out);
                ConstExp(in, out);
                OtherOp(in, out);
                //todo:此处有疑问

                //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.pop,PcodeExecutor.specialEnum.nothing));
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.pop, 0));
            }
        }
        //printSynAna("FuncFParam", out);
    }

    private static void BType(PushbackReader in, BufferedWriter out)
    {
        printWord(out);
        gETSYM(in);
    }

    /*
    ConstDecl
     */


    /*
    exp系列
     */
    public static void Exp(PushbackReader in, BufferedWriter out)
    {
        AddExp(in, out);
        //printSynAna("Exp", out);
    }

    public static void AddExp(PushbackReader in, BufferedWriter out)
    {
        MulExp(in, out);
        while (token.equals("+") || token.equals("-"))
        {
            String temp = token;
            OtherOp(in, out);
            MulExp(in, out);
            if (temp.equals("+"))
            {
                //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.add,PcodeExecutor.specialEnum.nothing));
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.add, 0));
            } else
            {
                //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.sub,PcodeExecutor.specialEnum.nothing));
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.sub, 0));
            }
        }
        //printSynAna("AddExp", out);
    }


    /*public static void AddExpx(PushbackReader in, BufferedWriter out) {
        if (token.equals("+") || token.equals("-")) {
            //printSynAna("AddExp", out);
            String op = token.equals("+") ? "ADD" : "SUB";
            OtherOp(in, out);
            MulExp(in, out);
            AddExpx(in, out);
            //System.out.println(op);
            if (op.equals("ADD")) {
                pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.add, PcodeExecutor.specialEnum.nothing));
            } else {
                pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.sub, PcodeExecutor.specialEnum.nothing));
            }
        }
    }*/


    public static void MulExp(PushbackReader in, BufferedWriter out)
    {
        UnaryExp(in, out);
        while (token.equals("*") || token.equals("/") || token.equals("%"))
        {
            String temp = token;
            OtherOp(in, out);
            UnaryExp(in, out);
            if (temp.equals("*"))
            {
                //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.mul,PcodeExecutor.specialEnum.nothing));
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.mul, 0));
            } else if (temp.equals("/"))
            {
                //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.div,PcodeExecutor.specialEnum.nothing));
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.div, 0));
            } else
            {
                //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.mod,PcodeExecutor.specialEnum.nothing));
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.mod, 0));
            }
        }
        //printSynAna("MulExp", out);
    }
/*
    public static void MulExpx(PushbackReader in, BufferedWriter out) {
        if (token.equals("*") || token.equals("/") || token.equals("%")) {
            String op = token.equals("*") ? "MUL" : (token.equals("/")) ? "DIV" : "MOD";
            //printSynAna("MulExp", out);
            OtherOp(in, out);
            UnaryExp(in, out);
            MulExpx(in, out);
            //System.out.println(op);
            if (op.equals("MUL")) {
                pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.mul, PcodeExecutor.specialEnum.nothing));
            } else if (op.equals("DIV")) {
                pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.div, PcodeExecutor.specialEnum.nothing));
            } else {
                pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.mod, PcodeExecutor.specialEnum.nothing));
            }
        }
    }
*/

    public static void OtherOp(PushbackReader in, BufferedWriter out)
    {
        printWord(out);
        gETSYM(in);
    }

    public static void UnaryExp(PushbackReader in, BufferedWriter out)
    {
        preGet(in, 1);
        if (token.equals("+") || token.equals("-") || token.equals("!"))//Uo UE
        {
            String op = token.equals("+") ? "POS" : (token.equals("-")) ? "NEG" : "NOT";
            UnaryOp(in, out);
            UnaryExp(in, out);
            //System.out.println(op);
            if (op.equals("NEG"))
                //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.neg, PcodeExecutor.specialEnum.nothing));
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.neg, 0));
            else if (op.equals("POS"))
                //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.pos, PcodeExecutor.specialEnum.nothing));
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.pos, 0));
            else
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.not, 0));
        } else if (symbol.equals(Symbols.IDENFR) && pre.get(0).getToken().equals("("))
        {
            String p = token;
            Ident(in, out);
            OtherOp(in, out);
            while (!token.equals(")"))
            {
                FuncRParams(in, out);
            }
            OtherOp(in, out);
            //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.jf, p));
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.jf, p));
        } else
        {
            PrimaryExp(in, out);
        }
        //printSynAna("UnaryExp", out);

    }

    public static void Ident(PushbackReader in, BufferedWriter out)
    {
        printWord(out);
        /*try {
            printWord(out);
            out.write("<Ident>\n");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        gETSYM(in);
    }

    public static void FuncRParams(PushbackReader in, BufferedWriter out)
    {
        Exp(in, out);
        while (token.equals(","))
        {
            OtherOp(in, out);
            Exp(in, out);
        }
        //printSynAna("FuncRParams", out);
    }

    public static void PrimaryExp(PushbackReader in, BufferedWriter out)
    {
        if (token.equals("("))
        {
            OtherOp(in, out);
            Exp(in, out);
            OtherOp(in, out);
        } else if (symbol.equals(Symbols.INTCON))
        {
            //System.out.println("PUSH " + token);
            int y = Integer.parseInt(token);
            //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.push, y));
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.push, y));
            Number(in, out);
        } else
        {
            LVal(in, out);
        }
        //printSynAna("PrimaryExp", out);
    }

    public static void LVal(PushbackReader in, BufferedWriter out)
    {
        //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.push, token));
        pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.push, token));
        Ident(in, out);
        if (token.equals("["))
        {
            while (token.equals("["))
            {
                OtherOp(in, out);
                Exp(in, out);
                OtherOp(in, out);
            }
            //pcodeExecutor.getExeSet().add(new InStr(PcodeExecutor.instr.select, PcodeExecutor.specialEnum.nothing));//System.out.println("SELECT");//根据下标给从数组中搜索具体值
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.select, 0));
        }

        //printSynAna("LVal", out);
    }

    public static void Number(PushbackReader in, BufferedWriter out)
    {
        printWord(out);
        //printSynAna("Number", out);
        gETSYM(in);
    }


    public static void UnaryOp(PushbackReader in, BufferedWriter out)
    {//+-!
        printWord(out);
        //printSynAna("UnaryOp", out);
        gETSYM(in);
    }

    public static void RelExp(PushbackReader in, BufferedWriter out)
    {
        AddExp(in, out);
        while (token.equals("<") || token.equals(">") || token.equals("<=") || token.equals(">="))
        {
            String op = token;
            OtherOp(in, out);
            AddExp(in, out);
            if (op.equals("<"))
            {
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.les, 0));
            } else if (op.equals(">"))
            {
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.gre, 0));
            } else if (op.equals("<="))
            {
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.leq, 0));
            } else
            {
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.geq, 0));
            }
        }
        //printSynAna("RelExp", out);
    }

    public static void RelExpx(PushbackReader in, BufferedWriter out)
    {
        if (token.equals("<") || token.equals(">") || token.equals("<=") || token.equals(">="))
        {
            String op = token.equals("<") ? "LST" : token.equals(">") ? "GRT" : token.equals(">=") ? "GRE" : "LSE";
            //printSynAna("RelExp", out);
            OtherOp(in, out);
            AddExp(in, out);
            RelExpx(in, out);
            System.out.println(op);
        }
    }

    public static void EqExp(PushbackReader in, BufferedWriter out)
    {
        RelExp(in, out);
        while (token.equals("==") || token.equals("!="))
        {
            String op = token.equals("==") ? "EQL" : "NEQ";
            OtherOp(in, out);
            RelExp(in, out);
            if (op.equals("EQL"))
            {
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.eql, 0));
            } else
            {
                pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.neq, 0));
            }
        }
        //printSynAna("EqExp", out);
    }

    public static void EqExpx(PushbackReader in, BufferedWriter out)
    {
        if (token.equals("==") || token.equals("!="))
        {
            String op = token.equals("==") ? "EQL" : "NEQ";
            //printSynAna("EqExp", out);
            OtherOp(in, out);
            RelExp(in, out);
            EqExpx(in, out);
            System.out.println(op);
        }
    }

    public static void LAndExp(PushbackReader in, BufferedWriter out)
    {
        EqExp(in, out);
        while (token.equals("&&"))
        {
            OtherOp(in, out);
            EqExp(in, out);
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.and, 0));
        }
        //printSynAna("LAndExp", out);
    }

    public static void LAndExpx(PushbackReader in, BufferedWriter out)
    {
        if (token.equals("&&"))
        {
            //printSynAna("LAndExp", out);
            OtherOp(in, out);
            EqExp(in, out);
            LAndExpx(in, out);
            System.out.println("AND");
        }
    }

    public static void LOrExp(PushbackReader in, BufferedWriter out)
    {
        LAndExp(in, out);
        while (token.equals("||"))
        {
            OtherOp(in, out);
            LAndExp(in, out);
            pcodeExecutor.instrSet.add(new PcodeInstr(PcodeExecutor.instrName.or, 0));
        }
        //printSynAna("LOrExp", out);
    }

    public static void LOrExpx(PushbackReader in, BufferedWriter out)
    {
        if (token.equals("||"))
        {
            try
            {
                out.write("<LOrExp>\n");
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            OtherOp(in, out);
            LAndExp(in, out);
            LOrExpx(in, out);
            System.out.println("OR");
        }
    }

    public static void ConstExp(PushbackReader in, BufferedWriter out)
    {
        AddExp(in, out);
        //printSynAna("ConstExp", out);
    }

    public static void preGet(PushbackReader in, int num)
    {
        String cur_token;
        Symbols cur_symbol;
        cur_symbol = symbol;
        cur_token = token;
        int size = cache.size();
        pre.clear();
        for (int i = 0; i < num; i++)
        {
            if (i + 1 <= size)
            {
                pre.add(cache.get(i));
            } else
            {
                getSYM(in);
                cache.add(new SymCache(token, symbol));
                pre.add(new SymCache(token, symbol));
            }
        }
        symbol = cur_symbol;
        token = cur_token;
    }


    public static int gETSYM(PushbackReader in)
    {
        if (!cache.isEmpty())
        {
            token = cache.get(0).getToken();
            symbol = cache.get(0).getSymbol();
            cache.removeFirst();
            g = 1;
            return 1;
        } else
        {
            g = getSYM(in);
            return g;
        }
    }

    /*
    以下是词法分析的程序______
     */
    /*
    getSYM:保证每次能读入单词,不用担心注释的问题.
     */
    public static int getSYM(PushbackReader in)
    {
        int k;
        while ((k = getsym(in)) != 1)
        {
            if (k == -1)
                return -1;//读完了
        }
        return k;
    }

    public static int getsym(PushbackReader in)
    {
        int j;
        j = getchar(in);
        if (j == -1) return -1;

        clearToken();
        while (chjudge.isSpace() || chjudge.isNewLine() || chjudge.isTab() || chjudge.isEndChar())
        {
            getchar(in);
        }
        if (chjudge.isLetter() || chjudge.isUnderLine())
        {
            while (chjudge.isLetter() || chjudge.isDigit() || chjudge.isUnderLine())
            {
                catToken();
                getchar(in);
            }
            retract(in, Char);
            symbol = reservedWordClass.reserve(token);
            return 1;
        } else if (chjudge.isDigit())
        {
            while (chjudge.isDigit())
            {
                catToken();
                getchar(in);
            }
            retract(in, Char);
            num = transNum();
            symbol = Symbols.INTCON;
            return 1;
        } else if (chjudge.isPlus())
        {
            symbol = Symbols.PLUS;
            token = "+";
            return 1;
        } else if (chjudge.isMod())
        {
            symbol = Symbols.MOD;
            token = "%";
            return 1;
        } else if (chjudge.isMinus())
        {
            symbol = Symbols.MINU;
            token = "-";
            return 1;
        } else if (chjudge.isStar())
        {
            symbol = Symbols.MULT;
            token = "*";
            return 1;
        } else if (chjudge.isDivi())
        {
            getchar(in);
            if (chjudge.isStar())
            {
                do
                {
                    do
                    {
                        getchar(in);
                    } while (!chjudge.isStar());
                    do
                    {
                        getchar(in);
                        if (chjudge.isDivi())
                        {
                            return 0;
                        }
                    } while (chjudge.isStar());
                } while (!chjudge.isStar());
            }
            if (chjudge.isDivi())
            {
                do
                {
                    getchar(in);
                } while (!chjudge.isNewLine());
                return 0;
            }
            retract(in, Char);
            symbol = Symbols.DIV;
            token = "/";
            return 1;
        } else if (chjudge.isAnd())
        {
            getchar(in);
            if (chjudge.isAnd())
            {
                symbol = Symbols.AND;
                token = "&&";
                return 1;
            } else
            {
                error();
            }

        } else if (chjudge.isOr())
        {
            getchar(in);
            if (chjudge.isOr())
            {
                symbol = Symbols.OR;
                token = "||";
                return 1;
            } else
            {
                error();
            }

        } else if (chjudge.isGanTanHao())
        {
            getchar(in);
            if (chjudge.isEqu())
            {
                symbol = Symbols.NEQ;
                token = "!=";
                return 1;
            } else
            {
                symbol = Symbols.NOT;
                retract(in, Char);
                token = "!";
                return 1;
            }
        } else if (chjudge.isLpar())
        {
            symbol = Symbols.LPARENT;
            token = "(";
            return 1;
        } else if (chjudge.isRpar())
        {
            symbol = Symbols.RPARENT;
            token = ")";
            return 1;
        } else if (chjudge.isLbarck())
        {
            symbol = Symbols.LBRACK;
            token = "[";
            return 1;
        } else if (chjudge.isRbarck())
        {
            symbol = Symbols.RBRACK;
            token = "]";
            return 1;
        } else if (chjudge.isLbarce())
        {
            symbol = Symbols.LBRACE;
            token = "{";
            return 1;
        } else if (chjudge.isComma())
        {
            symbol = Symbols.COMMA;
            token = ",";
            return 1;

        } else if (chjudge.isSemi())
        {
            symbol = Symbols.SEMICN;
            token = ";";
            return 1;
        } else if (chjudge.isRbarce())
        {
            symbol = Symbols.RBRACE;
            token = "}";
            return 1;
        } else if (chjudge.isBigger())
        {
            getchar(in);
            if (chjudge.isEqu())
            {
                symbol = Symbols.GEQ;
                token = ">=";
                return 1;
            } else
            {
                symbol = Symbols.GRE;
                retract(in, Char);
                token = ">";
                return 1;
            }
        } else if (chjudge.isEqu())
        {
            getchar(in);
            if (chjudge.isEqu())
            {
                symbol = Symbols.EQL;
                token = "==";
                return 1;
            } else
            {
                symbol = Symbols.ASSIGN;
                retract(in, Char);
                token = "=";
                return 1;
            }
        } else if (chjudge.isSmaller())
        {
            getchar(in);
            if (chjudge.isEqu())
            {
                symbol = Symbols.LEQ;
                token = "<=";
                return 1;
            } else
            {
                symbol = Symbols.LSS;
                retract(in, Char);
                token = "<";
                return 1;
            }
        } else if (chjudge.isShuang())
        {
            int temp = 0;
            while (temp != 2)
            {
                if (chjudge.isShuang())
                {
                    temp++;
                }
                catToken();
                getchar(in);

            }
            retract(in, Char);
            symbol = Symbols.STRCON;
            return 1;
        }

        return 0;


    }

    public static void retract(PushbackReader in, int citem)//回退一个字符
    {
        try
        {
            in.unread(citem);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static int getchar(PushbackReader in)
    {
        try
        {
            int temp = in.read();
            Char = ((char) temp);
            return temp;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    public static void clearToken()
    {
        token = "";
    }

    public static int transNum()
    {
        int res;
        res = Integer.parseInt(token);
        return res;
    }

    public static void catToken()
    {
        token = token + Char;
    }

    /*
    以上是词法分析的程序______
     */
    public static void printWord(BufferedWriter out)
    {
        if (g == 1)
        {
            try
            {
                out.write(symbol + " " + token + "\n");
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void printSynAna(String item, BufferedWriter out)
    {
        try
        {
            out.write("<" + item + ">" + "\n");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void error()
    {
        System.out.println("error:-(");
    }

    public static boolean isNum(String in)
    {
        for (int i = 0; i < in.length(); i++)
        {
            if (!(in.charAt(i) >= '0' && in.charAt(i) <= '9'))
            {
                return false;
            }
        }
        return true;
    }


}
