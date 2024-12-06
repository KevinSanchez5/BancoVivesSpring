package vives.bancovives.rest.movements.storage;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vives.bancovives.rest.accounts.model.Account;
import vives.bancovives.rest.movements.exceptions.MovementBadRequest;
import vives.bancovives.rest.movements.model.Movement;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

@Slf4j
@Service
public class MovementsStoragePDFImpl implements MovementsStoragePDF{
    @Override
    public void save(File file, Account account, List<Movement> movements) {
        log.info("Guardando movimientos de la cuenta con id: {}", account.getId());
        try (FileOutputStream fos = new FileOutputStream(file)) {
            Document document = new Document();
            PdfWriter.getInstance(document, fos);
            document.open();

            //Título
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Movimientos:", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            //Info
            Font textFont = new Font(Font.HELVETICA, 12);
            Paragraph text = new Paragraph("Here is a list of items with their quantities and prices.", textFont);
            text.setSpacingBefore(20);
            text.setSpacingAfter(20);
            document.add(text);

            //Tabla con los movimientos
            PdfPTable table = new PdfPTable(3); // 3 columns
            table.setWidthPercentage(100);

            addTableHeader(table, List.of("Fecha", "Tu cuenta", "Otra cuenta", "Importe", "Balance"));

            for (Movement item : movements) {
                String date = item.getUpdatedAt().getYear() + "-" + item.getUpdatedAt().getMonth() + "-" + item.getUpdatedAt().getDayOfMonth();
                table.addCell(date);
                if (account.getIban().equals(item.getAccountOfDestination().getIban())) {
                    table.addCell(item.getAccountOfReference().getIban());
                    table.addCell(String.valueOf(item.getAccountOfReference()));
                    table.addCell("-" + item.getAmountOfMoney());
                    table.addCell(String.valueOf(item.getAccountOfDestination().getBalance()));
                }else {
                    table.addCell(item.getAccountOfReference().getIban());
                    table.addCell(item.getAccountOfDestination().getIban());
                    table.addCell("+" + item.getAmountOfMoney());
                    table.addCell(String.valueOf(item.getAccountOfDestination().getBalance()));
                }
            }

            document.add(table);

            document.close();
        } catch (Exception e) {
            throw new MovementBadRequest("Hubo un error al crear el archivo PDF");
        }
    }

    private static void addTableHeader(PdfPTable table, List<String> headers) {
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        for (String header : headers) {
            table.addCell(new Phrase(header, headerFont));
        }
    }

}
