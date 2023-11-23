package main;

import com.google.common.io.Files;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class App extends Application {

	private TileList tileList = new TileList();
	private final Spinner spinX = new Spinner<Integer>(Integer.MIN_VALUE, Integer.MAX_VALUE, 0),
			spinY = new Spinner<Integer>(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
	private final Button open = new Button("Add image");
	private final FileChooser openImage = new FileChooser();
	private File openAt = null;
	private final Pane displayStack = new Pane();
	private final Pane displayTile = new Pane();
	private final Button remove = new Button("Remove"), move = new Button("Move"), cancel = new Button("Cancel");
	private final Spinner spinPos = new Spinner<Integer>(0, Integer.MAX_VALUE, 0);
	private TileElement selectTile = null;
	private int selectId = -1;
	private final Button toggleGreyBkg = new Button("Toggle grey BKG");
	private final Button previewBtn = new Button("Preview");
	private static int previewCount = 1;
	private final ArrayList<Stage> previews = new ArrayList<>();
	private final Button export = new Button("Export");
	private final Spinner spinTemplate = new Spinner<Integer>(0, Integer.MAX_VALUE, 0);
	private int templateId = 0;
	private final Button setTemplate = new Button("Set template"),
			insertTemplate = new Button("Insert template"),
			appendTemplate = new Button("Append template");
	private final HashMap<Integer, ArrayList<Image>> templates = new HashMap<>();
	private final Button clearTile = new Button("Clear tile");
	private final Button previewGridBtn = new Button("Preview grid");
	private final Spinner spinPreviewScale = new Spinner<Double>(0, Double.MAX_VALUE, 1);
	private double previewScale = 1;
	private final Button loadSession = new Button("Load session"), saveSession = new Button("Save session");

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		stage.setOnCloseRequest(c -> {
			Platform.exit();
		});
		Pane menu = new Pane();
		menu.setPrefSize(1310, 450);
		displayStack.setPrefSize(1100, 300);
		displayStack.setLayoutX(190);
		displayStack.setLayoutY(120);
		Line li = new Line(170, 100, 170, 400);
		displayTile.setPrefSize(100, 300);
		displayTile.setLayoutX(50);
		displayTile.setLayoutY(120);
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
			if (null != openAt && null != openAt.getParentFile()) {
				openImage.setInitialDirectory(openAt.getParentFile());
			}
			File imageFile = openImage.showOpenDialog(stage);
			if (null != imageFile && isPNG(imageFile)) {
				openAt = imageFile;
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
			Pane preview = process(tileList.tileElements, previewScale);
			displayPreview(preview);
		});
		export.setLayoutX(previewBtn.getLayoutX() + previewBtn.getPrefWidth() + padding);
		export.setLayoutY(20);
		export.setTextAlignment(TextAlignment.CENTER);
		export.setPrefWidth(70);
		export.setOnAction(a -> {
			Pane preview = process(tileList.tileElements, 1);
			if (!preview.getChildren().isEmpty()) {
				FileChooser exportAs = new FileChooser();
				exportAs.setTitle("Export image");
				exportAs.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image", "*.png"));
				File saveFile = exportAs.showSaveDialog(stage);
				if (null != saveFile) {
					try {
						if (saveFile.createNewFile()) {
							snapshot(preview, saveFile);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		menu.getChildren().addAll(previewBtn, export);

		spinTemplate.setLayoutX(padding);
		spinTemplate.setLayoutY(60);
		spinTemplate.setPrefWidth(80);
		spinTemplate.setEditable(true);
		spinTemplate.valueProperty().addListener(l -> {
			templateId = (int) spinTemplate.getValue();
		});
		setTemplate.setLayoutX(spinTemplate.getLayoutX() + spinTemplate.getPrefWidth() + padding);
		setTemplate.setLayoutY(spinTemplate.getLayoutY());
		setTemplate.setPrefWidth(90);
		setTemplate.setTextAlignment(TextAlignment.CENTER);
		insertTemplate.setLayoutX(setTemplate.getLayoutX() + setTemplate.getPrefWidth() + padding);
		insertTemplate.setLayoutY(setTemplate.getLayoutY());
		insertTemplate.setPrefWidth(100);
		insertTemplate.setTextAlignment(TextAlignment.CENTER);
		appendTemplate.setLayoutX(insertTemplate.getLayoutX() + insertTemplate.getPrefWidth() + padding);
		appendTemplate.setLayoutY(insertTemplate.getLayoutY());
		appendTemplate.setPrefWidth(110);
		appendTemplate.setTextAlignment(TextAlignment.CENTER);
		setTemplate.setOnAction(a -> {
			Coo coo = new Coo((int) spinX.getValue(), (int) spinY.getValue());
			if (tileList.coos.contains(coo)) {
				templates.put(templateId, new ArrayList(tileList.get(coo).images));
			}
		});
		insertTemplate.setOnAction(a -> {
			if (templates.containsKey(templateId)) {
				TileElement tile = getOrCreateCurrentTileElement();
				tile.images.clear();
				tile.images.addAll(templates.get(templateId));
				displayTileStack();
			}
		});
		appendTemplate.setOnAction(a -> {
			if (templates.containsKey(templateId)) {
				getOrCreateCurrentTileElement().images.addAll(templates.get(templateId));
				displayTileStack();
			}
		});
		menu.getChildren().addAll(spinTemplate, setTemplate, insertTemplate, appendTemplate);

		clearTile.setLayoutX(appendTemplate.getLayoutX() + appendTemplate.getPrefWidth() + padding);
		clearTile.setLayoutY(appendTemplate.getLayoutY());
		clearTile.setPrefWidth(80);
		clearTile.setTextAlignment(TextAlignment.CENTER);
		previewGridBtn.setLayoutX(clearTile.getLayoutX() + clearTile.getPrefWidth() + padding);
		previewGridBtn.setLayoutY(clearTile.getLayoutY());
		previewGridBtn.setPrefWidth(90);
		previewGridBtn.setTextAlignment(TextAlignment.CENTER);
		spinPreviewScale.setLayoutX(previewGridBtn.getLayoutX() + previewGridBtn.getPrefWidth() + padding);
		spinPreviewScale.setLayoutY(previewGridBtn.getLayoutY());
		spinPreviewScale.setPrefWidth(80);
		spinPreviewScale.setEditable(true);
		clearTile.setOnAction(a -> {
			Coo coo = new Coo((int) spinX.getValue(), (int) spinY.getValue());
			if (tileList.coos.contains(coo)) {
				TileElement tile = tileList.get(coo);
				tile.images.clear();
				tileList.remove(tileList.coos.indexOf(coo));
				displayTileStack();
			}
		});
		previewGridBtn.setOnAction(a -> {
			Pane preview = gridProcess(tileList.tileElements, previewScale);
			displayPreview(preview);
		});
		spinPreviewScale.valueProperty().addListener(l -> {
			previewScale = (double) spinPreviewScale.getValue();
		});
		menu.getChildren().addAll(clearTile, previewGridBtn, spinPreviewScale);

		loadSession.setLayoutX(spinPreviewScale.getLayoutX() + spinPreviewScale.getPrefWidth() + padding);
		loadSession.setLayoutY(spinPreviewScale.getLayoutY());
		loadSession.setTextAlignment(TextAlignment.CENTER);
		loadSession.setPrefWidth(100);
		saveSession.setLayoutX(loadSession.getLayoutX() + loadSession.getPrefWidth() + padding);
		saveSession.setLayoutY(loadSession.getLayoutY());
		saveSession.setTextAlignment(TextAlignment.CENTER);
		saveSession.setPrefWidth(100);
		loadSession.setOnAction(a -> {
			FileChooser load = new FileChooser();
			load.setTitle("Load session (.ser file)");
			File loadFile = load.showOpenDialog(stage);
			if (null != loadFile) {
				TileList loadList = Loader.load(loadFile);
				tileList = loadList;
				displayTileStack();
			}
		});
		saveSession.setOnAction(a -> {
			FileChooser save = new FileChooser();
			save.setTitle("Save session");
			save.getExtensionFilters().add(new FileChooser.ExtensionFilter("Serialized data file", "*.ser"));
			File saveFile = save.showSaveDialog(stage);
			if (null != saveFile) {
				Loader.save(tileList, saveFile);
			}
		});
		menu.getChildren().addAll(loadSession, saveSession);

		stage.setScene(new Scene(menu));
		stage.setTitle("Map Creator");
		stage.getIcons().add(new Image(getClass().getResource("/mapIcon.png").toExternalForm()));
		stage.show();
		displayTileStack();
	}

	private TileElement getOrCreateCurrentTileElement() {
		Coo coo = new Coo((int) spinX.getValue(), (int) spinY.getValue());
		TileElement tile;
		if (tileList.coos.contains(coo)) {
			tile = tileList.get(coo);
		} else {
			tile = new TileElement();
			tile.c = coo;
			tileList.add(tile);
		}
		return tile;
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

	private static Pane process(ArrayList<TileElement> tileElements, double scale) {
		Pane pane = new Pane();
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
		for (TileElement tileElement : tileElements) {
			double x = scale * tileElement.c.x * TileElement.WIDTH;
			double y = scale * tileElement.c.y * TileElement.HEIGHT;
			if (x < minX) {
				minX = x;
			}
			if (y < minY) {
				minY = y;
			}
			if (maxX < x + scale * TileElement.WIDTH) {
				maxX = x + scale * TileElement.WIDTH;
			}
			if (maxY < y + scale * TileElement.HEIGHT) {
				maxY = y + scale * TileElement.HEIGHT;
			}
			for (Image image : tileElement.images) {
				ImageView imgV = new ImageView(image);
				imgV.setLayoutX(x);
				imgV.setLayoutY(y);
				imgV.setFitWidth(scale * TileElement.WIDTH);
				imgV.setFitHeight(scale * TileElement.HEIGHT);
				pane.getChildren().add(imgV);
			}
		}
		for (Node n : pane.getChildren()) {
			n.setTranslateX(-minX);
			n.setTranslateY(-minY);
		}
		pane.setPrefSize(maxX - minX, maxY - minY);
		return pane;
	}

	private static Pane gridProcess(ArrayList<TileElement> tileElements, double scale) {
		Pane pane = new Pane();
		if (tileElements.isEmpty()) {
			return pane;
		}
		int rowHeight = 20, columnWidth = 20;
		int maxC = 0, maxR = 0;
		double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
		for (TileElement tileElement : tileElements) {
			double x = scale * tileElement.c.x * TileElement.WIDTH;
			double y = scale * tileElement.c.y * TileElement.HEIGHT;
			if (x < minX) {
				minX = x;
			}
			if (y < minY) {
				minY = y;
			}
			if (maxX < x + scale * TileElement.WIDTH) {
				maxX = x + scale * TileElement.WIDTH;
			}
			if (maxY < y + scale * TileElement.HEIGHT) {
				maxY = y + scale * TileElement.HEIGHT;
			}
			Rectangle r = new Rectangle(scale * TileElement.WIDTH, scale * TileElement.HEIGHT, Color.TRANSPARENT);
			r.setStroke(Color.BLACK);
			r.setStrokeType(StrokeType.CENTERED);
			r.setLayoutX(columnWidth + x);
			r.setLayoutY(rowHeight + y);
			pane.getChildren().add(r);
			for (Image image : tileElement.images) {
				ImageView imgV = new ImageView(image);
				imgV.setLayoutX(r.getLayoutX());
				imgV.setLayoutY(r.getLayoutY());
				imgV.setFitWidth(scale * TileElement.WIDTH);
				imgV.setFitHeight(scale * TileElement.HEIGHT);
				pane.getChildren().add(0, imgV);
			}
			if (tileElement.c.x > maxC) {
				maxC = tileElement.c.x;
			}
			if (tileElement.c.y > maxR) {
				maxR = tileElement.c.y;
			}
		}
		for (Node n : pane.getChildren()) {
			n.setTranslateX(-minX);
			n.setTranslateY(-minY);
		}
		Label columnTop[] = new Label[maxC + 1];
		Label rowLeft[] = new Label[maxR + 1];
		for (int i = 0; i < columnTop.length; i++) {
			columnTop[i] = new Label("" + i);
			columnTop[i].setLayoutX(columnWidth + (i + 0.45) * TileElement.WIDTH);
		}
		for (int i = 0; i < rowLeft.length; i++) {
			rowLeft[i] = new Label("" + i);
			rowLeft[i].setLayoutY(rowHeight + (i + 0.45) * TileElement.HEIGHT);
			rowLeft[i].setLayoutX(columnWidth * 0.35);
		}
		pane.getChildren().addAll(columnTop);
		pane.getChildren().addAll(rowLeft);
		pane.setPrefSize(maxX - minX + columnWidth + 10, maxY - minY + rowHeight + 10);
		return pane;
	}

	private static void snapshot(Pane previewPane, File file) {
		WritableImage img = previewPane.snapshot(null, null);
		try {
			ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void displayPreview(Pane preview) {
		if (!preview.getChildren().isEmpty()) {
			Stage previewStage = new Stage();
			Pane root = new Pane();
			root.getChildren().add(preview);
			previewStage.setScene(new Scene(root));
			previewStage.setTitle("Preview #" + previewCount++);
			previews.add(previewStage);
			previewStage.setOnCloseRequest(c -> previews.remove(previewStage));
			previewStage.show();
		}
	}

	public static class Coo implements Serializable {

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

	public static class TileElement implements Serializable {

		public transient ArrayList<Image> images = new ArrayList<>();
		public Coo c;
		public static final int WIDTH = 64, HEIGHT = 64;

		private void writeObject(ObjectOutputStream out) throws IOException {
			out.defaultWriteObject();

			int size = images.size();
			out.writeInt(size);
			for (Image image : images) {
				byte[] imageData = serializeImage(image);
				out.writeInt(imageData.length);
				out.write(imageData);
			}
		}

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();

			int size = in.readInt();
			images = new ArrayList<>(size);
			for (int i = 0; i < size; i++) {
				int length = in.readInt();
				byte[] imageData = new byte[length];
				in.readFully(imageData);
				Image image = deserializeImage(imageData);
				images.add(image);
			}
		}

		private byte[] serializeImage(Image image) throws IOException {
			BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "png", baos);
			return baos.toByteArray();
		}

		private Image deserializeImage(byte[] imageData) throws IOException {
			ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
			return new Image(bais);
		}

	}

	public static class TileList implements Serializable {

		private final ArrayList<Coo> coos = new ArrayList<>();
		private final ArrayList<TileElement> tileElements = new ArrayList<>();

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
