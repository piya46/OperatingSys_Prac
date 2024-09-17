import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ErrorDiffusion {
    private final int width;
    private final int height;
    private final byte[][] image;
    private final byte[][] output;
    private final int numThreads;

    public ErrorDiffusion(int width, int height, int numThreads) {
        this.width = width + 2; // เพิ่มช่องว่างที่ขอบซ้ายและขวา
        this.height = height + 2; // เพิ่มช่องว่างที่ขอบบนและล่าง
        this.image = new byte[this.height][this.width];
        this.output = new byte[this.height][this.width];
        this.numThreads = numThreads;
    }

    // การตั้งค่า input image จาก byte[][] ที่ได้รับมา
    public void setInputImage(byte[][] img) {
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                image[i][j] = img[i - 1][j - 1]; // คัดลอกภาพเข้าไปในพื้นที่ที่ไม่ใช่ขอบ
            }
        }
    }

    // ฟังก์ชันหลักสำหรับการดำเนินการ Error Diffusion
    public void performErrorDiffusion() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 1; i < height - 1; i += numThreads) {
            final int rowStart = i;
            executor.submit(() -> {
                for (int row = rowStart; row < Math.min(rowStart + numThreads, height - 1); row++) {
                    diffuseRow(row);
                }
            });
        }

        // ปิดการทำงานของเธรดทั้งหมด
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    // กระบวนการ Diffusion สำหรับแต่ละแถว
    private void diffuseRow(int row) {
        for (int col = 1; col < width - 1; col++) {
            int oldPixel = Byte.toUnsignedInt(image[row][col]);
            int newPixel = oldPixel > 127 ? 255 : 0; // Threshold ที่ 127
            output[row][col] = (byte) newPixel;
            int error = oldPixel - newPixel;

            // กระจายความผิดพลาดไปยังพิกเซลข้างเคียง
            image[row][col + 1] += error * 7 / 16;
            image[row + 1][col - 1] += error * 3 / 16;
            image[row + 1][col] += error * 5 / 16;
            image[row + 1][col + 1] += error * 1 / 16;
        }
    }

    // ฟังก์ชันเพื่อดึงข้อมูลภาพที่ผ่านการ Diffuse แล้ว
    public byte[][] getOutputImage() {
        return output;
    }

    // ฟังก์ชันในการแปลง BufferedImage เป็น byte[][]
    public static byte[][] convertImageToByteArray(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        byte[][] byteImage = new byte[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF; // ใช้ค่าเฉลี่ยสีเทา
                byteImage[y][x] = (byte) gray;
            }
        }

        return byteImage;
    }

    // ฟังก์ชันในการแปลง byte[][] กลับเป็น BufferedImage เพื่อบันทึกเป็นไฟล์
    public static BufferedImage convertByteArrayToImage(byte[][] byteImage) {
        int height = byteImage.length;
        int width = byteImage[0].length;
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int gray = Byte.toUnsignedInt(byteImage[y][x]);
                int rgb = (gray << 16) | (gray << 8) | gray;
                outputImage.setRGB(x, y, rgb);
            }
        }

        return outputImage;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // อ่านภาพจากไฟล์
        String fileName = "miraidon.png";
        File inputFile = new File(fileName);
        BufferedImage inputImage = ImageIO.read(inputFile);

        int width = inputImage.getWidth();
        int height = inputImage.getHeight();
        int numThreads = Runtime.getRuntime().availableProcessors() - 1; // ใช้จำนวนเธรดน้อยกว่าจำนวนคอร์

        // แปลงภาพเป็น byte[][] สำหรับการประมวลผล
        byte[][] inputByteArray = convertImageToByteArray(inputImage);

        // บันทึกภาพที่เป็น Grayscale ก่อนประมวลผล
        BufferedImage grayImage = convertByteArrayToImage(inputByteArray);
        File grayOutputFile = new File("%s_gray_output.png");
        ImageIO.write(grayImage, "png", grayOutputFile);

        System.out.println("บันทึกภาพ Grayscale เรียบร้อย");

        // เริ่มการกระจายความผิดพลาด
        ErrorDiffusion errorDiffusion = new ErrorDiffusion(width, height, numThreads);
        errorDiffusion.setInputImage(inputByteArray);
        errorDiffusion.performErrorDiffusion();

        // ดึงภาพที่ผ่านการประมวลผล
        byte[][] outputByteArray = errorDiffusion.getOutputImage();

        // แปลง byte[][] กลับเป็น BufferedImage
        BufferedImage outputImage = convertByteArrayToImage(outputByteArray);

        // บันทึกภาพที่ประมวลผลแล้วไปยังไฟล์
        File outputFile = new File("%s_output_diffusion.png");
        ImageIO.write(outputImage, "png", outputFile);

        System.out.println("บันทึกภาพที่ประมวลผลgเสร็จแล้ว");
    }
}
