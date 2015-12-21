package chip;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Chip {

	private char[] memory;
	private char[] V;
	private char I;
	private char pc;
	
	private char stack[];
	private int stackPointer;
	
	private int delayTimer;
	private int soundTimer;
	
	private byte[] keys;
	
	private byte[] display;
	
	private boolean needRedraw;
	
	public void init() {
		memory = new char[4096];
		V = new char[16];
		I = 0x0;
		pc = 0x200;
		
		stack = new char[16];
		stackPointer = 0;
		
		delayTimer = 0;
		soundTimer = 0;
		
		keys = new byte[16];
		
		display = new byte[64 * 32];
		
		needRedraw = false;
	}
	
	public void run() {
		// fetch opcode
		char opcode = (char) ((memory[pc] << 8) | memory[pc + 1]);
		System.out.println(Integer.toHexString(opcode));
		
		// decode opcode
		switch (opcode & 0xF000) {
		
		case 0x0000: {
			switch (opcode & 0x00FF) {
			case 0x00E0: { // 00E0 - Clears the display.
				break;
			}
				
			case 0x00EE: { // 00EE - Return from a subroutine.
				break;
			}
				
			default: {
				System.err.println("Unsupported 0xxx Opcode.");
				System.exit(0);
			} // default
			
			}
			break;
		}
			
		case 0x1000: { // 1NNN - Jump to location NNN.
			break;
		}
			
		case 0x2000: { // 2NNN - Call subroutine at NNN.
			stack[stackPointer] = pc;
			
			stackPointer++;
			
			pc = (char) (opcode & 0x0FFF);
			
			break;
		}
			
		case 0x3000: { // 3XNN - Skip next instruction if VX = NN.
			break;
		}
			
		case 0x4000: { // 4XNN - Skip next instruction if VX != NN.
			break;
		}
			
		case 0x5000: { // 5XY0 - Skip next instruction if VX = VY.
			break;
		}
			
		case 0x6000: { // 6XNN - Set VX = NN.
			int X  = (opcode & 0x0F00) >> 8;
		
			V[X] = (char) (opcode & 0x00FF);
			
			pc += 2;
			
			break;
		}
			
		case 0x7000: { // 7XNN - Set VX = VX + NN
			int X = (opcode & 0x0F00) >> 8;
			int NN = (opcode & 0x00FF);
			
			V[X] = (char) ((V[X] + NN) & 0xFF);
			
			pc += 2;
			
			break;
		}
		
		case 0x8000: // Contains more data in the last nibble.
		{
			switch (opcode & 0x000F) {
			case 0x0000: { // 8XY0 - Set VX = VY.
				break;
			}
				
			case 0x0001: { // 8XY1 - Set VX = VX OR VY.
				break;
			}
				
			case 0x0002: { // 8XY2 - Set VX = VX AND VY.
				break;
			}
				
			case 0x0003: { // 8XY3 - Set VX = VX XOR VY.
				break;
			}
				
			case 0x0004: { // 8XY4 - Set VX = VX + VY, set VF = carry.
				break;
			}
				
			case 0x0005: { // 8XY5 - Set VX = VX - VY, set VF = NOT borrow.
				break;
			}
				
			case 0x0006: { // 8XY6 - Set VX = VX SHR 1.
				break;
			}
				
			case 0x0007: { // 8XY7 - Set VX = VY - VX, set VF = NOT borrow.
				break;
			}
				
			case 0x000E: { // 8XYE - Set VX = VX SHL 1.
				break;
			}
				
			default: {
				System.err.println("Unsupported 8xxx Opcode.");
				System.exit(0);
			} // default
			
			}
			break;
		}
			
		case 0x9000: { // 9XY0 - Skip next instruction if VX != VY.
			break;
		}
			
		case 0xA000: { // ANNN - Set I = NNN.
			
			I = (char) (opcode & 0x0FFF);
			
			pc += 2;
			
			break;
		}
			
		case 0xB000: { // BNNN - Jump to location NNN + V0.
			break;
		}
			
		case 0xC000: { // CXNN - Set VX = random byte AND NN.
			break;
		}
			
		case 0xD000: { // DXYN - Display n-byte sprite starting at memory location I at (VX, VY), set VF = collision.
			int X = (opcode & 0x0F00) >> 8;
			int Y = (opcode & 0x00F0) >> 4;
			int N = (opcode & 0x000F);
			
			int x = V[X];
			int y = V[Y];
			int height = N;
			
			V[0xF] = 0;
			
			for (int _y = 0; _y < height; _y++) {
				
				int line = memory[I + _y];
				
				for (int _x = 0; _x < 8; _x++) {
					
					int pixel = line & (0x80 >> _x);
					
					if (pixel != 0) {
						
						int totalX = x + _x;
						int totalY = y + _y;
						int index = totalY * 64 + totalX;
						
						if (display[index] == 1) {
							V[0xF] = 1;
						}
						
						display[index] ^= 1;
						
					}
					
				}
			}
			
			pc += 2;
			
			needRedraw = true;
			
			break;
		}
			
		case 0xE000: {
			switch (opcode & 0x00FF) {
			case 0x009E: { // EX9E - Skip next instruction if key with the value of VX is pressed.
				break;
			}
				
			case 0x00A1: { // EXA1 - Skip next instruction if key with the value of VX is not pressed.
				break;
			}
				
			default: {
				System.err.println("Unsupported  Exxx Opcode.");
				System.exit(0);
			} //default
				
			}
			break;
		}
			
		case 0xF000: {
			switch (opcode & 0x00FF) {
			case 0x0007: { // FX07 - Set VX = delay timer value.
				break;
			}
				
			case 0x000A: { // FX0A - Wait for a key press, store the value of the key in VX.
				break;
			}
				
			case 0x0015: { // FX15 - Set delay timer = VX.
				break;
			}
				
			case 0x0018: { // FX18 - Set sound timer = VX.
				break;
			}
				
			case 0x001E: { // FX1E - Set I = I + VX.
				break;
			}
				
			case 0x0029: { // FX29 - Set I = location of sprite for digit VX.
				break;
			}
				
			case 0x0033: { // FX33 - Store BCD representation of VX in memory locations I, I + 1, and I + 2.
				break;
			}
				
			case 0x0055: { // FX55 - Store registers V0 through VX in memory starting at location I.
				break;
			}
				
			case 0x0065: { // FX65 - Read registers V0 through VX from memory starting at location I.
				break;
			}
				
			default: {
				System.err.println("Unsupported Fxxx Opcode.");
				System.exit(0);
			} // default
			
			}
			break;
		}
			
		default: {
			System.err.println("Unsupported Opcode.");
			System.exit(0);
		} // default
		
		}
			// execute opcode
	}
	
	/**
	 * Returns the display data
	 * @return display
	 * Current state of the 64x32 display
	 */
	public byte[] getDisplay() {
		return display;
	}

	/**
	 * Loads the program into memory
	 * @param file
	 * @throws IOException
	 */
	public void loadProgam(String file) throws IOException {
		DataInputStream input = null;
		try {
			input = new DataInputStream(new FileInputStream(new File(file)));
			
			int offset = 0;
			while (input.available() > 0) {
				memory[0x200 + offset] = (char) (input.readByte() & 0xFF);
				offset++;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}
	
	/**
	 * Loads the fontset into memory
	 */
	public void loadFontset() {
		for (int i = 0; i < ChipData.fontset.length; i++) {
			memory[0x50 + i] = (char) (ChipData.fontset[i] & 0xFF);
		}
	}

	/**
	 * Checks if there is a redraw needed
	 * @return needRedraw
	 */
	public boolean needsRedraw() {
		return needRedraw;
	}

	/**
	 * Notify the chip that is has been redraw
	 */
	public void removeDrawFlag() {
		needRedraw = false;
	}
	
}
