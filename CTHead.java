import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.scene.input.MouseEvent;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.ScrollPane;

/*
 * @author Vlad Stejeroiu
 */

public class CTHead extends Application {
	short cthead[][][]; // store the 3D volume data set
	short min, max; // min/max value in the 3D volume data set
	// sliders to step through the slices (z and y directions) (remember 113 slices
	// in z direction 0-112)
	Slider zslider = new Slider(0, 112, 0);
	Slider yslider = new Slider(0, 255, 0);
	Slider xslider = new Slider(0, 255, 0);
	Slider SIZEslider = new Slider(0, 499, 0); // slider for the resized image

	// variables to check if histogram equalization is turned on or not
	private boolean histogramTopOn = false;
	private boolean histogramFrontOn = false;
	private boolean histogramSideOn = false;
	WritableImage medical_image4;

	@Override
	public void start(Stage stage) throws FileNotFoundException, IOException {
		stage.setTitle("CThead Viewer");

		ReadData();

		int width = 256;
		int height = 256;
		WritableImage medical_image1 = new WritableImage(width, height);
		ImageView imageView1 = new ImageView(medical_image1);
		WritableImage medical_image2 = new WritableImage(width, height);
		ImageView imageView2 = new ImageView(medical_image2);
		WritableImage medical_image3 = new WritableImage(width, height);
		ImageView imageView3 = new ImageView(medical_image3);
		medical_image4 = medical_image1;
		ImageView imageView4 = new ImageView(medical_image4);

		Button mip_button1 = new Button("MIP"); // an example button to switch to MIP mode
		Button mip_button2 = new Button("MIP2");
		Button mip_button3 = new Button("MIP3");
		Button 	histogram1 = new Button("Histogram Equalization Top");
		Button 	histogram2 = new Button("Histogram Equalization Front");
		Button histogram3 = new Button("Histogram Equaliation Side");

		// customize the sliders to look a bit better
		xslider.setShowTickLabels(true);
		xslider.setShowTickMarks(true);
		xslider.setMajorTickUnit(50);
		xslider.setMinorTickCount(5);
		yslider.setShowTickLabels(true);
		yslider.setShowTickMarks(true);
		yslider.setMajorTickUnit(50);
		yslider.setMinorTickCount(5);
		zslider.setShowTickLabels(true);
		zslider.setShowTickMarks(true);
		zslider.setMajorTickUnit(20);
		zslider.setMinorTickCount(5);
		SIZEslider.setShowTickLabels(true);
		SIZEslider.setShowTickMarks(true);
		SIZEslider.setMajorTickUnit(50);
		SIZEslider.setMinorTickCount(5);

		// Actions for the three histogram buttons, one for each view
		// when pressed calls the histogram equalization function
		// when pressed again turns off the histogram equalization.
		histogram1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (histogramTopOn == false) {
					histogramTopOn = true;
					histogram1.setStyle("-fx-background-color: Blue");
					histogramEQ(medical_image1, zslider.valueProperty().intValue(), 1);
				} else {
					histogramTopOn = false;
					histogram1.setStyle("-fx-background-color: DarkGrey");
					MIP(medical_image1, zslider.valueProperty().intValue(), 1);
				}
			}
		});
		histogram2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (histogramFrontOn == false) {
					histogramFrontOn = true;
					histogram2.setStyle("-fx-background-color: Blue");
					histogramEQ(medical_image2, yslider.valueProperty().intValue(), 2);
				} else {
					histogramFrontOn = false;
					histogram2.setStyle("-fx-background-color: DarkGrey");
					MIP(medical_image2, yslider.valueProperty().intValue(), 2);
				}
			}
		});
		histogram3.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (histogramSideOn == false) {
					histogramSideOn = true;
					histogram3.setStyle("-fx-background-color: Blue");
					histogramEQ(medical_image3, xslider.valueProperty().intValue(), 3);
				} else {
					histogramSideOn = false;
					histogram3.setStyle("-fx-background-color: DarkGrey");
					MIP(medical_image3, xslider.valueProperty().intValue(), 3);
				}
			}
		});

		// Actions for the three MIP buttons, one for each view
		// when pressed activate the maximum intensity projection function.
		mip_button1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				mip_button1.setStyle("-fx-background-color: Red");
				viewMIP(medical_image1, 1);
			}
		});

		mip_button2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				mip_button2.setStyle("-fx-background-color: Red");
				viewMIP(medical_image2, 2);
			}
		});

		mip_button3.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				mip_button3.setStyle("-fx-background-color: Red");
				viewMIP(medical_image3, 3);
			}
		});
		// action when moving slider for top view
		zslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (histogramTopOn) {
					histogramEQ(medical_image1, newValue.intValue(), 1);
				} else {
					mip_button1.setStyle("-fx-background-color: DarkGray");
					MIP(medical_image1, newValue.intValue(), 1);
				}
				// System.out.println(newValue.intValue());
			}
		});
		//// action when moving slider for front view
		yslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (histogramFrontOn) {
					histogramEQ(medical_image2, newValue.intValue(), 2);
				} else {
					mip_button2.setStyle("-fx-background-color: DarkGray");
					MIP(medical_image2, newValue.intValue(), 2);
					// System.out.println(newValue.intValue());
				}
			}
		});
		// action when moving slider for side view
		xslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (histogramSideOn) {
					histogramEQ(medical_image3, newValue.intValue(), 3);
				} else {
					mip_button3.setStyle("-fx-background-color: DarkGray");
					MIP(medical_image3, newValue.intValue(), 3);
					// System.out.println(newValue.intValue());
				}
			}
		});

		// A FlowPane for each view(top, front, side) and one for the resized image 
		// containing the respective image for that view,
		// a slider and the button for switching to histogram equalization.
		FlowPane extras = new FlowPane();
		extras.setAlignment(Pos.CENTER);
		extras.setHgap(30);
		extras.getChildren().addAll(new Label("Resize: "), imageView4, SIZEslider);

		// action when moving the slider for the resized image
		SIZEslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				medical_image4 = resizeB(medical_image4, SIZEslider.valueProperty().intValue());
				extras.getChildren().set(1, new ImageView(medical_image4));
				// System.out.println(newValue.intValue());
			}
		});

		FlowPane top = new FlowPane();
		top.setAlignment(Pos.CENTER);
		top.setHgap(10);
		top.getChildren().addAll(new Label("Top View: "), imageView1, mip_button1, zslider, histogram1);
		FlowPane front = new FlowPane();
		front.setAlignment(Pos.CENTER);
		front.setHgap(10);
		front.getChildren().addAll(new Label("Front View: "), imageView2, mip_button2, yslider, histogram2);
		FlowPane side = new FlowPane();
		side.setAlignment(Pos.CENTER);
		side.setHgap(10);
		side.getChildren().addAll(new Label("Side View: "), imageView3, mip_button3, xslider, histogram3);
		
		// Paired top with resize, and front with side, into two HBoxes
		// Then put them into a VBox that will be displayed
		HBox box = new HBox();
		box.setSpacing(1);
		box.getChildren().addAll(top, extras);
		
		HBox sides = new HBox();
		sides.setSpacing(1);
		sides.getChildren().addAll(front, side);
		
		VBox root = new VBox();
		root.setSpacing(1);
		root.getChildren().addAll(box,sides);

		/// Making a scroll pane which will contain the thumbnails.
		ScrollPane scr = new ScrollPane();
		scr.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);
		scr.setFitToWidth(true);
		scr.setContent(Thumbnails(medical_image1));
		root.getChildren().add(scr);

		Scene scene = new Scene(root, 1000, 1000);
		stage.setScene(scene);
		stage.show();
		scr.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				/// scr.getHvalue() returns how much the user moved
				/// the horizontal bar of the scroll pane.

				MIP(medical_image1, rounding(event, 100, Math.floor(scr.getHvalue() * 100) / 100), 1);
				HBox hbox = new HBox();
				hbox.getChildren().add(new ImageView(medical_image1));
				Scene scene1 = new Scene(hbox, 256, 256);
				stage.setScene(scene1);

				hbox.setOnMouseClicked(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent arg0) {
						stage.setScene(scene);
					}

				});
			}
		});
	}
	/**
	 * Function read from CTHead data set.
	 * @throws IOException
	 */
	public void ReadData() throws IOException {
		File file = new File("CThead");
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

		int i, j, k;

		min = Short.MAX_VALUE;
		max = Short.MIN_VALUE;
		short read;
		int b1, b2;
		cthead = new short[113][256][256];
		for (k = 0; k < 113; k++) {
			for (j = 0; j < 256; j++) {
				for (i = 0; i < 256; i++) {
					b1 = ((int) in.readByte()) & 0xff;
					b2 = ((int) in.readByte()) & 0xff;
					read = (short) ((b2 << 8) | b1);
					if (read < min)
						min = read;
					if (read > max)
						max = read;
					cthead[k][j][i] = read;
				}
			}
		}
		in.close();
	}

	/**
	 * Method to see the slices from the top view perspective.
	 * This function shows how to carry out an operation on an image. It obtains the
	 * dimensions of the image, and then loops through the image carrying out the
	 * copying of a slice of data into the image.
	 * 
	 * @param image The image shown by moving a slider.
	 * @param slice Variable for the slider to change the slice.
	 * @param view The variable for deciding the view, 1 for top, 2 for front and 3 for side.
	 */
	public void MIP(WritableImage image, int slice, int view) {
		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;
		// slices through the top view
		if (view == 1) {
			for (j = 0; j < h; j++) {
				for (i = 0; i < w; i++) {
					datum = cthead[slice][j][i];
					col = (((float) datum - (float) min) / ((float) (max - min)));
					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
				}
			}
			// slices through the front view
		} else if (view == 2) {
			for (j = 0; j < 113; j++) {
				for (i = 0; i < w; i++) {
					datum = cthead[j][slice][i];
					col = (((float) datum - (float) min) / ((float) (max - min)));
					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
				}
			}
			// slices through the side view
		} else if (view == 3) {
			for (j = 0; j < 113; j++) {
				for (i = 0; i < h; i++) {
					datum = cthead[j][i][slice];
					col = (((float) datum - (float) min) / ((float) (max - min)));
					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
				}
			}
		} else {
			System.out.println("Image error!");
		}
	}

	/**
	 * Method implementing the maximum intensity projection.
	 * 
	 * @param image The image on which MIP is performed.
	 * @param MIPview The variable for deciding the view, 1 for top, 2 for front and 3 for side.
	 */
	public void viewMIP(WritableImage image, int MIPview) {
		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, k;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;
		short myMaximum;
		// MIP for the top view
		if (MIPview == 1) {
			for (j = 0; j < h; j++) {
				for (i = 0; i < w; i++) {
					myMaximum = min;
					for (k = 0; k < 113; k++) {
						datum = cthead[k][j][i];
						if (myMaximum < datum)
							myMaximum = datum;
						col = (((float) myMaximum - (float) min) / ((float) (max - min)));
						image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
					}
				}
			}
			// MIP for the front view
		} else if (MIPview == 2) {
			for (j = 0; j < 113; j++) {
				for (i = 0; i < w; i++) {
					myMaximum = min;
					for (k = 0; k < 256; k++) {
						datum = cthead[j][k][i];
						if (myMaximum < datum)
							myMaximum = datum;
						col = (((float) myMaximum - (float) min) / ((float) (max - min)));
						image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
					}
				}
			}
			// MIP for the side view
		} else if (MIPview == 3) {
			for (j = 0; j < 113; j++) {
				for (i = 0; i < h; i++) {
					myMaximum = min;
					for (k = 0; k < 256; k++) {
						datum = cthead[j][i][k];
						if (myMaximum < datum)
							myMaximum = datum;
						col = (((float) myMaximum - (float) min) / ((float) (max - min)));
						image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
					}
				}
			}
		} else {
			System.out.println("MIP error!");
		}
	}

	/**
	 * Method for performing histogram equalization on the data set.
	 * 
	 * @param image The image for a new slice view on which histogram equalization
	 *              is performed.
	 * @param slice Variable for the slider to change the slice.
	 * @param histogramView The variable for deciding the view, 1 for top, 2 for front and 3 for side.
	 */
	public void histogramEQ(WritableImage image, int slice, int histogramView) {
		int w = (int) image.getWidth(), h = (int) image.getHeight(), i, j, k;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;
		int index;
		int histogram[] = new int[max - min + 1];
		float mapping[] = new float[max - min + 1];
		int t[] = new int[max - min + 1];
		float Size = 7405568;

		// initialized the to 0 for all indexes
		for (int p = 0; p < max - min + 1; p++)
			histogram[p] = 0;
		// Stage 1 Creating the histogram
		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {
				for (k = 0; k < 113; k++) {
					index = cthead[k][j][i] - min;
					histogram[index]++;
				}
			}
		}

		// Stage 2 Create the cumulative distribution function t
		for (int n = 0; n < max - min + 1; n++) {
			if (n == 0) {
				t[0] = histogram[0];
			} else {
				t[n] = t[n - 1] + histogram[n];
			}
		}
		// Stage 3 create mapping
		for (int n = 0; n < max - min + 1; n++) {
			mapping[n] = (t[n] / Size);
		}
		// Stage 4 create image
		// histogram equalization for top view
		if (histogramView == 1) {
			for (j = 0; j < h; j++) {
				for (i = 0; i < w; i++) {
					datum = cthead[zslider.valueProperty().intValue()][j][i];
					col = mapping[datum - min];
					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
				}
			}
			// histogram equalization for front view
		} else if (histogramView == 2) {
			for (j = 0; j < 113; j++) {
				for (i = 0; i < w; i++) {
					datum = cthead[j][yslider.valueProperty().intValue()][i];
					col = mapping[datum - min];
					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
				}
			}
			// histogram equalization for side view
		} else if (histogramView == 3) {
			for (j = 0; j < 113; j++) {
				for (i = 0; i < h; i++) {
					datum = cthead[j][i][xslider.valueProperty().intValue()];
					col = mapping[datum - min];
					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
				}
			}
		} else {
			datum = cthead[0][0][0];
			System.out.println("Histo error!");
		}
	}

	/**
	 * Method for resizing an image using the nearest neighbour approach.
	 * 
	 * @param image The image that will be resized.
	 * @return The resized image.
	 */
	private WritableImage resize(WritableImage image) {

		int height = (int) image.getHeight();
		int width = (int) image.getWidth();

		float x, y;
		int size = 100;

		WritableImage newImage = new WritableImage(size, size);
		PixelWriter image_writer = newImage.getPixelWriter();
		PixelReader pixelReader = image.getPixelReader();
		
		for (int j = 0; j < size; j++) {
			for (int i = 0; i < size; i++) {

				y = (float) (j * height) / size;
				x = (float) (i * width) / size;

				x = Math.round(x);
				y = Math.round(y);

				Color color = pixelReader.getColor((int) x, (int) y);
				image_writer.setColor(i, j, color);
			}
		}
		return newImage;
	}

	/**
	 * Method for resizing an image using the bilinear interpolation approach.
	 * 
	 * @param image The image that will be resized.
	 * @param size  The dimension of the new image.
	 * @return The resized image.
	 */
	private WritableImage resizeB(WritableImage image, int size) {

		WritableImage newImage = new WritableImage(500, 500);
		PixelWriter image_writer = newImage.getPixelWriter();

		float x, y, x1, y1, x2, y2;
		short datum1, datum2, datum3, datum4;
		float col1, col2, col;
		float v1, v2, v3, v4;

		for (int j = 0; j < size; j++) {
			for (int i = 0; i < size; i++) {
				x = i * 255 / size;
				y = j * 255 / size;
				x1 = (int) Math.floor(x);
				y1 = (int) Math.floor(y);
				x2 = x1 + 1;
				y2 = y1 + 1;

				datum1 = cthead[zslider.valueProperty().intValue()][(int) y1][(int) x1];
				datum2 = cthead[zslider.valueProperty().intValue()][(int) y1][(int) x2];
				datum3 = cthead[zslider.valueProperty().intValue()][(int) y2][(int) x1];
				datum4 = cthead[zslider.valueProperty().intValue()][(int) y2][(int) x2];

				v1 = (((float) datum1 - (float) min) / ((float) (max - min)));
				v2 = (((float) datum2 - (float) min) / ((float) (max - min)));
				v3 = (((float) datum3 - (float) min) / ((float) (max - min)));
				v4 = (((float) datum4 - (float) min) / ((float) (max - min)));
				// find position given color
				col1 = v1 + (v2 - v1) * ((x - x1) / (x2 - x1));
				col2 = v3 + (v4 - v3) * ((x - x1) / (x2 - x1));
				col = col1 + (col2 - col1) * ((y - y1) / (y2 - y1));

				image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
			}
		}
		return newImage;
	}

	/**
	 * This method helps in order to know which image the user clicked on from the
	 * scroll pane.
	 * 
	 * @param event        what action the user did on the scroll pane.
	 * @param size         Size of the image.
	 * @param sizeOfHvalue how much the user moved the horizontal bar.
	 * @return the index of the image the user clicked on.
	 */
	public int rounding(MouseEvent event, int size, double sizeOfHvalue) {

		return (int) Math.floor(event.getSceneX() / size + sizeOfHvalue * size);

	}

	/**
	 * This methods is making an Hbox which contains all the slices of an image
	 * which were made smaller.
	 * @param img the image from where we want to display all the thumbnails.
	 * @return the Hbox with all the thumbnails.
	 */
	public HBox Thumbnails(WritableImage img) {

		HBox hbox = new HBox();
		ImageView[] images = new ImageView[113];

		for (int i = 0; i < 113; i++) {
			MIP(img, i, 1);
			images[i] = new ImageView(resize(img));
			hbox.getChildren().add(images[i]);
		}
		
		return hbox;
	}

	public static void main(String[] args) {
		launch();
	}
}