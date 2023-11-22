package main;

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

	private final ArrayList<TileElement> tileElements = new ArrayList<>();
	private final Spinner spinX = new Spinner<Integer>(0, Integer.MAX_VALUE, 0),
			spinY = new Spinner<Integer>(0, Integer.MAX_VALUE, 0);
	private final Button open = new Button();
	private final FileChooser openImage = new FileChooser();
	private File openAt;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		DirectoryChooser onStart = new DirectoryChooser();
		openAt = onStart.showDialog(stage);

		Pane menu = new Pane();
		menu.setPrefSize(1200, 400);
		spinX.setLayoutX(20);
		spinX.setLayoutY(20);
		spinX.setEditable(true);
		spinX.setPrefWidth(80);
		spinY.setLayoutX(120);
		spinY.setLayoutY(20);
		spinY.setEditable(true);
		spinY.setPrefWidth(80);
		open.setLayoutX(220);
		open.setLayoutY(20);
		open.setPrefWidth(90);
		open.setText("Add image");
		open.setTextAlignment(TextAlignment.CENTER);
		open.setOnAction(a -> {
			openImage.setInitialDirectory(openAt);
			File imageFile = openImage.showOpenDialog(stage);
			if (null != imageFile) {
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

	private void addImage(Image img, int x, int y) {

	}

	private static Pane process(ArrayList<TileElement> tileElements) {
		Pane pane = new Pane();
		for (TileElement tileElement : tileElements) {
			for (Image image : tileElement.images) {
				ImageView imgV = new ImageView(image);
				imgV.setLayoutX(tileElement.x * TileElement.WIDTH);
				imgV.setLayoutY(tileElement.y * TileElement.HEIGHT);
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

}
