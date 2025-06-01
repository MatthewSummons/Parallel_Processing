package TwentyFourGame.Client.Parsing;

final public class Token {
    private TokenType type;
    private Object value;

    public Token(TokenType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "(" + type + ", " + value + ")";
    }
}