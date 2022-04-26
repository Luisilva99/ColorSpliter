import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Main {
    static final Double PRECISION = 0.0;

    static HashMap<Color, List<int[]>> trimColors(HashMap<Color, List<int[]>> color, int imageWidth, int imageHeight, int colorPixelLimit, int colorClusterDisparity) {
        // Color trimming
        HashMap<Color, List<int[]>> colorAuxMap = new HashMap<>();
        Color colorCluster = new ArrayList<>(color.keySet()).get(0);
        int rgbaLastRedValue = 0;
        int rgbaLastGreenValue = 0;
        int rgbaLastBlueValue = 0;

        for (Color colorList : color.keySet()) {
            int rgbaCurrentRedValue = colorList.getRed();
            int rgbaCurrentGreenValue = colorList.getGreen();
            int rgbaCurrentBlueValue = colorList.getBlue();

            if (color.get(colorList).size() <= colorPixelLimit && (Math.abs((rgbaLastRedValue - rgbaCurrentRedValue)) <= colorClusterDisparity || Math.abs((rgbaLastGreenValue - rgbaCurrentGreenValue)) <= colorClusterDisparity || Math.abs((rgbaLastBlueValue - rgbaCurrentBlueValue)) <= colorClusterDisparity)) {
                if (!colorAuxMap.containsKey(colorCluster)) {
                    colorAuxMap.put(colorCluster, new ArrayList<>());
                }

                colorAuxMap.get(colorCluster).addAll(color.get(colorList));
            } else {
                colorCluster = colorList;

                if (!colorAuxMap.containsKey(colorCluster)) {
                    colorAuxMap.put(colorCluster, new ArrayList<>());
                }

                colorAuxMap.get(colorCluster).addAll(color.get(colorList));
            }

            rgbaLastRedValue = rgbaCurrentRedValue;
            rgbaLastGreenValue = rgbaCurrentGreenValue;
            rgbaLastBlueValue = rgbaCurrentBlueValue;
        }

        return colorAuxMap;
    }

    static HashMap<Color, List<int[]>> trimColorsGeneric(HashMap<Color, List<int[]>> color, int imageWidth, int imageHeight, int colorPixelLimit, int colorClusterDisparity) {
        // Color trimming
        HashMap<Color, List<int[]>> colorAuxMap = new HashMap<>();
        Color colorCluster = new ArrayList<>(color.keySet()).get(0);
        int rgbaLastValue = 0;

        for (Color colorList : color.keySet()) {
            int rgbaCurrentValue = colorList.getRed() + colorList.getGreen() + colorList.getBlue();

            if (color.get(colorList).size() <= colorPixelLimit && Math.abs((rgbaLastValue - rgbaCurrentValue)) <= colorClusterDisparity) {
                if (!colorAuxMap.containsKey(colorCluster)) {
                    colorAuxMap.put(colorCluster, new ArrayList<>());
                }

                colorAuxMap.get(colorCluster).addAll(color.get(colorList));
            } else {
                colorCluster = colorList;

                if (!colorAuxMap.containsKey(colorCluster)) {
                    colorAuxMap.put(colorCluster, new ArrayList<>());
                }

                colorAuxMap.get(colorCluster).addAll(color.get(colorList));
            }

            rgbaLastValue = rgbaCurrentValue;
        }

        return colorAuxMap;
    }

    /* K-Means++ implementation, initializes K centroids from data */
    static LinkedList<HashMap<String, Double>> kmeanspp(DataSet data, int K) {
        LinkedList<HashMap<String, Double>> centroids = new LinkedList<>();

        centroids.add(data.randomFromDataSet());

        for (int i = 1; i < K; i++) {
            centroids.add(data.calculateWeighedCentroid());
        }

        return centroids;
    }

    /* K-Means itself, it takes a dataset and a number K and adds class numbers
     * to records in the dataset */
    static void kmeans(DataSet data, int K) {
        // select K initial centroids
        LinkedList<HashMap<String, Double>> centroids = kmeanspp(data, K);

        // initialize Sum of Squared Errors to max, we'll lower it at each iteration
        Double SSE = Double.MAX_VALUE;

        // breaking iteration counter
        int iteration = 0;

        while (true) {

            // assign observations to centroids
            var records = data.getRecords();

            // for each record
            for (var record : records) {
                Double minDist = Double.MAX_VALUE;
                // find the centroid at a minimum distance from it and add the record to its cluster
                for (int i = 0; i < centroids.size(); i++) {
                    Double dist = DataSet.euclideanDistance(centroids.get(i), record.getRecord());
                    if (dist < minDist) {
                        minDist = dist;
                        record.setClusterNo(i);
                    }
                }

            }

            // recompute centroids according to new cluster assignments
            centroids = data.recomputeCentroids(K);

            // exit condition, SSE changed less than PRECISION parameter
            Double newSSE = data.calculateTotalSSE(centroids);

            if (SSE - newSSE <= PRECISION) {
                break;
            }
            SSE = newSSE;

            System.out.println("Finnish an iteration. Iteration " + iteration++ + ".");
        }
    }

    public static void main(String[] args) {
        File file;
        HashMap<Color, List<int[]>> colorMap = new HashMap<>();
        int imageWidth, imageHeight;
        try {
            //file = new File("src/main/resources/default_meme.bmp");
            file = new File("src/main/resources/pine_tree.png");
            //file = new File("src/main/resources/2_color_test.bmp");
            //file = new File("src/main/resources/mosaic.png");
            //file = new File("src/main/resources/tree_sample.png");
            //file = new File("src/main/resources/digital_zone.png");
            BufferedImage image = ImageIO.read(file);
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();

            for (int i = 0; image.getWidth() > i; i++) {
                for (int j = 0; image.getHeight() > j; j++) {
                    Color pixel = new Color(image.getRGB(i, j));
                    if (!colorMap.containsKey(pixel)) {
                        colorMap.put(pixel, new ArrayList<>());
                    }
                    colorMap.get(pixel).add(new int[]{i, j});
                }
            }

            StringBuilder str = new StringBuilder();

            for (Color color : colorMap.keySet()) {
                str.append("\n").append("-----------------------------");
                str.append("\n").append("Red:\t").append(color.getRed());
                str.append("\n").append("Green:\t").append(color.getGreen());
                str.append("\n").append("Blue:\t").append(color.getBlue());
                str.append("\n").append("Alpha:\t").append(color.getAlpha());
                str.append("\n").append("This color has ").append(colorMap.get(color).size()).append(" pixel cells of this color.");
            }

            str.append("\n").append("||||||||||||||||||||||||||");
            str.append("\n").append("Total of different colors:\t").append(colorMap.keySet().size()).append(".").append("\n");

            System.out.println(str);

            // Color trimming
            HashMap<Color, List<int[]>> colorAuxMap;
            int colorPixelLimit = (int) Math.floor(Math.sqrt(Math.floor(Math.sqrt(imageWidth * imageHeight))));
            int colorClusterDisparity = 20;

            colorAuxMap = trimColorsGeneric(colorMap, imageWidth, imageHeight, colorPixelLimit, colorClusterDisparity);

            System.out.println("\nTotal of different colors after treatment: " + colorAuxMap.keySet().size() + "\n");

            for (int i = 0; i < 2000 && colorAuxMap.size() > 8000; i++) {
                colorAuxMap = trimColorsGeneric(colorMap, imageWidth, imageHeight, colorPixelLimit, colorClusterDisparity);
            }

            System.out.println("\nTotal of different colors after treatment: " + colorAuxMap.keySet().size() + "\n");

            // This K-Means is this algorithm here - https://github.com/Mentathiel/KMeansJava

            // Read data
            DataSet data = new DataSet(colorAuxMap);

            // Clusters
            kmeans(data, 20);

            // output into color clusters
            HashMap<Integer, List<Color>> imageColorMapping = data.createOutputColorMap(colorAuxMap);

            System.out.println("\nTotal of different image parts that will be created: " + imageColorMapping.keySet().size() + "\n");

            for (int colorSection : imageColorMapping.keySet()) {
                // Constructs a BufferedImage of one of the predefined image types.
                BufferedImage imageColorPart = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

                // Create a graphics which can be used to draw into the buffered image
                Graphics2D g2d = imageColorPart.createGraphics();

                g2d.setComposite(AlphaComposite.Clear);
                g2d.fillRect(0, 0, imageWidth, imageHeight);

                for (Color colorList : imageColorMapping.get(colorSection)) {
                    for (int[] pixelLocation : colorAuxMap.get(colorList)) {
                        g2d.setComposite(AlphaComposite.Src);
                        g2d.setColor(colorList);
                        g2d.fillRect(pixelLocation[0], pixelLocation[1], 1, 1);
                    }
                }

                // Disposes of this graphics context and releases any system resources that it is using.
                g2d.dispose();

                // Save as PNG
                File imagePart = new File("src/splitImages/colorImage" + colorSection + ".png");
                ImageIO.write(imageColorPart, "png", imagePart);
            }

            // Constructs a BufferedImage of one of the predefined image types.
            BufferedImage imageColorPart = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

            // Create a graphics which can be used to draw into the buffered image
            Graphics2D g2d = imageColorPart.createGraphics();

            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, imageWidth, imageHeight);

            for (int colorSection : imageColorMapping.keySet()) {
                for (Color colorList : imageColorMapping.get(colorSection)) {
                    for (int[] pixelLocation : colorAuxMap.get(colorList)) {
                        g2d.setComposite(AlphaComposite.Src);
                        g2d.setColor(colorList);
                        g2d.fillRect(pixelLocation[0], pixelLocation[1], 1, 1);
                    }
                }
            }

            // Disposes of this graphics context and releases any system resources that it is using.
            g2d.dispose();

            // Save as PNG
            File imagePart = new File("src/splitImages/colorImageFused.png");
            ImageIO.write(imageColorPart, "png", imagePart);

            TrayIconDemo td = new TrayIconDemo("src/main/resources/warning_icon.png", "Color Splitter has finnish!", "Check it out!");
            td.displayTray();
        } catch (IOException e) {
            String workingDir = System.getProperty("user.dir");
            System.out.println("Current working directory : " + workingDir);
            e.printStackTrace();
        } catch (AWTException e) {
            System.out.println("Error in notifying user.");
            e.printStackTrace();
        }
    }
}
