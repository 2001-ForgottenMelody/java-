public class Sym {
    public PcodeExecutor.SymType symType;
    public String symObj;
    public int arrayDimension;
    public int array2RIGHT;
    public int[] array_1_Addr;
    public int[][] array_2_Addr;
    public int value;
    public int array1Length;
    public int array2Left;


    public Sym(PcodeExecutor.SymType symType, String symObj, int arrayDimension)
    {
        this.symType = symType;
        this.symObj = symObj;
        this.arrayDimension = arrayDimension;
        this.array1Length = 0;
        this.array2Left = 0;
        this.array2RIGHT = 0;
        this.array_1_Addr = null;
        this.array_2_Addr = null;
        this.value = 0;
    }

    public void alloc1(int length)
    {
        this.array_1_Addr = new int[length];
    }

    public void alloc2(int left, int right)
    {
        this.array_2_Addr = new int[left][right];
    }
}
