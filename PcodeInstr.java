public class PcodeInstr {
    public PcodeExecutor.instrName instrName;
    public Object instrObj;

    public PcodeInstr(PcodeExecutor.instrName instrName, Object instrObj)
    {
        this.instrName = instrName;
        this.instrObj = instrObj;
    }

}
