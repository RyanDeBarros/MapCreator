package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import main.App.TileList;

public class Loader {

	public static File file(String rsrc) {
		return new File(Loader.class.getResource(rsrc).toExternalForm());
	}

	public static TileList load(File file) {
		try {
			try (FileInputStream is = new FileInputStream(file); ObjectInputStream in = new ObjectInputStream(is);) {
				return (TileList) in.readObject();
			}
		} catch (java.io.StreamCorruptedException e) {
			return null;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(2);
			return null;
		}
	}

	public static void save(TileList tileList, File file) {
		try {
			try (FileOutputStream os = new FileOutputStream(file); ObjectOutputStream out = new ObjectOutputStream(os);) {
				out.writeObject(tileList);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}

}
