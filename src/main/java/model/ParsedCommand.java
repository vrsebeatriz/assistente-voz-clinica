package model;

public class ParsedCommand {

    private final CommandType type;
    private final String argument;
    private final String originalText;

    public ParsedCommand(CommandType type, String argument, String originalText) {
        this.type = type;
        this.argument = argument;
        this.originalText = originalText;
    }

    public CommandType getType() {
        return type;
    }

    public String getArgument() {
        return argument;
    }

    public String getOriginalText() {
        return originalText;
    }
}
