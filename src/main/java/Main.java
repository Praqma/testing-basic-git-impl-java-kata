public class Main {

    private static Command parseCommand(String[] args) {
        if (args.length == 0) return new Command.NullCommand();
        else if ("init".equals((args[0]))) return new Command.Init(new FileSystem());
        else if ("commit".equals((args[0]))) return new Command.Commit(new FileSystem());
            // Exercise: Implement "help"
            // Exercise: Implement "log"
            // Exercise: Implement "hash-object"
            // Exercise: Implement "branches"
            // Exercise: Implement "(simple)tags"
            // Exercise: Implement "cat-file"
        else return new Command.NullCommand();
    }

    public static void main(String[] args) {
        Command cmd = parseCommand(args);
        cmd.execute();
    }
}
