public class Term {
    public int value;
    public int[] array_1_Addr;
    public int[][] array_2_Addr;
    /*
    * value可能是：
    * 立即数的值
    *
    * 函数返回地址$pc
    * */
    public PcodeExecutor.termType termType;
    public int arrayDimension;
    public int array2RIGHT;
    public int array1Length;
    public int array2Left;
    public Term(PcodeExecutor.termType termType,int value)
    {
        this.value = value;
        this.termType = termType;
        /*
        * 初始化的时候，下面的值都设为0
        * */
        this.arrayDimension = 0;
        this.array2RIGHT = 0;
        this.array1Length = 0;
        this.array2Left = 0;
        this.array_1_Addr = null;
        this.array_2_Addr = null;
    }




}
