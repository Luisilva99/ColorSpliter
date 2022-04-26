import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ColorMap {
    private HashMap<Color, List<int[]>> colorMap;
    private Integer imageWidth;
    private Integer imageHeight;

    public ColorMap(HashMap<Color, List<int[]>> colorMap) {
        this.colorMap = new HashMap<>(colorMap);
    }

    public ColorMap(String imageFileName) throws IOException {
        File file = new File("src/main/resources/" + imageFileName);
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
    }

    private void addColor(Color color, int[] pixelLocation) {
        if (!colorMap.containsKey(color)) {
            colorMap.put(color, new ArrayList<>());
        }

        colorMap.get(color).add(pixelLocation);
    }

    private void removeColorPixel(Color color, int[] pixelLocation) throws Exception {
        if (!colorMap.containsKey(color)) {
            throw new Exception("Color nonexistent.");
        }

        colorMap.get(color).remove(pixelLocation);
    }

    public java.util.Set<Color> getColorKeys() {
        return colorMap.keySet();
    }

    public List<int[]> getColorPixelList(Color color) throws Exception {
        if (!colorMap.containsKey(color)) {
            throw new Exception("Color nonexistent.");
        }

        return colorMap.get(color);
    }

    public HashMap<Color, List<int[]>> trimColorDistribution(int colorPixelLimit, int colorClusterDisparity) {
        HashMap<Color, List<int[]>> colorAuxMap = new HashMap<>();
        Color colorCluster = new ArrayList<>(colorMap.keySet()).get(0);
        int rgbaLastValue = 0;

        for (Color colorList : colorMap.keySet()) {
            int rgbaCurrentValue = (colorList.getRed() + colorList.getGreen() + colorList.getBlue());
            if (colorMap.get(colorList).size() <= colorPixelLimit && Math.abs((rgbaLastValue - rgbaCurrentValue)) <= colorClusterDisparity) {
                if (!colorAuxMap.containsKey(colorCluster)) {
                    colorAuxMap.put(colorCluster, new ArrayList<>());
                }

                colorAuxMap.get(colorCluster).addAll(colorMap.get(colorList));
            } else {
                colorCluster = colorList;

                if (!colorAuxMap.containsKey(colorCluster)) {
                    colorAuxMap.put(colorCluster, new ArrayList<>());
                }

                colorAuxMap.get(colorCluster).addAll(colorMap.get(colorList));
            }

            rgbaLastValue = rgbaCurrentValue;
        }

        System.out.println("\nTotal of different colors after trimming (returned color map): " + colorAuxMap.keySet().size() + "\n");

        return colorAuxMap;
    }

    public void toImages(DataSet data) throws IOException {
        // output into color clusters
        HashMap<Integer, List<Color>> imageColorMapping = data.createOutputColorMap(colorMap);

        System.out.println("\nTotal of different image parts that will be created: " + (imageColorMapping.keySet().size() + 1) + ".\n");

        for (int colorSection : imageColorMapping.keySet()) {
            // Constructs a BufferedImage of one of the predefined image types.
            BufferedImage imageColorPart = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

            // Create a graphics which can be used to draw into the buffered image
            Graphics2D g2d = imageColorPart.createGraphics();

            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, imageWidth, imageHeight);

            for (Color colorList : imageColorMapping.get(colorSection)) {
                for (int[] pixelLocation : colorMap.get(colorList)) {
                    g2d.setColor(colorList);
                    g2d.setComposite(AlphaComposite.SrcIn);
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
                for (int[] pixelLocation : colorMap.get(colorList)) {
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
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        for (Color color : colorMap.keySet()) {
            str.append("-----------------------------");
            str.append("\n").append("Red:\t").append(color.getRed()).append("\n");
            str.append("\n").append("Green:\t").append(color.getGreen()).append("\n");
            str.append("\n").append("Blue:\t").append(color.getBlue()).append("\n");
            str.append("\n").append("Alpha:\t").append(color.getAlpha()).append("\n");
            str.append("This color has ").append(colorMap.get(color).size()).append(" pixel cells of this color.");
        }

        str.append("\n").append("||||||||||||||||||||||||||");
        str.append("\n").append("Total of different colors:\t").append(colorMap.keySet().size()).append(".").append("\n");

        return str.toString();
    }
}
