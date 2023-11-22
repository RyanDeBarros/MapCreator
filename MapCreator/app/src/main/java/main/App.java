package main;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class App extends Application {

	private final TileList tileList = new TileList();
	private final Spinner spinX = new Spinner<Integer>(0, Integer.MAX_VALUE, 0),
			spinY = new Spinner<Integer>(0, Integer.MAX_VALUE, 0);
	private final Button open = new Button("Add image");
	private final FileChooser openImage = new FileChooser();
	private File openAt;
	private Pane displayStack = new Pane();
	private Pane displayTile = new Pane();
	private final Button remove = new Button("Remove"), move = new Button("Move"), cancel = new Button("Cancel");
	private Spinner spinPos = new Spinner<Integer>(0, Integer.MAX_VALUE, 0);
	private TileElement selectTile = null;
	private int selectId = -1;
	private Button toggleGreyBkg = new Button("Toggle grey BKG");
	private Button previewBtn = new Button("Preview");
	private static int previewCount = 1;
	private final ArrayList<Stage> previews = new ArrayList<>();

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		DirectoryChooser onStart = new DirectoryChooser();
		onStart.setTitle("Choose starting directory");
		openAt = onStart.showDialog(stage);

		stage.setOnCloseRequest(c -> {
			Platform.exit();
		});
		Pane menu = new Pane();
		menu.setPrefSize(1310, 450);
		displayStack.setPrefSize(1100, 300);
		displayStack.setLayoutX(190);
		displayStack.setLayoutY(100);
		Line li = new Line(170, 100, 170, 400);
		displayTile.setPrefSize(100, 300);
		displayTile.setLayoutX(50);
		displayTile.setLayoutY(100);
		menu.getChildren().addAll(displayStack, li, displayTile);

		int padding = 20;

		spinX.setLayoutX(padding);
		spinX.setLayoutY(20);
		spinX.setEditable(true);
		spinX.setPrefWidth(80);
		spinX.valueProperty().addListener(l -> displayTileStack());
		spinY.setLayoutX(spinX.getLayoutX() + spinX.getPrefWidth() + padding);
		spinY.setLayoutY(20);
		spinY.setEditable(true);
		spinY.setPrefWidth(80);
		spinY.valueProperty().addListener(l -> displayTileStack());
		open.setLayoutX(spinY.getLayoutX() + spinY.getPrefWidth() + padding);
		open.setLayoutY(20);
		open.setPrefWidth(90);
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

		remove.setLayoutX(open.getLayoutX() + open.getPrefWidth() + padding);
		remove.setLayoutY(20);
		remove.setTextAlignment(TextAlignment.CENTER);
		remove.setPrefWidth(90);
		spinPos.setLayoutX(remove.getLayoutX() + remove.getPrefWidth() + padding);
		spinPos.setLayoutY(20);
		spinPos.setPrefWidth(80);
		move.setLayoutX(spinPos.getLayoutX() + spinPos.getPrefWidth() + padding);
		move.setLayoutY(20);
		move.setTextAlignment(TextAlignment.CENTER);
		move.setPrefWidth(90);
		cancel.setLayoutX(move.getLayoutX() + move.getPrefWidth() + padding);
		cancel.setLayoutY(20);
		cancel.setTextAlignment(TextAlignment.CENTER);
		cancel.setPrefWidth(90);
		remove.setOnAction(a -> {
			selectTile.images.remove(selectId);
			if (selectTile.images.isEmpty()) {
				tileList.remove(selectId);
			}
			displayTileStack();
		});
		move.setOnAction(a -> {
			int to = (int) spinPos.getValue();
			if (to >= selectTile.images.size()) {
				to = selectTile.images.size() - 1;
			}
			Image img = selectTile.images.remove(selectId);
			selectTile.images.add(to, img);
			displayTileStack();
		});
		cancel.setOnMousePressed(m -> displayTileStack());
		menu.getChildren().addAll(remove, spinPos, move, cancel);

		toggleGreyBkg.setLayoutX(cancel.getLayoutX() + cancel.getPrefWidth() + padding);
		toggleGreyBkg.setLayoutY(20);
		toggleGreyBkg.setTextAlignment(TextAlignment.CENTER);
		toggleGreyBkg.setPrefWidth(120);
		Rectangle greyBkg = new Rectangle(displayStack.getPrefWidth(), displayStack.getPrefHeight(), Color.LIGHTGREY);
		greyBkg.setLayoutX(displayStack.getLayoutX());
		greyBkg.setLayoutY(displayStack.getLayoutY());
		greyBkg.setOpacity(0);
		toggleGreyBkg.setOnAction(a -> greyBkg.setOpacity(1 - greyBkg.getOpacity()));
		menu.getChildren().add(toggleGreyBkg);
		menu.getChildren().add(0, greyBkg);

		previewBtn.setLayoutX(toggleGreyBkg.getLayoutX() + toggleGreyBkg.getPrefWidth() + padding);
		previewBtn.setLayoutY(20);
		previewBtn.setTextAlignment(TextAlignment.CENTER);
		previewBtn.setPrefWidth(80);
		previewBtn.setOnAction(a -> {
			Pane preview = process(tileList.tileElements);
			if (!preview.getChildren().isEmpty()) {
				Stage previewStage = new Stage();
				previewStage.setScene(new Scene(preview));
				previewStage.setTitle("Preview #" + previewCount++);
				previews.add(previewStage);
				previewStage.setOnCloseRequest(c -> previews.remove(previewStage));
				previewStage.show();
			}
		});
		menu.getChildren().add(previewBtn);

		stage.setScene(new Scene(menu));
		stage.setTitle("Map Creator");
		stage.show();
		displayTileStack();
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
		displayTile.getChildren().clear();

		remove.setDisable(true);
		spinPos.setDisable(true);
		move.setDisable(true);
		cancel.setDisable(true);
		selectTile = null;
		selectId = -1;
		TileElement tileElement = tileList.get(new Coo((int) spinX.getValue(), (int) spinY.getValue()));
		if (null == tileElement) {
			return;
		}

		for (int i = 0; i < tileElement.images.size(); i++) {
			ImageView imgV = new ImageView(tileElement.images.get(i));
			imgV.setLayoutX(i * (TileElement.WIDTH + 10));
			imgV.setLayoutY(0.5 * (displayStack.getPrefHeight() - TileElement.HEIGHT));
			int j = i;
			imgV.setOnMousePressed(m -> imageSelect(tileElement, j));
			displayStack.getChildren().add(imgV);
		}
		for (int i = 0; i < tileElement.images.size(); i++) {
			ImageView imgV = new ImageView(tileElement.images.get(i));
			imgV.setLayoutX(0.5 * (displayTile.getPrefWidth() - TileElement.WIDTH));
			imgV.setLayoutY(0.5 * (displayTile.getPrefHeight() - TileElement.HEIGHT));
			displayTile.getChildren().add(imgV);
		}
	}

	private void imageSelect(TileElement tileElement, int i) {
		selectTile = tileElement;
		selectId = i;
		remove.setDisable(false);
		spinPos.setDisable(false);
		move.setDisable(false);
		cancel.setDisable(false);
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

		public void remove(int i) {
			coos.remove(i);
			tileElements.remove(i);
		}

	}

}
