package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class App extends Application {

	private final ArrayList<TileElement> tileElements = new ArrayList<>();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {

	}

	private static Pane process(ArrayList<TileElement> tileElements) {
		Pane pane = new Pane();
		for (TileElement tileElement : tileElements) {
			ImageView imgV = new ImageView(tileElement.image);
			imgV.setLayoutX(tileElement.x * TileElement.WIDTH);
			imgV.setLayoutY(tileElement.y * TileElement.HEIGHT);
			pane.getChildren().add(imgV);
//			if (pane.getPrefWidth() < imgV.getLayoutX() + TileElement.WIDTH) {
//				pane.setPrefWidth(imgV.getLayoutX() + TileElement.WIDTH);
//			}
//			if (pane.getPrefHeight() < imgV.getLayoutY() + TileElement.HEIGHT) {
//				pane.setPrefHeight(imgV.getLayoutY() + TileElement.HEIGHT);
//			}
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

}
