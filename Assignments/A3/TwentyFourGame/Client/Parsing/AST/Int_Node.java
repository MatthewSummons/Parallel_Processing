package TwentyFourGame.Client.Parsing.AST;

public class Int_Node extends AST_Node{
    public final int value;

    public Int_Node(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
