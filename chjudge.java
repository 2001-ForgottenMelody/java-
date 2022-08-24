public class chjudge {
    public static boolean isColon() {
        return Compiler.Char == ':';
    }

    public static boolean isGanTanHao() {
        return Compiler.Char == '!';
    }

    public static boolean isComma() {
        return Compiler.Char == ',';
    }

    public static boolean isSemi() {
        return Compiler.Char == ';';
    }

    public static boolean isAnd() {
        return Compiler.Char == '&';
    }

    public static boolean isOr() {
        return Compiler.Char == '|';
    }

    public static boolean isBigger() {
        return Compiler.Char == '>';
    }

    public static boolean isSmaller() {
        return Compiler.Char == '<';
    }

    public static boolean isEqu() {
        return Compiler.Char == '=';
    }

    public static boolean isPlus() {
        return Compiler.Char == '+';
    }

    public static boolean isMinus() {
        return Compiler.Char == '-';
    }

    public static boolean isDivi() {
        return Compiler.Char == '/';
    }

    public static boolean isMod() {
        return Compiler.Char == '%';
    }

    public static boolean isStar() {
        return Compiler.Char == '*';
    }

    public static boolean isLpar() {
        return Compiler.Char == '(';
    }

    public static boolean isRpar() {
        return Compiler.Char == ')';
    }

    public static boolean isLbarck() {
        return Compiler.Char == '[';
    }

    public static boolean isRbarck() {
        return Compiler.Char == ']';
    }

    public static boolean isLbarce() {
        return Compiler.Char == '{';
    }

    public static boolean isRbarce() {
        return Compiler.Char == '}';
    }

    public static boolean isSpace() {
        return Compiler.Char == ' ';
    }

    public static boolean isNewLine() {
        return Compiler.Char == '\n' || Compiler.Char == '\r';
    }

    public static boolean isTab() {
        return Compiler.Char == '\t';
    }

    public static boolean isUnderLine() {
        return Compiler.Char == '_';
    }

    public static boolean isEndChar() {
        return Compiler.Char == '\0';
    }

    public static boolean isLetter() {
        return (Compiler.Char >= 'a' && Compiler.Char <= 'z') || (Compiler.Char >= 'A' && Compiler.Char <= 'Z');
    }

    public static boolean isShuang() {
        return Compiler.Char == '"';
    }

    public static boolean isDigit() {
        return ((Compiler.Char >= '0') && (Compiler.Char <= '9'));
    }
}
