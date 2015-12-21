package emu;

import java.io.IOException;

import chip.Chip;

public class Main extends Thread {
	
	private Chip chip8;
	private ChipFrame frame;
	
	
	public Main() throws IOException {
		chip8 = new Chip();
		chip8.init();
		chip8.loadProgam("./pong2.hex");
		frame = new ChipFrame(chip8);
	}
	
	public void run() {
		// 60 hz
		while (true) {
			chip8.run();
			if (chip8.needsRedraw()) {
				frame.repaint();
				chip8.removeDrawFlag();
			}
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				// Unthrown exception
			}
		}
	}

	public static void main(String[] args) throws IOException {
		Main main = new Main();
		main.start();
	}

}
