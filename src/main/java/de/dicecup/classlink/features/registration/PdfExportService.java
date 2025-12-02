package de.dicecup.classlink.features.registration;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfExportService {

    public byte[] exportSheet(String title, List<QrCodeDescriptor> descriptors) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            if (title != null && !title.isBlank()) {
                Paragraph heading = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
                heading.setAlignment(Element.ALIGN_CENTER);
                heading.setSpacingAfter(18);
                document.add(heading);
            }

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(12f);
            table.setSpacingAfter(12f);

            for (QrCodeDescriptor descriptor : descriptors) {
                PdfPCell cell = new PdfPCell();
                cell.setPadding(10f);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);

                Image qrImage = Image.getInstance(descriptor.qrPng());
                qrImage.scaleToFit(200f, 200f);
                qrImage.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(qrImage);

                Paragraph label = new Paragraph(descriptor.label(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
                label.setAlignment(Element.ALIGN_CENTER);
                label.setSpacingBefore(8f);
                cell.addElement(label);

                if (descriptor.description() != null && !descriptor.description().isBlank()) {
                    Paragraph desc = new Paragraph(descriptor.description(), FontFactory.getFont(FontFactory.HELVETICA, 10));
                    desc.setAlignment(Element.ALIGN_CENTER);
                    desc.setSpacingBefore(4f);
                    cell.addElement(desc);
                }

                table.addCell(cell);
            }

            if (table.size() % 2 != 0) {
                table.addCell(new PdfPCell());
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Failed to create PDF", e);
        }
    }

    public record QrCodeDescriptor(String label, byte[] qrPng, String description) {
    }
}
