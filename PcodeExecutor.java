/*
 * LiuCode虚拟机
 * all rights reserved
 * */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class PcodeExecutor {
    public static enum instrName {
        con, init, var, push, pop, add, sub, mul, div, mod, array, select, paraGet,
        jf, jmp, j,
        initstart, initcheck,
        bst, bnd,
        func,
        arg,
        Return,
        getint, printf,
        $__mainStart,
        $__mainEnd,
        pos, neg,
        eql, neq,
        gre, les, geq, leq,
        not,
        bez, bnz,
        and, or,
        _whileStart_, _whileEnd_,
        $label,
        _ifStart_, _ifEnd_,
        _break_, _continue_
    }
    /*
     * eql:==    neq:!=
     * gre:>     les:<   geq:>=      leq:<=
     * not:!
     * bnz:非0时跳转    bez:栈顶元素为0时跳转
     * */

    public static enum termType {
        imm,
        arrayaddr,
        retaddr,
        bst,
        initstart,
        whilestart,
        ifstart
    }
    /*
     * 这次我们把符号表和运行栈分开，符号表也采用栈式结构
     * 符号表记录某个Ident是常量or变量，几维数组，存储地址（数组的话记录首地址），相关信息等等
     * 开辟一个全局大ArrayList作为LiuCode虚拟机的内存条
     * */

    public static enum SymType {
        var,
        con,
        arg,
        /*
         * 关于bst，retaddr这两个特殊标志，在生成和消除时，需要同时对运行栈和符号表操作
         * 因此在符号表里面也要加入这两个伙计
         * */
        bst,
        retaddr,
        whilestart,
        ifstart
    }

    public int[] memory;
    public int $pc;
    public int $sp;//内存分配指针
    public ArrayList<PcodeInstr> instrSet;
    //public Stack<Term> runTimeStack;
    //public Stack<Sym> symStack;
    public MyStackOfTerm runTimeStack;
    public MyStackOfSym symStack;
    public HashMap<String, Integer> labelMap;
    public HashMap<String, Integer> funcMap;
    public static Scanner scanner = new Scanner(System.in);
    public Stack<Integer> symBst;
    public Stack<Integer> runBst;

    public PcodeExecutor()
    {
        this.memory = null;
        this.$pc = 0;
        this.$sp = 0;
        this.instrSet = new ArrayList<>();
        this.runTimeStack = new MyStackOfTerm();
        this.symStack = new MyStackOfSym();
        this.labelMap = new HashMap<>();
        this.funcMap = new HashMap<>();
        this.symBst = new Stack<>();
        this.runBst = new Stack<>();
    }

    public void initLabelMap()
    {
        for (int i = 0; i < instrSet.size(); i++)
        {
            PcodeInstr ii = instrSet.get(i);
            if (ii.instrName.equals(instrName.$label))
            {
                labelMap.put((String) ii.instrObj, i);
            } else if (ii.instrName.equals(instrName.func))
            {
                funcMap.put((String) ii.instrObj, i);
            }
        }
    }

    public void start()
    {
        try
        {
            BufferedWriter pcodeOut = new BufferedWriter(new FileWriter("pcoderesult.txt"));
            int firstOtherFunPosition = 0;
            boolean findOtherfun = false;
            int mainFunPosition = 0;
            for (int i = 0; i < instrSet.size(); i++)
            {
                if (instrSet.get(i).instrName.equals(instrName.func))
                {
                    firstOtherFunPosition = i;
                    findOtherfun = true;
                    break;
                }
            }
            for (int i = 0; i < instrSet.size(); i++)
            {
                if (instrSet.get(i).instrName.equals(instrName.$__mainStart))
                {
                    mainFunPosition = i;
                    break;
                }
            }
            if (findOtherfun)
            {
                $pc = 0;
                while ($pc != firstOtherFunPosition)
                {
                    execute(pcodeOut);
                }
                $pc = mainFunPosition;
                while ($pc < instrSet.size())
                {
                    execute(pcodeOut);
                }
            } else
            {
                while ($pc < instrSet.size())
                {
                    execute(pcodeOut);
                }
            }
            pcodeOut.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public void execute(BufferedWriter pcodeOut)
    {
        /*
         * 执行当前pc指向的指令，修改运行栈和符号表
         * 完毕后，更新pc
         * */
        PcodeExecutor.instrName curInstrName = instrSet.get($pc).instrName;
        Object curInsObj = instrSet.get($pc).instrObj;
        if (curInstrName.equals(instrName.add))
        {
            int a = runTimeStack.pop().value;
            int b = runTimeStack.pop().value;
            int r = a + b;
            Term temp = new Term(termType.imm, r);
            runTimeStack.push(temp);
            $pc++;
        } else if (curInstrName.equals(instrName.sub))
        {
            int a = runTimeStack.pop().value;
            int b = runTimeStack.pop().value;
            int r = b - a;
            Term temp = new Term(termType.imm, r);
            runTimeStack.push(temp);
            $pc++;
        } else if (curInstrName.equals(instrName.mul))
        {
            int a = runTimeStack.pop().value;
            int b = runTimeStack.pop().value;
            int r = b * a;
            Term temp = new Term(termType.imm, r);
            runTimeStack.push(temp);
            $pc++;
        } else if (curInstrName.equals(instrName.div))
        {
            int a = runTimeStack.pop().value;
            int b = runTimeStack.pop().value;
            int r = b / a;
            Term temp = new Term(termType.imm, r);
            runTimeStack.push(temp);
            $pc++;
        } else if (curInstrName.equals(instrName.mod))
        {
            int a = runTimeStack.pop().value;
            int b = runTimeStack.pop().value;
            int r = b % a;
            Term temp = new Term(termType.imm, r);
            runTimeStack.push(temp);
            $pc++;
        } else if (curInstrName.equals(instrName.neg))
        {
            int a = runTimeStack.pop().value;
            int r = -a;
            Term temp = new Term(termType.imm, r);
            runTimeStack.push(temp);
            $pc++;
        }
        else if(curInstrName.equals(instrName.and))
        {
            int a = runTimeStack.pop().value;
            int b = runTimeStack.pop().value;
            int r = (a == 1 && b == 1) ? 1 : 0;
            Term temp = new Term(termType.imm, r);
            runTimeStack.push(temp);
            $pc++;
        }
        else if(curInstrName.equals(instrName.or))
        {
            int a = runTimeStack.pop().value;
            int b = runTimeStack.pop().value;
            int r = (a == 1 || b == 1) ? 1 : 0;
            Term temp = new Term(termType.imm, r);
            runTimeStack.push(temp);
            $pc++;
        }
        else if (curInstrName.equals(instrName.pos))
        {
            $pc++;
        } else if (curInstrName.equals(instrName.con))
        {
            String t = (String) curInsObj;
            Sym sym = new Sym(SymType.con, t, 0);
            symStack.push(sym);
            $pc++;
        } else if (curInstrName.equals(instrName.var))
        {
            String t = (String) curInsObj;
            Sym sym = new Sym(SymType.var, t, 0);
            symStack.push(sym);
            $pc++;
        } else if (curInstrName.equals(instrName.array))
        {
            int dimension = (int) curInsObj;
            int a, b;
            switch (dimension)
            {
                case 1:
                    a = runTimeStack.pop().value;
                    symStack.peek().array1Length = a;
                    symStack.peek().alloc1(a);
                    symStack.peek().arrayDimension = 1;
                    break;
                case 2:
                    a = runTimeStack.pop().value;//列数
                    b = runTimeStack.pop().value;//行数
                    symStack.peek().array2RIGHT = a;
                    symStack.peek().array2Left = b;
                    symStack.peek().alloc2(b, a);
                    symStack.peek().arrayDimension = 2;
                    break;
                default:
                    System.out.println("初始化数组" + symStack.peek().symObj + "时出现错误，数组维度既不是一也不是二");
                    break;
            }
            $pc++;
        } else if (curInstrName.equals(instrName.init))
        {
            int l = symStack.peek().array2Left;
            int r = symStack.peek().array2RIGHT;
            int len = symStack.peek().array1Length;
            if (symStack.peek().arrayDimension == 0)
            {
                symStack.peek().value = runTimeStack.pop().value;
            } else if (symStack.peek().arrayDimension == 1)
            {
                for (int i = len - 1; i >= 0; i--)
                {
                    int temp = runTimeStack.pop().value;
                    symStack.peek().array_1_Addr[i] = temp;
                }
            } else if (symStack.peek().arrayDimension == 2)
            {
                int row = symStack.peek().array2RIGHT;
                for (int i = l * r - 1; i >= 0; i--)
                {
                    int temp = runTimeStack.pop().value;
                    symStack.peek().array_2_Addr[i / row][i % row] = temp;//////
                }
            } else
            {
                System.out.println("数组初始化发生错误");
            }
            $pc++;
        } else if (curInstrName.equals(instrName.push))
        {
            if (curInsObj instanceof Integer)
            {
                Term term = new Term(termType.imm, (Integer) curInsObj);
                runTimeStack.push(term);
            } else if (curInsObj instanceof String)
            {
                int tp = symStack.size() - 1;
                for (int i = tp; i >= 0; i--)
                {
                    if (symStack.get(i).symObj.equals(curInsObj))
                    {
                        Sym find = symStack.get(i);
                        if (find.arrayDimension == 0)
                        {
                            runTimeStack.push(new Term(termType.imm, find.value));
                        } else if (find.arrayDimension == 1)
                        {
                            Term term = new Term(termType.arrayaddr, 0);
                            term.arrayDimension = 1;
                            term.array1Length = find.array1Length;
                            term.array_1_Addr = find.array_1_Addr;
                            runTimeStack.push(term);
                        } else if (find.arrayDimension == 2)
                        {
                            Term term = new Term(termType.arrayaddr, 0);
                            term.arrayDimension = 2;
                            term.array2Left = find.array2Left;
                            term.array2RIGHT = find.array2RIGHT;
                            term.array_2_Addr = find.array_2_Addr;
                            runTimeStack.push(term);
                        }
                        break;
                    }
                }
            }
            $pc++;
        } else if (curInstrName.equals(instrName.select))
        {
            int tp = runTimeStack.size() - 1;
            int weidu = 0;
            int xiabiao = 0;
            for (int i = tp; i >= 0; i--)
            {
                if (runTimeStack.get(i).termType.equals(termType.arrayaddr))
                {
                    weidu = runTimeStack.get(i).arrayDimension;
                    break;
                } else if (runTimeStack.get(i).termType.equals(termType.imm))
                {
                    xiabiao++;
                } else
                {
                    System.out.println("select发生错误");
                }
            }
            if (weidu == 1 && xiabiao == 1)
            {
                int xia = runTimeStack.pop().value;
                int res = runTimeStack.pop().array_1_Addr[xia];
                runTimeStack.push(new Term(termType.imm, res));
            } else if (weidu == 2 && xiabiao == 2)
            {
                int rxia = runTimeStack.pop().value;
                int lxia = runTimeStack.pop().value;
                int res = runTimeStack.pop().array_2_Addr[lxia][rxia];
                runTimeStack.push(new Term(termType.imm, res));
            } else if (weidu == 2 && xiabiao == 1)
            {
                //todo:此时是子数组，需要保存子数组首地址
                int left = runTimeStack.pop().value;
                int _2left = runTimeStack.peek().array2Left;
                int _2right = runTimeStack.peek().array2RIGHT;
                int[][] _2addr = runTimeStack.peek().array_2_Addr;
                runTimeStack.peek().termType = termType.arrayaddr;
                runTimeStack.peek().arrayDimension = 1;
                runTimeStack.peek().array_1_Addr = _2addr[left];
                runTimeStack.peek().array1Length = _2right;
            } else
            {
                System.out.println("select时发生错误");
            }
            $pc++;
        } else if (curInstrName.equals(instrName.pop))
        {
            if (curInsObj.equals(0))
            {
                runTimeStack.pop();
            } else
            {
                String obj = (String) curInsObj;
                int tp = symStack.size() - 1;
                for (int i = tp; i >= 0; i--)
                {
                    if (symStack.get(i).symObj.equals(obj))
                    {
                        Sym sym = symStack.get(i);
                        if (sym.arrayDimension == 0)
                        {
                            sym.value = runTimeStack.pop().value;
                        } else if (sym.arrayDimension == 1)
                        {
                            int res = runTimeStack.pop().value;
                            int dst = runTimeStack.pop().value;
                            sym.array_1_Addr[dst] = res;
                        } else if (sym.arrayDimension == 2)
                        {
                            int res = runTimeStack.pop().value;
                            int dsr = runTimeStack.pop().value;
                            int dsl = runTimeStack.pop().value;
                            sym.array_2_Addr[dsl][dsr] = res;
                        }
                        break;
                    }
                }
            }
            $pc++;
        } else if (curInstrName.equals(instrName.bst))
        {
            runTimeStack.push(new Term(termType.bst, 0));
            runBst.push(runTimeStack.top);
            symStack.push(new Sym(SymType.bst, "bst", 0));
            symBst.push(symStack.top);
            $pc++;
        } else if (curInstrName.equals(instrName.bnd))
        {
            int dst = runTimeStack.size() - 1;
            int flag = 0;
            for(int i = dst;i >= 0;i--)
            {
                if(runTimeStack.get(i).termType.equals(termType.bst))
                {
                    flag = i;
                    break;
                }
            }
            //int flag = runBst.pop();
            runTimeStack.changeSize(flag - 1);
            int flags = 0;
            for(int i = symStack.size() - 1;i >= 0;i--)
            {
                if(symStack.get(i).symType.equals(SymType.bst))
                {
                    flags = i;
                    break;
                }
            }
            //int flags = symBst.pop();
            symStack.changeSize(flags - 1);
            $pc++;
        } else if (curInstrName.equals(instrName.func))
        {
            $pc++;
        } else if (curInstrName.equals(instrName.arg))
        {
            String t = (String) curInsObj;
            Sym sym = new Sym(SymType.arg, t, 0);
            symStack.push(sym);
            $pc++;
        } else if (curInstrName.equals(instrName.paraGet))
        {
            int tp = symStack.size() - 1;
            int tprun = runTimeStack.size() - 1;
            int cnt = 0;
            for (int i = tp; i >= 0; i--)
            {
                if (symStack.get(i).symType.equals(SymType.retaddr))
                {
                    break;
                }
                cnt++;
            }
            for (int i = 0; i < cnt; i++)
            {
                Term rpara = runTimeStack.get(tprun - 1 - i);
                Sym fpara = symStack.get(tp - i);
                if (rpara.termType.equals(termType.arrayaddr))
                {
                    fpara.arrayDimension = rpara.arrayDimension;
                    if (fpara.arrayDimension == 1)
                    {
                        fpara.array_1_Addr = rpara.array_1_Addr;
                        fpara.array1Length = rpara.array1Length;
                    } else if (fpara.arrayDimension == 2)
                    {
                        fpara.array_2_Addr = rpara.array_2_Addr;
                        fpara.array2Left = rpara.array2Left;
                        fpara.array2RIGHT = rpara.array2RIGHT;
                    }
                } else if (rpara.termType.equals(termType.imm))
                {
                    fpara.value = rpara.value;
                } else
                {
                    System.out.println("传参过程发生错误");
                }
            }
            $pc++;
        } else if (curInstrName.equals(instrName.jf))
        {
            int retpc = $pc + 1;
            runTimeStack.push(new Term(termType.retaddr, retpc));
            symStack.push(new Sym(SymType.retaddr, "", 0));
           /* boolean findFunc = false;

            for (int i = 0; i < instrSet.size(); i++)
            {
                if (instrSet.get(i).instrName.equals(instrName.func) && instrSet.get(i).instrObj.equals(curInsObj))
                {
                    $pc = i;
                    findFunc = true;
                    break;
                }
            }
            if (!findFunc)
            {
                System.out.println("没有找到对应函数");
                $pc = retpc;
            }*/
            $pc = funcMap.get((String) curInsObj);
        } else if (curInstrName.equals(instrName.Return))
        {
            int argnum = 0;
            int retaddr = 0;
            int retvalue = runTimeStack.peek().value;
            int tp = symStack.size() - 1;
            int tprun = runTimeStack.size() - 1;
            for (int i = tp; i >= 0; i--)
            {
                if (symStack.get(i).symType.equals(SymType.retaddr))
                {
                    break;
                }
                if (symStack.get(i).symType.equals(SymType.arg))
                {
                    argnum++;
                }
            }
            for (int i = tprun; i >= 0; i--)
            {
                if (runTimeStack.get(i).termType.equals(termType.retaddr))
                {
                    retaddr = runTimeStack.get(i).value;
                    break;
                }
            }

            for (int i = symStack.size()-1; i >= 0; i--)
            {
                if(symStack.get(i).symType.equals(SymType.retaddr))
                {
                    symStack.changeSize(i - 1);
                    break;
                }
            }
            for (int i = runTimeStack.size()-1; i >= 0; i--)
            {
                if(runTimeStack.get(i).termType.equals(termType.retaddr))
                {
                    runTimeStack.changeSize(i - 1);
                    break;
                }
            }
            for (int i = 0; i < argnum; i++)
            {
                runTimeStack.pop();
            }
            if (curInsObj.equals("~"))
            {
                runTimeStack.push(new Term(termType.imm, retvalue));
            }
            $pc = retaddr;
        } else if (curInstrName.equals(instrName.getint))
        {
            int a = scanner.nextInt();
            runTimeStack.push(new Term(termType.imm, a));
            $pc++;
        } else if (curInstrName.equals(instrName.printf))
        {
            String t = (String) curInsObj;
            int dnum = 0, sdnum;
            int a = runTimeStack.size() - 1;
            for (int i = 1; i < t.length() - 1; i++)
            {
                if (t.charAt(i) == '%' && t.charAt(i + 1) == 'd')
                {
                    dnum++;
                }
            }
            sdnum = dnum;
            for (int i = 1; i < t.length() - 1; i++)
            {
                if (t.charAt(i) == '%' && t.charAt(i + 1) == 'd')
                {
                    //System.out.print(stack.get(a - sdnum + 1).getTermObj());
                    try
                    {
                        pcodeOut.write(String.valueOf((int) runTimeStack.get(a - sdnum + 1).value));
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    sdnum--;
                    i++;
                } else if (t.charAt(i) == '\\' && t.charAt(i + 1) == 'n')
                {
                    //System.out.print("\n");
                    try
                    {
                        pcodeOut.write("\n");
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    i++;
                } else
                {
                    //System.out.print(t.charAt(i));
                    try
                    {
                        pcodeOut.write(t.charAt(i));
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            for (int i = 0; i < dnum; i++)
            {
                runTimeStack.pop();
            }
            $pc++;
        } else if (curInstrName.equals(instrName.$__mainStart))
        {
            runTimeStack.push(new Term(termType.retaddr, instrSet.size()));
            symStack.push(new Sym(SymType.retaddr, "null", 0));
            $pc++;
        } else if (curInstrName.equals(instrName.$__mainEnd))
        {
            $pc = instrSet.size();
        } else if (curInstrName.equals(instrName.gre))
        {
            int a = runTimeStack.pop().value;
            int b = runTimeStack.pop().value;
            int r = (b > a) ? 1 : 0;
            runTimeStack.push(new Term(termType.imm, r));
            $pc++;
        } else if (curInstrName.equals(instrName.les))
        {
            int a = runTimeStack.pop().value;
            int b = runTimeStack.pop().value;
            int r = (b < a) ? 1 : 0;
            runTimeStack.push(new Term(termType.imm, r));
            $pc++;
        } else if (curInstrName.equals(instrName.geq))
        {
            int a = runTimeStack.pop().value;
            int b = runTimeStack.pop().value;
            int r = (b >= a) ? 1 : 0;
            runTimeStack.push(new Term(termType.imm, r));
            $pc++;
        } else if (curInstrName.equals(instrName.leq))
        {
            int a = runTimeStack.pop().value;
            int b = runTimeStack.pop().value;
            int r = (b <= a) ? 1 : 0;
            runTimeStack.push(new Term(termType.imm, r));
            $pc++;
        } else if (curInstrName.equals(instrName.eql))
        {
            int a = runTimeStack.pop().value;
            int b = runTimeStack.pop().value;
            int r = (b == a) ? 1 : 0;
            runTimeStack.push(new Term(termType.imm, r));
            $pc++;
        } else if (curInstrName.equals(instrName.neq))
        {
            int a = runTimeStack.pop().value;
            int b = runTimeStack.pop().value;
            int r = (b != a) ? 1 : 0;
            runTimeStack.push(new Term(termType.imm, r));
            $pc++;
        } else if (curInstrName.equals(instrName.not))
        {
            int t = runTimeStack.peek().value;
            runTimeStack.peek().value = (t == 0) ? 1 : 0;
            $pc++;
        } else if (curInstrName.equals(instrName.bez))
        {
            int rela = runTimeStack.pop().value;
            if (rela == 0)
            {
                $pc = labelMap.get((String) curInsObj);
            } else
            {
                $pc++;
            }
        } else if (curInstrName.equals(instrName.jmp))
        {
            for (int i = symStack.size()-1; i >= 0; i--)
            {
                if(symStack.get(i).symType.equals(SymType.whilestart))
                {
                    symStack.changeSize(i);
                    break;
                }
            }
            for (int i = runTimeStack.size()-1; i >= 0; i--)
            {
                if(runTimeStack.get(i).termType.equals(termType.whilestart))
                {
                    runTimeStack.changeSize(i);
                    break;
                }
            }
            $pc = labelMap.get((String) curInsObj);
        } else if (curInstrName.equals(instrName.$label))
        {
            $pc++;
        } else if (curInstrName.equals(instrName._whileStart_))
        {
            runTimeStack.push(new Term(termType.whilestart, (Integer) curInsObj));
            symStack.push(new Sym(SymType.whilestart, "", 0));
            $pc++;
        } else if (curInstrName.equals(instrName._whileEnd_))
        {
            for (int i = symStack.size()-1; i >= 0; i--)
            {
                if(symStack.get(i).symType.equals(SymType.whilestart))
                {
                    symStack.changeSize(i - 1);
                    break;
                }
            }
            for (int i = runTimeStack.size()-1; i >= 0; i--)
            {
                if(runTimeStack.get(i).termType.equals(termType.whilestart))
                {
                    runTimeStack.changeSize(i - 1);
                    break;
                }
            }
            $pc++;
        } else if (curInstrName.equals(instrName._ifStart_))
        {
            runTimeStack.push(new Term(termType.ifstart, (Integer) curInsObj));
            symStack.push(new Sym(SymType.ifstart, "", 0));
            $pc++;
        } else if (curInstrName.equals(instrName._ifEnd_))
        {
            for (int i = symStack.size()-1; i >= 0; i--)
            {
                if(symStack.get(i).symType.equals(SymType.ifstart))
                {
                    symStack.changeSize(i - 1);
                    break;
                }
            }
            for (int i = runTimeStack.size()-1; i >= 0; i--)
            {
                if(runTimeStack.get(i).termType.equals(termType.ifstart))
                {
                    runTimeStack.changeSize(i - 1);
                    break;
                }
            }
            $pc++;
        } else if (curInstrName.equals(instrName._break_))
        {
            int a = runTimeStack.size() - 1;
            int startflag = 0;
            for (int i = a; i >= 0; i--)
            {
                Term iterm = runTimeStack.get(i);
                if (iterm.termType.equals(termType.whilestart))
                {
                    startflag = iterm.value;
                    break;
                }
            }
            String end = "loopEnd" + startflag;
            for (int i = symStack.size()-1; i >= 0; i--)
            {
                if(symStack.get(i).symType.equals(SymType.whilestart))
                {
                    symStack.changeSize(i);
                    break;
                }
            }
            for (int i = runTimeStack.size()-1; i >= 0; i--)
            {
                if(runTimeStack.get(i).termType.equals(termType.whilestart))
                {
                    runTimeStack.changeSize(i);
                    break;
                }
            }
            $pc = labelMap.get(end);
        } else if (curInstrName.equals(instrName._continue_))
        {
            int a = runTimeStack.size() - 1;
            int startflag = 0;
            for (int i = a; i >= 0; i--)
            {
                Term iterm = runTimeStack.get(i);
                if (iterm.termType.equals(termType.whilestart))
                {
                    startflag = iterm.value;
                    break;
                }
            }
            String start = "loopStart" + startflag;
            for (int i = symStack.size()-1; i >= 0; i--)
            {
                if(symStack.get(i).symType.equals(SymType.whilestart))
                {
                    symStack.changeSize(i);
                    break;
                }
            }
            for (int i = runTimeStack.size()-1; i >= 0; i--)
            {
                if(runTimeStack.get(i).termType.equals(termType.whilestart))
                {
                    runTimeStack.changeSize(i);
                    break;
                }
            }
            $pc = labelMap.get(start);
        } else if (curInstrName.equals(instrName.j))
        {
            $pc = labelMap.get((String) curInsObj);
        } else
        {
            System.out.println(curInstrName);
        }
    }
}
