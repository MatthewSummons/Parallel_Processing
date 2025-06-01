package TwentyFourGame.Client.Parsing;

import java.util.ArrayList;

public class Lexer {
    private String input;
    private int position;
    private char currentChar;

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        if (input.length() > 0) {
            this.currentChar = input.charAt(0);
        } else {
            this.currentChar = '\0';
        }
    }

    private void advance() {
        position++;
        if (position >= input.length()) {
            currentChar = '\0';  // End of input
        } else {
            currentChar = input.charAt(position);
        }
    }

    private void skipWhitespace() {
        while (currentChar != '\0' && Character.isWhitespace(currentChar)) {
            advance();
        }
    }

    private int parseNumber() {
        StringBuilder sb = new StringBuilder();
        while (currentChar != '\0' && Character.isDigit(currentChar)) {
            sb.append(currentChar);
            advance();
        }
        
        return Integer.parseInt(sb.toString());
    }

    public ArrayList<Token> tokenize() throws IllegalArgumentException {
        ArrayList<Token> tokens = new ArrayList<>();
        
        while (currentChar != '\0') {
            if (Character.isWhitespace(currentChar)) {
                skipWhitespace();
                continue;
            }
            
            if (Character.isDigit(currentChar)) {
                int value = parseNumber();
                tokens.add(new Token(TokenType.NUMBER, value));
                continue;
            }
            
            switch (currentChar) {
                case 'a':
                case 'A':
                    tokens.add(new Token(TokenType.NUMBER, 1));
                    advance();
                    break;
                case 'j':
                case 'J':
                    tokens.add(new Token(TokenType.NUMBER, 11));
                    advance();
                    break;
                case 'q':
                case 'Q':
                    tokens.add(new Token(TokenType.NUMBER, 12));
                    advance();
                    break;                
                case 'k':
                case 'K':
                    tokens.add(new Token(TokenType.NUMBER, 13));
                    advance();
                    break;
                case '+':
                    tokens.add(new Token(TokenType.ADD, null));
                    advance();
                    break;
                case '-':
                    tokens.add(new Token(TokenType.SUB, null));
                    advance();
                    break;
                case '.':
                case 'x':
                case '*':
                    tokens.add(new Token(TokenType.MUL, null));
                    advance();
                    break;
                case '÷':
                case '/':
                    tokens.add(new Token(TokenType.DIV, null));
                    advance();
                    break;
                case '(':
                    tokens.add(new Token(TokenType.LPAREN, null));
                    advance();
                    break;
                case ')':
                    tokens.add(new Token(TokenType.RPAREN, null));
                    advance();
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognised Character: " + currentChar);
            }
        }
        
        tokens.add(new Token(TokenType.EOF, null));
        return tokens;
    }
}