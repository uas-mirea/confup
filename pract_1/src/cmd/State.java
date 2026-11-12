package cmd;

import dirfs.VirtualFs;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class State {
    private final VirtualFs fs;
    private VirtualFs.File cwd;

    private final InputStream stdin;
    private final PrintStream stdout;
    private final Scanner stdinScanner;

    public State(
            VirtualFs fs, VirtualFs.File cwd,
            InputStream stdin, PrintStream stdout, Scanner stdinScanner) {
        this.fs = fs;
        this.cwd = cwd;
        this.stdin = stdin;
        this.stdout = stdout;
        this.stdinScanner = stdinScanner;
    }

    public VirtualFs getFs() {
        return fs;
    }

    public VirtualFs.File getCwd() {
        return cwd;
    }

    public InputStream getStdin() {
        return stdin;
    }

    public PrintStream getStdout() {
        return stdout;
    }

    public Scanner getStdinScanner() {
        return stdinScanner;
    }

    public void setCwd(VirtualFs.File dir) {
        if (!dir.isDirectory()) {
            throw new RuntimeException("CWD must be a directory");
        }
        cwd = dir;
    }
}