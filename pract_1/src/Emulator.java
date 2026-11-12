import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;

import cmd.Command;
import cmd.CommandMap;
import cmd.State;
import dirfs.VirtualFs;

public class Emulator {
    private final VirtualFs fs;

    Emulator(String vfsPath) throws VirtualFs.VfsException {
        fs = new VirtualFs(vfsPath);
    }

    public void run(String scriptPath) {
        InputStream stream = System.in;
        if (scriptPath != null) {
            try {
                stream = new FileInputStream(scriptPath);
            } catch (FileNotFoundException e) {
                System.out.println("Script file not found");
            }
        }
        Scanner sc = new Scanner(stream);

        State state = new State(fs, fs.getRoot(), stream, System.out, sc);

        int lastExitCode = 0;
        for (; ; ) {
            if (stream != System.in &&
                    (!sc.hasNextLine() || lastExitCode != 0)) {
                if (lastExitCode != 0) {
                    System.out.println("An error occurred, stopping script");
                }

                // Continue input from stdin
                stream = System.in;
                sc.close();
                sc = new Scanner(System.in);
            }

            System.out.printf("%s ", fs.getFsName());
            if (lastExitCode != 0) {
                System.out.printf("%d|", lastExitCode);
            }
            System.out.printf("%s$ ", state.getCwd().getPath());

            String line = sc.nextLine();
            if (stream != System.in) {
                System.out.println(line);
            }

            String[] parsedArgs = ArgParser.parseArgs(line);
            if (parsedArgs.length == 0) {
                continue;
            }

            if (Objects.equals(parsedArgs[0], "exit")) {
                break;
            }

            Command cmd = CommandMap.get(parsedArgs[0]);
            if (cmd == null) {
                System.out.printf("%s: command not found\n", parsedArgs[0]);
                continue;
            }

            try {
                lastExitCode = cmd.run(parsedArgs, state);
            } catch (Exception e) {
                System.out.println("Unexpected exception");
                e.printStackTrace();
                lastExitCode = 255;
            }
        }

        sc.close();
        try {
            if (stream != System.in) {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}