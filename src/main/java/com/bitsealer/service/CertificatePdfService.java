package com.bitsealer.service;

import com.bitsealer.model.FileHash;
import com.bitsealer.model.FileStamp;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CertificatePdfService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Color MAIN_BLUE = new Color(41, 128, 185);
    private static final Color LIGHT_GRAY = new Color(245, 245, 245);
    private static final Color TEXT_GRAY = new Color(100, 100, 100);

    public byte[] generate(FileHash fileHash, FileStamp stamp, String otsProofSha256Hex) {
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float width = page.getMediaBox().getWidth();
            float height = page.getMediaBox().getHeight();
            float margin = 50;
            float y = height - 40;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                
                // --- CABECERA ---
                cs.setNonStrokingColor(MAIN_BLUE);
                cs.addRect(0, height - 80, width, 80);
                cs.fill();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 22);
                cs.setNonStrokingColor(Color.WHITE);
                cs.newLineAtOffset(margin, height - 50);
                cs.showText("BitSealer");
                cs.endText();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.newLineAtOffset(margin, height - 65);
                cs.showText("CERTIFICADO DE SELLADO DE TIEMPO (OPENTIMESTAMPS)");
                cs.endText();

                y = height - 120;

                // --- INFORMACIÓN GENERAL (Caja Gris) ---
                drawBox(cs, margin, y - 65, width - (margin * 2), 75);
                y -= 20;
                y = writeKeyValue(cs, margin + 15, y, "Fecha de generación", DT.format(LocalDateTime.now()), false);
                y = writeKeyValue(cs, margin + 15, y, "Archivo origen", safe(fileHash.getFileName()), false);
                y = writeKeyValue(cs, margin + 15, y, "Identificador Stamp", stamp.getId() != null ? String.valueOf(stamp.getId()) : "-", false);

                y -= 40;

                // --- SECCIÓN: PRUEBA CRIPTOGRÁFICA ---
                y = writeSectionTitle(cs, margin, y, "DETALLES DE LA PRUEBA (OTS)");
                y = writeKeyValue(cs, margin, y, "SHA-256 Archivo", safe(fileHash.getSha256()), true);
                y = writeKeyValue(cs, margin, y, "SHA-256 Proof", safe(otsProofSha256Hex), true);
                y = writeKeyValue(cs, margin, y, "Tamaño Proof", stamp.getOtsProof() != null ? stamp.getOtsProof().length + " bytes" : "-", false);

                y -= 30;

                // --- SECCIÓN: ESTADO EN BLOCKCHAIN ---
                y = writeSectionTitle(cs, margin, y, "ESTADO DEL SELLADO");
                
                // Badge de Estado
                String status = stamp.getStatus() != null ? stamp.getStatus().name() : "UNKNOWN";
                drawStatusBadge(cs, margin, y, status);
                y -= 25;

                y = writeKeyValue(cs, margin, y, "Transacción (TXID)", safe(stamp.getTxid()), true);
                y = writeKeyValue(cs, margin, y, "Fecha Creación", stamp.getCreatedAt() != null ? DT.format(stamp.getCreatedAt()) : "-", false);
                y = writeKeyValue(cs, margin, y, "Fecha Sellado", stamp.getSealedAt() != null ? DT.format(stamp.getSealedAt()) : "PENDIENTE", false);
                y = writeKeyValue(cs, margin, y, "Intentos realizados", String.valueOf(stamp.getAttempts()), false);

                if (stamp.getLastError() != null && !stamp.getLastError().isBlank()) {
                    y -= 20;
                    cs.setNonStrokingColor(new Color(192, 57, 43));
                    y = writeSectionTitle(cs, margin, y, "AVISO / ERROR");
                    y = writeParagraph(cs, margin, y, stamp.getLastError(), PDType1Font.HELVETICA, 10);
                }

                // --- PIE DE PÁGINA ---
                cs.setStrokingColor(Color.LIGHT_GRAY);
                cs.moveTo(margin, 70);
                cs.lineTo(width - margin, 70);
                cs.stroke();

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 8);
                cs.setNonStrokingColor(TEXT_GRAY);
                String footer = "Este documento es un certificado de integridad. " +
                                "Para validar la autenticidad, se requiere el archivo original y el archivo de prueba .ots generado.";
                float footerWidth = PDType1Font.HELVETICA_OBLIQUE.getStringWidth(footer) / 1000 * 8;
                cs.newLineAtOffset((width - footerWidth) / 2, 50);
                cs.showText(footer);
                cs.endText();
            }

            doc.save(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF profesional", e);
        }
    }

    private void drawBox(PDPageContentStream cs, float x, float y, float w, float h) throws Exception {
        cs.setNonStrokingColor(LIGHT_GRAY);
        cs.addRect(x, y, w, h);
        cs.fill();
    }

    private void drawStatusBadge(PDPageContentStream cs, float x, float y, String status) throws Exception {
        boolean isSealed = "SEALED".equalsIgnoreCase(status);
        cs.setNonStrokingColor(isSealed ? new Color(39, 174, 96) : new Color(243, 156, 18));
        cs.addRect(x, y - 5, 80, 15);
        cs.fill();
        
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 9);
        cs.setNonStrokingColor(Color.WHITE);
        cs.newLineAtOffset(x + 5, y);
        cs.showText(status);
        cs.endText();
    }

    private float writeSectionTitle(PDPageContentStream cs, float x, float y, String title) throws Exception {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.setNonStrokingColor(MAIN_BLUE);
        cs.newLineAtOffset(x, y);
        cs.showText(title);
        cs.endText();
        
        cs.setStrokingColor(MAIN_BLUE);
        cs.setLineWidth(1f);
        cs.moveTo(x, y - 4);
        cs.lineTo(x + 50, y - 4);
        cs.stroke();
        
        return y - 25;
    }

    private float writeKeyValue(PDPageContentStream cs, float x, float y, String k, String v, boolean mono) throws Exception {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.setNonStrokingColor(TEXT_GRAY);
        cs.newLineAtOffset(x, y);
        cs.showText(k + ":");
        cs.endText();

        cs.setNonStrokingColor(Color.BLACK);
        float vx = x + 120;
        return writeParagraph(cs, vx, y, v, mono ? PDType1Font.COURIER : PDType1Font.HELVETICA_BOLD, 10);
    }

    private float writeParagraph(PDPageContentStream cs, float x, float y, String text, 
                                 org.apache.pdfbox.pdmodel.font.PDFont font, int size) throws Exception {
        List<String> lines = wrap(text, 70);
        float leading = size + 4;
        float yy = y;

        for (String line : lines) {
            cs.beginText();
            cs.setFont(font, size);
            cs.newLineAtOffset(x, yy);
            cs.showText(line);
            cs.endText();
            yy -= leading;
        }
        return yy;
    }

    private List<String> wrap(String s, int max) {
        String text = (s == null) ? "-" : s;
        List<String> out = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            int end = Math.min(i + max, text.length());
            out.add(text.substring(i, end));
            i = end;
        }
        return out;
    }

    private String safe(String s) {
        if (s == null || s.isBlank()) return "-";
        return s.replaceAll("[\\r\\n\\t]", " ").trim();
    }
}