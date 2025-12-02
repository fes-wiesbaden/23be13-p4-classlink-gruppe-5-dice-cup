package de.dicecup.classlink.features.registration;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class QrCodeService {

    private static final String INVITE_TEMPLATE = "https://classlink.local/register?token=%s";
    private static final String RESET_TEMPLATE = "https://classlink.local/reset?token=%s";

    public byte[] inviteQr(String token) {
        return generate(buildInviteUrl(token));
    }

    public byte[] passwordResetQr(String token) {
        return generate(buildResetUrl(token));
    }

    public String buildInviteUrl(String token) {
        return INVITE_TEMPLATE.formatted(token);
    }

    public String buildResetUrl(String token) {
        return RESET_TEMPLATE.formatted(token);
    }

    private byte[] generate(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(matrix, "PNG", out);
                return out.toByteArray();
            }
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Could not generate QR code", e);
        }
    }
}
