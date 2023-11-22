package main;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class App extends Application {

	private final TileList tileList = new TileList();
	private final Spinner spinX = new Spinner<Integer>(0, Integer.MAX_VALUE, 0),
			spinY = new Spinner<Integer>(0, Integer.MAX_VALUE, 0);
	private final Button open = new Button();
	private final FileChooser openImage = new FileChooser();
	private File openAt;
	private Pane displayStack = new Pane();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		DirectoryChooser onStart = new DirectoryChooser();
		openAt = onStart.showDialog(stage);

		Pane menu = new Pane();
		menu.setPrefSize(1200, 450);
		displayStack.setPrefSize(1100, 300);
		displayStack.setLayoutX(50);
		displayStack.setLayoutY(100);
		menu.getChildren().add(displayStack);

		spinX.setLayoutX(20);
		spinX.setLayoutY(20);
		spinX.setEditable(true);
		spinX.setPrefWidth(80);
		spinX.valueProperty().addListener(l -> displayTileStack());
		spinY.setLayoutX(120);
		spinY.setLayoutY(20);
		spinY.setEditable(true);
		spinY.setPrefWidth(80);
		spinY.valueProperty().addListener(l -> displayTileStack());
		open.setLayoutX(220);
		open.setLayoutY(20);
		open.setPrefWidth(90);
		open.setText("Add image");
		open.setTextAlignment(TextAlignment.CENTER);
		open.setOnAction(a -> {
			openImage.setInitialDirectory(openAt);
			File imageFile = openImage.showOpenDialog(stage);
			if (null != imageFile && isPNG(imageFile)) {
				openAt = imageFile.getParentFile();
				Image image = new Image(imageFile.getAbsolutePath());
				addImage(image, (int) spinX.getValue(), (int) spinY.getValue());
			}
		});
		menu.getChildren().addAll(spinX, spinY, open);
		stage.setScene(new Scene(menu));
		stage.setTitle("Map Creator");
		stage.show();
	}

	private static boolean isPNG(File file) {
		return Files.getFileExtension(file.getAbsolutePath()).equals("png");
	}

	private void addImage(Image img, int x, int y) {
		TileElement tileElement = tileList.get(new Coo(x, y));
		if (null == tileElement) {
			tileElement = new TileElement();
			tileElement.c = new Coo(x, y);
			if (!tileList.add(tileElement)) {
				System.err.println("TileElement " + tileElement + " already has coordinates in tileList.");
				System.exit(1);
			}
		}
		tileElement.images.add(img);
		displayTileStack();
	}

	private void displayTileStack() {
		displayStack.getChildren().clear();
		TileElement tileElement = tileList.get(new Coo((int) spinX.getValue(), (int) spinY.getValue()));
		if (null == tileElement) {
			return;
		}
		for (int i = 0; i < tileElement.images.size(); i++) {
			ImageView imgV = new ImageView(tileElement.images.get(i));
			imgV.setLayoutX(i * (TileElement.WIDTH + 10));
			imgV.setLayoutY(0.5 * (displayStack.getPrefHeight() - TileElement.HEIGHT));
			displayStack.getChildren().add(imgV);
		}
	}

	private static Pane process(ArrayList<TileElement> tileElements) {
		Pane pane = new Pane();
		for (TileElement tileElement : tileElements) {
			for (Image image : tileElement.images) {
				ImageView imgV = new ImageView(image);
				imgV.setLayoutX(tileElement.c.x * TileElement.WIDTH);
				imgV.setLayoutY(tileElement.c.y * TileElement.HEIGHT);
				pane.getChildren().add(imgV);
//				if (pane.getPrefWidth() < imgV.getLayoutX() + TileElement.WIDTH) {
//					pane.setPrefWidth(imgV.getLayoutX() + TileElement.WIDTH);
//				}
//				if (pane.getPrefHeight() < imgV.getLayoutY() + TileElement.HEIGHT) {
//					pane.setPrefHeight(imgV.getLayoutY() + TileElement.HEIGHT);
//				}
			}
		}
		return pane;
	}

	private static void snapshot(Pane previewPane, File file) {
//		SnapshotParameters snapshot = new SnapshotParameters();
//		snapshot.setViewport(new javafx.geometry.Rectangle2D(0, 0, previewPane.getPrefWidth(), previewPane.getPrefHeight()));
//		WritableImage img = previewPane.snapshot(snapshot, null);
		WritableImage img = previewPane.snapshot(null, null);
		try {
			ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static class Coo {

		public final int x, y;

		public Coo(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object obj) {
			return null != obj && (obj == this || (obj instanceof Coo coo && coo.x == this.x && coo.y == this.y));
		}
	}

	public static class TileList {

		private ArrayList<Coo> coos = new ArrayList<>();
		private ArrayList<TileElement> tileElements = new ArrayList<>();

		public TileElement get(Coo coo) {
			for (int i = 0; i < coos.size(); i++) {
				if (coos.get(i).equals(coo)) {
					return tileElements.get(i);
				}
			}
			return null;
		}

		public boolean add(TileElement tileElement) {
			if (null != get(tileElement.c)) {
				return false;
			}
			coos.add(tileElement.c);
			tileElements.add(tileElement);
			return true;
		}

	}

}
