import dirfs.VirtualFs;

public class Main {
    static public void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide path to VFS as an argument");
            return;
        }

        Emulator emu;
        try {
            emu = new Emulator(args[0]);
        } catch (VirtualFs.VfsException e) {
            System.out.println(e.getMessage());
            return;
        }

        emu.run(args.length >= 2 ? args[1] : null);
    }
}