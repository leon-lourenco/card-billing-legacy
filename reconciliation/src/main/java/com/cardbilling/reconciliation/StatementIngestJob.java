package com.cardbilling.reconciliation;

import com.cardbilling.domain.ExternalStatementLine;
import com.cardbilling.domain.repository.ExternalStatementLineRepository;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads an external statement CSV (columns: external_reference,document_number,amount_cents,
 * statement_date) and loads every row into {@code external_statement_lines}, unparsed and
 * unmatched, for reconciliation to work through later.
 *
 * <p>{@link CSVReader#readAll()} loads the whole file into memory rather than streaming
 * row by row - fine for a demo-sized file, the kind of thing that quietly stops being fine once
 * a real file lands. There is also no guard against re-ingesting the same file twice: the
 * unique constraint on {@code external_reference} will reject the duplicate rows and abort the
 * whole run, rather than skipping them - a real, reproducible instance of the "legacy batch
 * isn't safe to just rerun" problem this project's evidence is built around.
 */
@Component
public class StatementIngestJob {

    private final ExternalStatementLineRepository statementLineRepository;

    public StatementIngestJob(ExternalStatementLineRepository statementLineRepository) {
        this.statementLineRepository = statementLineRepository;
    }

    @Transactional
    public int ingest(Path csvFile) throws IOException {
        List<String[]> rows;
        try (CSVReader reader = new CSVReader(new FileReader(csvFile.toFile()))) {
            rows = reader.readAll();
        } catch (Exception e) {
            throw new IOException("Failed to read " + csvFile, e);
        }

        int ingestedCount = 0;
        for (String[] row : rows) {
            if (ingestedCount == 0 && "external_reference".equals(row[0])) {
                continue; // header row
            }
            ExternalStatementLine line = new ExternalStatementLine(
                    row[0], row[1], Long.parseLong(row[2]), LocalDate.parse(row[3]), String.join(",", row));
            statementLineRepository.save(line);
            ingestedCount++;
        }
        return ingestedCount;
    }
}
