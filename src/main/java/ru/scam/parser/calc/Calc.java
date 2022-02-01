package ru.scam.parser.calc;

class Calc
{
    static String Lex[];
    static int ptrL;

    static String opStack[];
    static int ptrOp;

    static String valStack[];
    static int ptrVal;

    public static void Init()
    {
        opStack=new String[200];
        ptrOp=0;
        valStack=new String[200];
        ptrVal=0;
    }

    public static int Prty(String o)
    {
        int r=-1;
        switch (o)
        {
            case "(":
                r=0;
                break;
            case "+":
            case "-":
                r=1;
                break;
            case "*":
            case "/":
                r=2;
                break;
            case "^":
                r=3;
        }
        return r;
    }

    public static String peekOp()
    {
        return opStack[ptrOp-1];
    }

    public static boolean isEmptyOp()
    {
        return (ptrOp==0);
    }

    public static boolean isEmptyVal()
    {
        return (ptrVal==0);
    }

    public static void pushOp(String op)
    {
        opStack[ptrOp++]=op;
    }

    public static void pushVal(String v)
    {
        valStack[ptrVal++]=v;
    }

    public static String popOp()
    {
        return opStack[--ptrOp];
    }

    public static String popVal()
    {
        return valStack[--ptrVal];
    }

    public static void exec()
    {
        double a1,a2,r;
        String v1,v2;
        String op;

        v2=popVal();
        v1=popVal();
        op=popOp();

        a1=Double.parseDouble(v1);
        a2=Double.parseDouble(v2);

        r=0.0;

        switch (op)
        {
            case "+":
                r=a1+a2;
                break;
            case "-":
                r=a1-a2;
                break;
            case "*":
                r=a1*a2;
                break;
            case "/":
                r=a1/a2;
                break;
            case "^":
                r=Math.pow(a1,a2);
                break;
        }

        v1=Double.toString(r);
        pushVal(v1);

    }

    public static void Calculate(String F)
    {
        int i;
        String curr,top;

        Init();
        parse(F);

        for (i=0; i<= ptrL; i++)
        {
            curr=Lex[i];
            switch (curr)
            {
                case "(":
                    pushOp(curr);
                    break;
                case "+":
                case "-":
                case "*":
                case "/":
                case "^":
                    if (isEmptyOp())
                    {
                        pushOp(curr);
                        break;
                    }
                    top=peekOp();
                    if (Prty(curr) > Prty(top))
                    {
                        pushOp(curr);
                        break;
                    }
                    else
                    {
                        exec();
                        pushOp(curr);
                        break;
                    }
                case ")":
                    while (true)
                    {
                        top=peekOp();
                        if (top.equals("("))
                        {
                            top=popOp();
                            break;
                        }
                        exec();
                    }
                    break;
                default:
                    pushVal(curr);
            }
        }

        while (! isEmptyOp())
        {
            exec();
        }

    }

    public static void parse(String Formula)
    {
        char s;
        int i;
        String Tmp="";
        Lex=new String[200];
        for (i=0; i<200; i++) Lex[i]="";
        ptrL=0;
        for (i=0; i<Formula.length(); i++)
        {
            s=Formula.charAt(i);
            switch (s)
            {
                case '+':
                case '-':
                case '*':
                case '^':
                case '/':
                case '(':
                case ')':
                    if (Tmp.length() > 0)
                    {
                        Lex[ptrL++]=Tmp;
                        Tmp="";
                    }
                    Lex[ptrL++]=""+s;
                    break;
                case ' ':
                    break;
                default:
                    Tmp=Tmp+s;
            }
        }
        if (Tmp.length() > 0) Lex[ptrL]=Tmp;
    }
}
