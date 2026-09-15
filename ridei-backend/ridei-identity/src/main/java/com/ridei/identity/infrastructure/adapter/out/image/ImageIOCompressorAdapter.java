package com.ridei.identity.infrastructure.adapter.out.image;

import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.ridei.identity.domain.port.out.ImageProcessorPort;

@Component 
public class ImageIOCompressorAdapter implements ImageProcessorPort {

    private static final int MAX_ATTEMPTS = 8;

    @Override
    public byte[] compressToFit(byte[] originalImage, long maxBytes) {
        BufferedImage image = readImage(originalImage);

        float quality = 0.9f;
        double scale = 1.0;
        byte[] result = null;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            BufferedImage candidate = scale < 1.0 ? resize(image, scale): image;
            result = encodeJpeg(candidate, quality);

            if (result.length <= maxBytes) {
                return result;
            }

            if (quality > 0.4f) {
                quality -= 0.15f;
            } else {
                scale *= 0.8;
            }
        }

        return result;
    }

    private BufferedImage readImage(byte[] bytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new IllegalArgumentException("error.unsupported_image_content_type");
            }
            return image;
        } catch (IOException e) {
            throw new IllegalArgumentException("error.unsupported_image_content_type");
        }
    }

    private BufferedImage resize(BufferedImage original, double scale) {
        int width = Math.max(1, (int) (original.getWidth() * scale));
        int height = Math.max(1, (int) (original.getHeight() * scale));

        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    private byte[] encodeJpeg(BufferedImage image, float quality) {
        try {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next();

            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(output)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(image, null, null), param);
            }
            writer.dispose();

            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to encode image", e);
        }
    }
    
}
